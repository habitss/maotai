package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.constants.StatusFinishFlagEnum;
import com.keytech.modules.ems.entity.*;
import com.keytech.modules.ems.mapper.CollectHalfhourUsageMapper;
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
 * 采集班聚合执行器
 * 可能有小时点位也有半小时点位，需要同时查小时表和半小时表
 * @author yepeng
 * @since 2025.05.13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectClassCalcExecutor implements IAggregationCalcExecutor {
    private final IAggregationStatusService aggregationStatusService;
    private final CollectHourUsageMapper collectHourUsageMapper;
    private final CollectHalfhourUsageMapper collectHalfhourUsageMapper;
    private final ICollectPointStatusService collectPointStatusService;
    private final AggregationCalcService aggregationCalcService;
    private final AggregationStatusGenerator aggregationStatusGenerator;

    @Override
    public void execute(List<AggregationStatus> statusList) {
        if (statusList.isEmpty()) {
            return;
        }

        LocalDateTime stime = statusList.get(0).getTimePoint();
        //比如0点-7点是早班，下面也方便用between查询
        LocalDateTime etime = statusList.get(0).getTimePoint().plusHours(8);

        // 聚合计算前先看此时段有没有点位小时用量未完成采集
        List<String> tagIds = statusList.stream().map(AggregationStatus::getTagId).distinct().toList();
        List<CollectPointStatus> undonePointList = this.collectPointStatusService.list(new LambdaQueryWrapper<CollectPointStatus>()
                .select(List.of(CollectPointStatus::getTagId)).in(CollectPointStatus::getTagId, tagIds)
                .ge(CollectPointStatus::getCollectTime, stime).lt(CollectPointStatus::getCollectTime, etime).eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal()));
        List<String> undoneTagIds = undonePointList.stream().map(CollectPointStatus::getTagId).distinct().toList();

        // 再看这些点位是否有最后一个时间点采集用量
        List<CollectPointStatus> lastUsageTagList = this.collectPointStatusService.list(new QueryWrapper<CollectPointStatus>()
                .select("tag_id").in("tag_id", tagIds).eq("collect_time", etime).eq("finish_flag", StatusFinishFlagEnum.FINISHED.getVal()));
        List<String> lastUsageTagIds = lastUsageTagList.stream().map(CollectPointStatus::getTagId).distinct().toList();

        long now = System.currentTimeMillis();
        log.info("（{}）>>>>[{} ~ {}] 未完成点位={}, 最后一个点={}", now, stime, etime, undoneTagIds, lastUsageTagIds);
        //最终得到可以进行聚合的点位
        List<String> tagIdList = statusList.stream().filter(s->lastUsageTagIds.contains(s.getTagId()) && !undoneTagIds.contains(s.getTagId()))
                .map(AggregationStatus::getTagId).distinct().toList();
        log.info("（{}）>>>>[{} ~ {}] 满足班计算的点位={}", now, stime, etime, tagIdList);
        if (tagIdList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(statusList.stream().map(AggregationStatus::getId).toList(), "还有小时数据未完成采集");
            log.info("当前采集班次时段的聚合任务还未满足聚合条件，暂时不进行聚合");
            return;
        }

        // 批量查询该时间段所有点位小时数据表中班时间段SUM数据
        QueryWrapper<CollectHourUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(EmsConstantsX.TAG_ID, EmsConstantsX.SUM_USAGE).in(EmsConstantsX.TAG_ID, tagIdList).ge(EmsConstantsX.UTIME, stime).lt(EmsConstantsX.UTIME, etime).groupBy(EmsConstantsX.TAG_ID);
        List<CollectHourUsage> sumUsageList = collectHourUsageMapper.selectList(queryWrapper);
        Map<String, BigDecimal> fusageMap = sumUsageList.stream().collect(Collectors.toMap(CollectHourUsage::getTagId, CollectHourUsage::getFusage));
        //取最新时间点的一个底数做为班底数
        QueryWrapper<CollectHourUsage> baseValQueryWrap = new QueryWrapper<>();
        baseValQueryWrap.select(EmsConstantsX.TAG_ID, "baseval").in(EmsConstantsX.TAG_ID, tagIdList).eq(EmsConstantsX.UTIME, etime);
        List<CollectHourUsage> baseValUsageList = collectHourUsageMapper.selectList(baseValQueryWrap);
        Map<String, BigDecimal> baseValMap = baseValUsageList.stream().collect(Collectors.toMap(CollectHourUsage::getTagId, CollectHourUsage::getBaseval));

        // 批量查询该时间段所有点位半小时数据表中班时间段SUM数据
        QueryWrapper<CollectHalfhourUsage> queryWrapperX = new QueryWrapper<>();
        queryWrapperX.select(EmsConstantsX.TAG_ID, EmsConstantsX.SUM_USAGE).in(EmsConstantsX.TAG_ID, tagIdList).ge(EmsConstantsX.UTIME, stime).lt(EmsConstantsX.UTIME, etime).groupBy(EmsConstantsX.TAG_ID);
        List<CollectHalfhourUsage> sumUsageListX = collectHalfhourUsageMapper.selectList(queryWrapperX);
        Map<String, BigDecimal> fusageMapX = sumUsageListX.stream().collect(Collectors.toMap(CollectHalfhourUsage::getTagId, CollectHalfhourUsage::getFusage));
        fusageMap.putAll(fusageMapX);
        //取最新时间点的一个底数做为班底数
        QueryWrapper<CollectHalfhourUsage> baseValQueryWrapX = new QueryWrapper<>();
        baseValQueryWrapX.select(EmsConstantsX.TAG_ID, "baseval").in(EmsConstantsX.TAG_ID, tagIdList).eq(EmsConstantsX.UTIME, etime);
        List<CollectHalfhourUsage> baseValUsageListX = collectHalfhourUsageMapper.selectList(baseValQueryWrapX);
        Map<String, BigDecimal> baseValMapX = baseValUsageListX.stream().collect(Collectors.toMap(CollectHalfhourUsage::getTagId, CollectHalfhourUsage::getBaseval));
        baseValMap.putAll(baseValMapX);

        // 遍历 status 按点位计算
        List<Integer> failStatusList = new ArrayList<>();
        List<Integer> succStatusList = new ArrayList<>();
        List<CollectClassUsage> classUsageList = new ArrayList<>();
        for (AggregationStatus status : statusList) {
            try {
                BigDecimal fusage = fusageMap.get(status.getTagId());
                if (fusage == null) {
                    failStatusList.add(status.getId());
                } else {
                    succStatusList.add(status.getId());
                    CollectClassUsage aggregatUsage = CollectClassUsage.builder()
                            .tenantId(status.getTenantId())
                            .factoryCode(status.getFactoryCode())
                            .deptCode(status.getDeptCode())
                            .lineCode(status.getLineCode())
                            .tagId(status.getTagId())
                            .fusage(fusage)
                            .baseval(baseValMap.getOrDefault(status.getTagId(), BigDecimal.ZERO))
                            //用量归属日期取开始时间与结束时间的中间值日期
                            .utime(stime.plusHours(4).toLocalDate())
                            .stime(stime)
                            .etime(stime.plusHours(8))
                            .createTime(LocalDateTime.now())
                            .createBy(EmsConstantsX.SYSTEM)
                            .updateBy(EmsConstantsX.SYSTEM)
                            .build();
                    classUsageList.add(aggregatUsage);
                }
            } catch (Exception e) {
                log.error("采集班聚合计算失败 statusId={} pointId={}", status.getId(), status.getTagId(), e);
            }
        }
        //班数据入库
        if (!classUsageList.isEmpty()) {
            this.aggregationCalcService.batchSaveOrUpdateCollectClassUsage(classUsageList);
        }
        //批量修改成功聚合状态
        if (!succStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateFinished(succStatusList);

            //生成ORS班聚合
            List<AggregationStatus> succAggregationStatusList = statusList.stream().filter(s -> succStatusList.contains(s.getId())).toList();
            aggregationStatusGenerator.generateAggregationStatusAfterCollectExec(succAggregationStatusList, GrainTypeEnum.SHIFT);
        }
        //批量修改错误聚合状态
        if (!failStatusList.isEmpty()) {
            this.aggregationStatusService.batchUpdateError(failStatusList, "未找到对应班的小时数据");
        }
    }
}
