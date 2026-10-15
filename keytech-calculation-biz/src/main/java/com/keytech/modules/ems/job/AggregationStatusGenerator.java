package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.entity.*;
import com.keytech.modules.ems.service.*;
import com.keytech.modules.ems.vo.TagVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 聚合任务生成器
 * @author yepeng
 * @since 2025.05.14
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AggregationStatusGenerator {
    private final IAggregationStatusService aggregationStatusService;
    private final IShiftDayPeriodSetService shiftDayPeriodSetService;
    private final IOrsdataTagService orsdataTagService;
    private final ICollectTagService collectTagService;

    /**
     * 周期小时和周半小时整点采集完成后，自动生成所有聚合任务
     * @param stime 采集时段小时/半小时起点
     * @param grainType 采集粒度 0-半小时 1-小时
     */
    public void generateAggregationStatusAfterHourCollect(LocalDateTime stime, Integer grainType) {
        String grainTypeStr = (grainType==GrainTypeEnum.HALFHOUR.getVal()?"半小时整点":"小时");
        log.info("[聚合任务生成器] 采集{}任务完成后启动聚合任务生成逻辑...[{}]", grainTypeStr, stime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 插入中间点位小时聚合
        List<OrsdataTag> orsdataTagList = orsdataTagService.list();
        List<TagVo> osrTagList = orsdataTagList.stream().map(TagVo::new).toList();
        if (!osrTagList.isEmpty()) {
            this.aggregationStatusService.setAfterCycleCollectAggregationStatusList(GrainTypeEnum.HOUR.getVal(), TagKindEnum.ORSDATA.getVal(), stime, osrTagList);
        }else {
            log.warn("[聚合任务生成器] 采集{}任务完成后的聚合任务未能找到相关联的中间点位...", grainTypeStr);
        }

        //所有小时或半小时采集点位
        List<CollectTag> emsPointList = collectTagService.getCollectTagList(grainType, null);
        List<TagVo> collectTagList = emsPointList.stream().map(TagVo::new).toList();

        //判断是否班结束
        List<ShiftDayPeriodSet> shiftDayPeriodSetList = shiftDayPeriodSetService.list(new QueryWrapper<ShiftDayPeriodSet>().select("data_period,shift,hour,period_time"));
        //找出当前采集时间小时对应的班
        ShiftDayPeriodSet shiftPeriodSet = shiftDayPeriodSetList.stream().filter(s->s.getDataPeriod() == DataPeriodEnum.SHIFT.getVal() && s.getHour() == (stime.getHour() + 1)%24).findFirst().orElse(null);
        if (shiftPeriodSet != null) {
            //实际上小时表7点已完成表示0-8点班已完成因为这里同小时的起点时间
            LocalDateTime shiftEndTime = stime.plusHours(1);
            LocalDateTime shiftStartTime = shiftEndTime.minusHours(shiftPeriodSet.getPeriodTime());

            // 插入采集点位的班聚合状态
            this.aggregationStatusService.setAfterCycleCollectAggregationStatusList(GrainTypeEnum.SHIFT.getVal(), TagKindEnum.COLLECT.getVal(), shiftStartTime, collectTagList);
        }

        //找出当前采集时间小时对应的天
        ShiftDayPeriodSet dayPeriodSet = shiftDayPeriodSetList.stream().filter(s->s.getDataPeriod() == DataPeriodEnum.DAY.getVal() && s.getHour() == (stime.getHour() + 1)%24).findFirst().orElse(null);
        if (dayPeriodSet != null) {
            LocalDateTime shiftEndTime = stime.plusHours(1);
            LocalDateTime shiftStartTime = shiftEndTime.minusHours(dayPeriodSet.getPeriodTime());

            // 插入采集点位的天聚合状态
            this.aggregationStatusService.setAfterCycleCollectAggregationStatusList(GrainTypeEnum.DAY.getVal(), TagKindEnum.COLLECT.getVal(), shiftStartTime, collectTagList);
        }

        log.info("[聚合任务生成器] 采集{}任务完成后生成聚合任务完成...", grainTypeStr);
    }

    /**
     * 重试或补采小时数据完成后，自动生成所有聚合任务
     * @param successPointList 成功采集的状态任务列表
     */
    public void generateAggregationStatusAfterRetryBackfill(List<CollectPointStatus> successPointList) {
        log.info("[聚合任务生成器] 重试或补采任务完成后启动任务生成逻辑...");
        List<String> tagIds = successPointList.stream().map(CollectPointStatus::getTagId).toList();

        // 插入相关联的中间点位小时聚合 
        List<String> baseTagIdList = tagIds.stream().map(s->"C:"+s).toList();
        List<TagVo> osrTagList = this.orsdataTagService.findRelateOrsTagByBaseTag(baseTagIdList);
        if (!osrTagList.isEmpty()) {
            this.aggregationStatusService.setBackfillOrsHourAggregationStatusList(successPointList, osrTagList);
        }else {
            log.warn("[聚合任务生成器] 重试或补采任务完成后未能找到相关联的中间点位...");
        }

        //判断当前时间是在哪个班并设置班聚合任务
        List<ShiftDayPeriodSet> shiftDayPeriodSetList = shiftDayPeriodSetService.list(new QueryWrapper<ShiftDayPeriodSet>().select("data_period,shift,hour,period_time"));
        List<ShiftDayPeriodSet> shiftPeriodSetList = shiftDayPeriodSetList.stream().filter(s->s.getDataPeriod()==DataPeriodEnum.SHIFT.getVal()).toList();
        this.aggregationStatusService.setBackfillCollectShiftAggregationStatusList(successPointList, shiftPeriodSetList);

        //判断当前时间是在哪一天并设置天聚合任务
        ShiftDayPeriodSet dayPeriodSet = shiftDayPeriodSetList.stream().filter(s->s.getDataPeriod() == DataPeriodEnum.DAY.getVal()).findFirst().orElse(null);
        if (dayPeriodSet != null) {
            this.aggregationStatusService.setBackfillCollectDayAggregationStatusList(successPointList, dayPeriodSet);
        }

        log.info("[聚合任务生成器] 重试或补采小时任务完成后生成聚合任务完成...");
    }

    /**
     * 采集班/天聚合完成后，自动生成ORS班/天/周/月聚合任务
     * @param statusList 成功聚合的状态任务列表
     * @param grainTypeEnum 聚合粒度 SHIFT-班 DAY-天 WEEK-周 MONTH-月
     */
    public void generateAggregationStatusAfterCollectExec(List<AggregationStatus> statusList, GrainTypeEnum grainTypeEnum) {
        log.info("[聚合任务生成器] 采集{}聚合任务完成后启动任务生成逻辑...", grainTypeEnum.getName());
        List<String> tagIds = statusList.stream().map(AggregationStatus::getTagId).toList();

        // 插入相关联的中间点位班聚合
        List<String> baseTagIdList = tagIds.stream().map(s->"C:"+s).toList();
        List<TagVo> osrTagList = this.orsdataTagService.findRelateOrsTagByBaseTag(baseTagIdList);
        if (!osrTagList.isEmpty()) {
            this.aggregationStatusService.setAfterCollectExecToOrsAggregationStatusList(statusList, osrTagList, grainTypeEnum);
        }else {
            log.warn("[聚合任务生成器] 采集{}聚合任务完成后未能找到相关联的中间点位...", grainTypeEnum.getName());
        }

        log.info("[聚合任务生成器] 采集{}聚合任务任务完成后生成聚合任务完成...", grainTypeEnum.getName());
    }

    /**
     * 第三方点位班/天聚合完成后，自动生成ORS班/天/周/月聚合任务
     * @param statusList 成功聚合的状态任务列表
     * @param grainTypeEnum 聚合粒度 SHIFT-班 DAY-天 WEEK-周 MONTH-月
     */
    public void generateAggregationStatusAfterThirdpartyExec(List<AggregationStatus> statusList, GrainTypeEnum grainTypeEnum) {
        log.info("[聚合任务生成器] 第三方点位{}聚合任务完成后启动任务生成逻辑...", grainTypeEnum.getName());
        List<String> tagIds = statusList.stream().map(AggregationStatus::getTagId).toList();

        // 插入相关联的中间点位班聚合
        List<String> baseTagIdList = tagIds.stream().map(s->"T:"+s).toList();
        List<TagVo> osrTagList = this.orsdataTagService.findRelateOrsTagByBaseTag(baseTagIdList);
        if (!osrTagList.isEmpty()) {
            this.aggregationStatusService.setAfterCollectExecToOrsAggregationStatusList(statusList, osrTagList, grainTypeEnum);
        }else {
            log.warn("[聚合任务生成器] 第三方点位{}聚合任务完成后未能找到相关联的中间点位...", grainTypeEnum.getName());
        }

        log.info("[聚合任务生成器] 第三方点位{}聚合任务任务完成后生成聚合任务完成...", grainTypeEnum.getName());
    }


}
