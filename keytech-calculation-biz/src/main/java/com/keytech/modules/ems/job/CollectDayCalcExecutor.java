package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.constants.StatusFinishFlagEnum;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.entity.CollectDayUsage;
import com.keytech.modules.ems.entity.CollectHourUsage;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.mapper.CollectHourUsageMapper;
import com.keytech.modules.ems.service.*;
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
public class CollectDayCalcExecutor implements IAggregationCalcExecutor {
    private final IAggregationStatusService aggregationStatusService;
    private final CollectHourUsageMapper collectHourUsageMapper;
    private final ICollectPointStatusService collectPointStatusService;
    private final AggregationCalcService aggregationCalcService;
    private final AggregationStatusGenerator aggregationStatusGenerator;

    @Override
    public void execute(List<AggregationStatus> statusList) {
        if (statusList.isEmpty()) {
            return;
        }

        LocalDateTime stime = statusList.get(0).getTimePoint();
        //比如0点-23点，下面也方便用between查询
        LocalDateTime etime = statusList.get(0).getTimePoint().plusHours(23);

        // 聚合计算前先看此时段有没有点位小时用量未完成采集
        List<String> tagIds = statusList.stream().map(AggregationStatus::getTagId).distinct().toList();
        List<CollectPointStatus> undonePointList = this.collectPointStatusService.list(new LambdaQueryWrapper<CollectPointStatus>()
                .select(List.of(CollectPointStatus::getTagId)).in(CollectPointStatus::getTagId, tagIds)
                .between(CollectPointStatus::getCollectTime, stime, etime).eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal()));
        List<String> undoneTagIds = undonePointList.stream().map(CollectPointStatus::getTagId).distinct().toList();

        // 再看这些点位是否有最后一个时间点采集用量
        List<CollectPointStatus> lastUsagePointList = this.collectPointStatusService.list(new QueryWrapper<CollectPointStatus>()
                .select("tag_id").in("tag_id", tagIds).eq("collect_time", etime).eq("finish_flag", StatusFinishFlagEnum.FINISHED.getVal()));
        List<String> lastUsageTagIds = lastUsagePointList.stream().map(CollectPointStatus::getTagId).distinct().toList();

        //最终得到可以进行聚合的点位
        List<String> tagIdList = statusList.stream().filter(s->lastUsageTagIds.contains(s.getTagId()) && !undoneTagIds.contains(s.getTagId()))
                .map(AggregationStatus::getTagId).distinct().toList();
        if (tagIdList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(statusList.stream().map(AggregationStatus::getId).toList(), "还有小时数据未完成采集");
            log.info("当前采集天时段的聚合任务还未满足聚合条件，暂时不进行聚合");
            return;
        }

        // 批量查询该时间段所有点位天时间段SUM数据
        QueryWrapper<CollectHourUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(EmsConstantsX.TAG_ID, EmsConstantsX.SUM_USAGE).in(EmsConstantsX.TAG_ID, tagIdList).between(EmsConstantsX.UTIME, stime, etime).groupBy(EmsConstantsX.TAG_ID);
        List<CollectHourUsage> sumUsageList = collectHourUsageMapper.selectList(queryWrapper);
        Map<String, BigDecimal> fusageMap = sumUsageList.stream().collect(Collectors.toMap(CollectHourUsage::getTagId, CollectHourUsage::getFusage));
        //取最新时间点的一个底数做为天底数
        QueryWrapper<CollectHourUsage> baseValQueryWrap = new QueryWrapper<>();
        baseValQueryWrap.select(EmsConstantsX.TAG_ID, "baseval").in(EmsConstantsX.TAG_ID, tagIdList).eq(EmsConstantsX.UTIME, etime);
        List<CollectHourUsage> baseValUsageList = collectHourUsageMapper.selectList(baseValQueryWrap);
        Map<String, BigDecimal> baseValMap = baseValUsageList.stream().collect(Collectors.toMap(CollectHourUsage::getTagId, CollectHourUsage::getBaseval));

        // 遍历 status 按点位计算
        List<Integer> failStatusList = new ArrayList<>();
        List<Integer> succStatusList = new ArrayList<>();
        List<CollectDayUsage> dayUsageList = new ArrayList<>();
        for (AggregationStatus status : statusList) {
            try {
                BigDecimal fusage = fusageMap.get(status.getTagId());
                if (fusage == null) {
                    failStatusList.add(status.getId());
                }else {
                    succStatusList.add(status.getId());
                    CollectDayUsage aggregatUsage = CollectDayUsage.builder()
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
                log.error("采集天聚合计算失败 statusId={} pointId={}", status.getId(), status.getTagId(), e);
            }
        }
        //天数据入库
        if (!dayUsageList.isEmpty()) {
            this.aggregationCalcService.batchSaveOrUpdateCollectDayUsage(dayUsageList);
        }
        //批量修改成功聚合状态
        if (!succStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateFinished(succStatusList);

            //生成ORS天聚合
            List<AggregationStatus> succAggregationStatusList = statusList.stream().filter(s -> succStatusList.contains(s.getId())).toList();
            this.aggregationStatusGenerator.generateAggregationStatusAfterCollectExec(succAggregationStatusList, GrainTypeEnum.DAY);
            //生成ORS周聚合（周直接算，没满一周也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterCollectExec(succAggregationStatusList, GrainTypeEnum.WEEK);
            //生成ORS月聚合（月直接算，没满一月也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterCollectExec(succAggregationStatusList, GrainTypeEnum.MONTH);
            //生成ORS年聚合（年直接算，没满一年也先计算出来）
            this.aggregationStatusGenerator.generateAggregationStatusAfterCollectExec(succAggregationStatusList, GrainTypeEnum.YEAR);
        }
        //批量修改错误聚合状态
        if (!failStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(failStatusList, "未找到对应天的小时数据");
        }
    }
}
