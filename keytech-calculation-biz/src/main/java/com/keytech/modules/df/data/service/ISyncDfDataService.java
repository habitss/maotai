package com.keytech.modules.df.data.service;

/**
 * 同步DF视图数据 接口
 * @author yepeng
 * @since 2025.12.12
 */
public interface ISyncDfDataService {

    /**
     * 同步DF视图数据到本地日志表
     * @param tenantId 租户ID
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @since 2025.12.12
     */
    void syncDfDataToLog(Integer tenantId, String startDate, String endDate);

}
