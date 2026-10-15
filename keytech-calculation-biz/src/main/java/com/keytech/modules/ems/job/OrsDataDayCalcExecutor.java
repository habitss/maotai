package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.ems.constants.CollectErrorFlagEnum;
import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.constants.StatusFinishFlagEnum;
import com.keytech.modules.ems.constants.TagKindCharEnum;
import com.keytech.modules.ems.entity.*;
import com.keytech.modules.ems.mapper.CollectDayUsageMapper;
import com.keytech.modules.ems.mapper.ManualDayUsageMapper;
import com.keytech.modules.ems.mapper.ThirdpartyDayUsageMapper;
import com.keytech.modules.ems.service.IAggregationCalcExecutor;
import com.keytech.modules.ems.service.IAggregationStatusService;
import com.keytech.modules.ems.service.IOrsdataFormulaService;
import com.keytech.modules.ems.service.impl.AggregationCalcService;
import com.keytech.modules.ems.util.FormulaParser;
import com.keytech.modules.ems.vo.FormulaParserResVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 中间数据天聚合执行器
 * @author yepeng
 * @since 2025.05.15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrsDataDayCalcExecutor implements IAggregationCalcExecutor {
    private final IOrsdataFormulaService orsdataFormulaService;
    private final IAggregationStatusService aggregationStatusService;
    private final CollectDayUsageMapper collectDayUsageMapper;
    private final ThirdpartyDayUsageMapper thirdpartyDayUsageMapper;
    private final ManualDayUsageMapper manualDayUsageMapper;
    private final AggregationCalcService aggregationCalcService;


    @Override
    public void execute(List<AggregationStatus> statusList) {
        if (statusList.isEmpty()) {
            return;
        }
        //这里是计算天聚合，所以取天粒度
        LocalDateTime utime = statusList.get(0).getTimePoint().truncatedTo(ChronoUnit.DAYS);

        List<String> orsTagIds = statusList.stream().map(AggregationStatus::getTagId).distinct().toList();
        //查询中间点位最新版公式
        List<OrsdataFormula> orsdataFormulaList = orsdataFormulaService.selectLastOrsFormulaByTagIds(orsTagIds);
        Map<String, OrsdataFormula> orsdataFormulaMap = orsdataFormulaList.stream().collect(Collectors.toMap(OrsdataFormula::getTagId, Function.identity()));
        //查看是否有没设置公式的点位
        List<Integer> noFormulaTagStatusIdList = statusList.stream().filter(s -> !orsdataFormulaMap.containsKey(s.getTagId())).map(AggregationStatus::getId).toList();
        if (!noFormulaTagStatusIdList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(noFormulaTagStatusIdList, "未找到中间点位当前时间的公式");
        }

        //只计算有公式的点位
        orsTagIds = orsdataFormulaMap.keySet().stream().distinct().toList();

        //取出所有公式中的基础点位(C:123 M:234 T:345)
        Set<String> allFormulaTagIds = new HashSet<>();
        for (String tagId : orsTagIds) {
            String analyseFormula = this.orsdataFormulaService.getAnalyseFormulaByOrigin(orsdataFormulaMap.get(tagId).getFormulaOrigin());
            allFormulaTagIds.addAll(FormulaParser.parseAllRelatedTagIds(analyseFormula));
        }

        //按点位大类分组取各点位的用量
        Map<String, BigDecimal> tagCharId2UsageMap = getTagCharId2UsageIdMap(allFormulaTagIds, utime);

        // 逐个判断是否能计算
        Map<Integer, String> failStatusMap = new HashMap<>();
        // 除法分母为0的直接给置为0视为聚合成功但是备注一下
        List<Integer> nanStatusList = new ArrayList<>();
        Map<Integer, OrsdataDayUsage> succStatusMap = new HashMap<>();
        for (AggregationStatus status : statusList) {
            try {
                String analyseFormula = this.orsdataFormulaService.getAnalyseFormulaByOrigin(orsdataFormulaMap.get(status.getTagId()).getFormulaOrigin());
                FormulaParserResVo result = FormulaParser.evaluateToObj(analyseFormula, tagCharId2UsageMap);
                //判断是否NaN
                boolean isNaN = Double.isNaN(result.getResult())  || Double.isInfinite(result.getResult());
                if (isNaN) {
                    log.warn("碰到分母为0导致NaN的直接给置为0");
                    result.setResult(0.0);
                    nanStatusList.add(status.getId());
                }
                BigDecimal fusage = BigDecimal.valueOf(result.getResult()).setScale(4, RoundingMode.HALF_UP);
                OrsdataDayUsage aggregatUsage = OrsdataDayUsage.builder()
                    .tenantId(status.getTenantId())
                    .factoryCode(status.getFactoryCode())
                    .deptCode(status.getDeptCode())
                    .lineCode(status.getLineCode())
                    .tagId(status.getTagId())
                    //用量归属日期取开始时间与结束时间的中间值日期
                    .utime(status.getTimePoint().plusHours(12).toLocalDate())
                    .stime(status.getTimePoint())
                    .etime(status.getTimePoint().plusHours(24))
                    .fusage(fusage)
                    .createBy(EmsConstantsX.SYSTEM)
                    .build();
                if (!isNaN) {
                    succStatusMap.put(status.getId(), aggregatUsage);
                }
            } catch (Exception e) {
                if (!(e instanceof JeecgBootBizTipException)) {
                    log.error("中间点天数据计算失败 statusId={} pointId={}", status.getId(), status.getTagId(), e);
                }
                failStatusMap.put(status.getId(), e.getMessage());
            }
        }

        //中间点天数据入库及批量修改成功聚合状态
        if (!succStatusMap.isEmpty()) {
            this.aggregationCalcService.batchSaveOrUpdateOrsdataDayUsage(succStatusMap.values().stream().toList());
            this.aggregationStatusService.batchUpdateFinished(succStatusMap.keySet());
        }
        //批量修改NaN的聚合状态
        if (!nanStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateStatus(nanStatusList, StatusFinishFlagEnum.FINISHED.getVal(), CollectErrorFlagEnum.NORMAL.getVal(), "除数(分母)为0导致NaN置为0");
        }
        //批量修改错误聚合状态
        if (!failStatusMap.isEmpty()) {
            for (Map.Entry<Integer, String> entry : failStatusMap.entrySet()) {
                AggregationStatus aggregationStatus = new AggregationStatus();
                aggregationStatus.setId(entry.getKey());
                aggregationStatus.setErrorFlag(CollectErrorFlagEnum.UNUSUAL.getVal());
                aggregationStatus.setErrorMsg(entry.getValue());
                this.aggregationStatusService.updateById(aggregationStatus);
            }
        }
    }

    /**
     * 按点位大类分组取各点位的用量
     * @param allFormulaTagIds 所有公式中的基础点位(C:123 M:234 T:345)
     */
    private Map<String, BigDecimal> getTagCharId2UsageIdMap(Set<String> allFormulaTagIds, LocalDateTime utime) {
        Map<String, BigDecimal> tagCharId2UsageMap = new HashMap<>();
        Map<String, List<String>> formulaTagMap = allFormulaTagIds.stream().collect(Collectors.groupingBy(s->s.split(":")[0]));
        formulaTagMap.keySet().forEach(tagKindChar -> {
            List<String> tagIds = formulaTagMap.get(tagKindChar).stream().map(s -> s.split(":")[1]).distinct().toList();
            if (tagKindChar.equals(TagKindCharEnum.COLLECT.getCode())) {
                LambdaQueryWrapper<CollectDayUsage> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(Arrays.asList(CollectDayUsage::getTagId, CollectDayUsage::getFusage)).in(CollectDayUsage::getTagId, tagIds).eq(CollectDayUsage::getUtime, utime);
                List<CollectDayUsage> collectDayUsageList = collectDayUsageMapper.selectList(queryWrapper);
                tagCharId2UsageMap.putAll(collectDayUsageList.stream().collect(Collectors.toMap(s -> tagKindChar + ":" + s.getTagId(), CollectDayUsage::getFusage)));
            } else if (tagKindChar.equals(TagKindCharEnum.THIRDPARTY.getCode())) {
                LambdaQueryWrapper<ThirdpartyDayUsage> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(Arrays.asList(ThirdpartyDayUsage::getTagId, ThirdpartyDayUsage::getFusage)).in(ThirdpartyDayUsage::getTagId, tagIds).eq(ThirdpartyDayUsage::getUtime, utime);
                List<ThirdpartyDayUsage> thirdpartyDayUsageList = thirdpartyDayUsageMapper.selectList(queryWrapper);
                tagCharId2UsageMap.putAll(thirdpartyDayUsageList.stream().collect(Collectors.toMap(s -> tagKindChar + ":" + s.getTagId(), ThirdpartyDayUsage::getFusage)));
            } else if (tagKindChar.equals(TagKindCharEnum.MANUAL.getCode())) {
                LambdaQueryWrapper<ManualDayUsage> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(Arrays.asList(ManualDayUsage::getTagId, ManualDayUsage::getFusage)).in(ManualDayUsage::getTagId, tagIds).eq(ManualDayUsage::getUtime, utime);
                List<ManualDayUsage> manualDayUsageList = manualDayUsageMapper.selectList(queryWrapper);
                tagCharId2UsageMap.putAll(manualDayUsageList.stream().collect(Collectors.toMap(s -> tagKindChar + ":" + s.getTagId(), ManualDayUsage::getFusage)));
            }
        });
        return tagCharId2UsageMap;
    }

}
