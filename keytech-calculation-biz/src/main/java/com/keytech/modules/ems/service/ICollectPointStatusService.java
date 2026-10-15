package com.keytech.modules.ems.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.keytech.base.service.BaseService;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.CollectTag;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 点位采集状态表
 * @author yepeng
 * @since 2025.04.29
 * */
public interface ICollectPointStatusService extends BaseService<CollectPointStatus> {

    /**
     * 查询指定点位最近的一条完成记录
     * @param tagIds 点位编码列表
     * @param collectTime 指定采集时间
     */
    List<CollectPointStatus> selectLastHadBaseValRecordsByPoints(List<String> tagIds, LocalDateTime collectTime);

    /**
     * 查询指定条件的点位采集状态同时查出FluxCode
     * @param queryWrapper 查询条件
     */
    List<CollectPointStatus> selectFluxList(Wrapper<CollectPointStatus> queryWrapper);

    /**
     * 查询未完成的周期性任务(半小时/小时)
     * @param grainType 0-半小时 1-小时
     */
    List<CollectPointStatus> queryPendingForPeriodic(Integer grainType);

    /**
     * 查询自动重试未完成任务(半小时/小时)
     * @param maxRetry 最大重试次数
     * @param grainType 0-半小时 1-小时
     * @param minusHours 指定起始时间为当前时间前几小时
     */
    List<CollectPointStatus> queryPendingForRetry(int maxRetry, Integer grainType, Integer minusHours);

    /**
     * 查询人工补采未设置参考值的记录
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param tagIds 点位编码列表
     */
    List<CollectPointStatus> queryNotLastBaseValBackfill(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<String> tagIds);

    /**
     * 查询人工补采逻辑任务(半小时/小时)
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param tagIds 点位编码列表
     */
    List<CollectPointStatus> queryPendingForBackfill(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<String> tagIds);

    /**
     * 查询指定采集粒度指定时间段指定点位需要补采的状态集合
     * 自动过滤掉数据库中存在的记录
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param emsPointList 点位列表
     */
    List<CollectPointStatus> getBackfillPointStatusList(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<CollectTag> emsPointList);

    /**
     * 获取需要采集的点位配置
     * 同时处理未配置FluxCode的点位，生成预警记录
     * @param grainType 时间粒度
     * @return 有效点位列表
     */
    List<CollectTag> getCollectPoints(int grainType);

}
