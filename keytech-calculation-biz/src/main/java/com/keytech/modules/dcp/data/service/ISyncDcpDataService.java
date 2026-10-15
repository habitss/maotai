package com.keytech.modules.dcp.data.service;

import java.util.List;
import java.util.Map;

/**
 * 同步DCP视图数据 接口
 */
public interface ISyncDcpDataService {

    /**
     * 同步DCP视图数据到本地日志表
     * @param tenantId 租户ID
     * @param sDate 开始时间
     * @param eDate 结束时间
     */
    void syncDcpData(Integer tenantId, String sDate, String eDate);

    /**
     * 查询DCP数据
     * @param tableName 表名
     * @param timeField 时间字段字段
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param indicatorField 实例化指标字段
     * @param indicator 实例化指标
     */
    List<Map<String, Object>> queryDcpData(String tableName, String timeField,
                                           String factoryField, String factoryCodes,
                                           String startDate, String endDate,
                                           String indicatorField, String indicator);

}
