package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.entity.ThirdpartyDayUsage;
import com.keytech.modules.ems.entity.ThirdpartyHourUsage;
import com.keytech.modules.ems.mapper.ThirdpartyHourUsageMapper;
import com.keytech.modules.ems.service.IAggregationCalcExecutor;
import com.keytech.modules.ems.service.IAggregationStatusService;
import com.keytech.modules.ems.service.impl.AggregationCalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采集天聚合执行器
 * @author yepeng
 * @since 2025.05.13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdpartyDayCalcExecutor implements IAggregationCalcExecutor {
    private final IAggregationStatusService aggregationStatusService;
    private final ThirdpartyHourUsageMapper thirpartyHourUsageMapper;
    private final AggregationCalcService aggregationCalcService;
    private final AggregationStatusGenerator aggregationStatusGenerator;

    @Override
    public void execute(List<AggregationStatus> statusList) {
        if (statusList.isEmpty()) {
            return;
        }

        LocalDateTime stime = statusList.get(0).getTimePoint();
        LocalDateTime etime = statusList.get(0).getTimePoint().plusHours(23);

        // 看这些点位是否有最后一个时间点采集用量
        List<String> tagIds = statusList.stream().map(AggregationStatus::getTagId).distinct().toList();
        List<ThirdpartyHourUsage> lastUsagePointList = this.thirpartyHourUsageMapper.selectList(new QueryWrapper<ThirdpartyHourUsage>()
                .select(EmsConstantsX.TAG_ID).in(EmsConstantsX.TAG_ID, tagIds).eq(EmsConstantsX.UTIME, etime));
        List<String> lastUsageTagIds = lastUsagePointList.stream().map(ThirdpartyHourUsage::getTagId).distinct().toList();

        //最终得到可以进行聚合的点位
        List<String> tagIdList = statusList.stream().map(AggregationStatus::getTagId)
                .filter(lastUsageTagIds::contains).distinct().toList();
        if (tagIdList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(statusList.stream().map(AggregationStatus::getId).toList(), "还有小时数据未完成同步");
            log.info("当前第三方天时段的聚合任务还未满足聚合条件，暂时不进行聚合");
            return;
        }

        // 批量查询该时间段所有点位天时间段SUM数据
        QueryWrapper<ThirdpartyHourUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(EmsConstantsX.TAG_ID, EmsConstantsX.SUM_USAGE).in(EmsConstantsX.TAG_ID, tagIdList).between(EmsConstantsX.UTIME, stime, etime).groupBy(EmsConstantsX.TAG_ID);
        List<ThirdpartyHourUsage> sumUsageList = this.thirpartyHourUsageMapper.selectList(queryWrapper);
        Map<String, BigDecimal> fusageMap = sumUsageList.stream().collect(Collectors.toMap(ThirdpartyHourUsage::getTagId, ThirdpartyHourUsage::getFusage));
        //取最新时间点的一个底数做为天底数
        QueryWrapper<ThirdpartyHourUsage> baseValQueryWrap = new QueryWrapper<>();
        baseValQueryWrap.select(EmsConstantsX.TAG_ID, "baseval").in(EmsConstantsX.TAG_ID, tagIdList).eq(EmsConstantsX.UTIME, etime);
        List<ThirdpartyHourUsage> baseValUsageList = this.thirpartyHourUsageMapper.selectList(baseValQueryWrap);
        Map<String, BigDecimal> baseValMap = baseValUsageList.stream().collect(Collectors.toMap(ThirdpartyHourUsage::getTagId, ThirdpartyHourUsage::getBaseval));

        // 遍历 status 按点位计算
        List<Integer> failStatusList = new ArrayList<>();
        List<Integer> succStatusList = new ArrayList<>();
        List<ThirdpartyDayUsage> dayUsageList = new ArrayList<>();
        for (AggregationStatus status : statusList) {
            try {
                BigDecimal fusage = fusageMap.get(status.getTagId());
                if (fusage == null) {
                    failStatusList.add(status.getId());
                }else {
                    succStatusList.add(status.getId());
                    ThirdpartyDayUsage aggregatUsage = ThirdpartyDayUsage.builder()
                        .tenantId(status.getTenantId())
                        .factoryCode(status.getFactoryCode())
                        .deptCode(status.getDeptCode())
                        .lineCode(status.getLineCode())
                        .tagId(status.getTagId())
                        .fusage(fusage)
                        .baseval(baseValMap.getOrDefault(status.getTagId(), BigDecimal.ZERO))
                        .editFlag(0)
                        //用量归属日期取开始时间与结束时间的中间值日期
                        .utime(stime.plusHours(12).toLocalDate())
                        .stime(stime)
                        .etime(stime.plusHours(24))
                        .createTime(LocalDateTime.now())
                        .createBy(EmsConstantsX.SYSTEM)
                        .updateBy(EmsConstantsX.SYSTEM)
                        .build();
                    dayUsageList.add(aggregatUsage);
                }
            } catch (Exception e) {
                log.error("第三方天聚合计算失败 statusId={} pointId={}", status.getId(), status.getTagId(), e);
            }
        }
        //天数据入库
        if (!dayUsageList.isEmpty()) {
            this.aggregationCalcService.batchSaveOrUpdateThirdDayUsage(dayUsageList);
        }
        //批量修改成功聚合状态
        if (!succStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateFinished(succStatusList);

            //生成ORS天聚合
            List<AggregationStatus> succAggregationStatusList = statusList.stream().filter(s -> succStatusList.contains(s.getId())).toList();
            this.aggregationStatusGenerator.generateAggregationStatusAfterThirdpartyExec(succAggregationStatusList, GrainTypeEnum.DAY);
            //生成ORS周聚合（周直接算，没满一周也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterThirdpartyExec(succAggregationStatusList, GrainTypeEnum.WEEK);
            //生成ORS月聚合（月直接算，没满一月也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterThirdpartyExec(succAggregationStatusList, GrainTypeEnum.MONTH);
            //生成ORS年聚合（年直接算，没满一年也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterThirdpartyExec(succAggregationStatusList, GrainTypeEnum.YEAR);
        }
        //批量修改错误聚合状态
        if (!failStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(failStatusList, "未找到对应天的小时数据");
        }
    }
}
