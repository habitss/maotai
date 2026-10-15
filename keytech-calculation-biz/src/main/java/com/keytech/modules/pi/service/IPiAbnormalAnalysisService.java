package com.keytech.modules.pi.service;

import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * @Description: PI异常分析
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
public interface IPiAbnormalAnalysisService extends BaseService<PiAbnormalAnalysis> {

    /**
     * 查询单个PI的异常分析记录
     * @param piAbnormalAnalysis
     * @return
     */
    PiAbnormalAnalysis queryAbnormalAnalysis(PiAbnormalAnalysis piAbnormalAnalysis);

    /**
     * 获取过程记录中每个点位的值
     * @param tableCode 数据表code
     * @param dataId 数据id
     * @param fieldCode 字段code
     */
    Map<String,List<MeasurementData>> getBasePointDataList(String tableCode, String dataId, String fieldCode,Integer dataPage,Integer dataOffSet);

    /**
     * 获取对应PI的标准以及描述
     * @param tableCode 基础数据表
     * @param dataId 数据id
     * @param fieldCode 字段代码
     * @param dataValue 字段值
     * @return standardMaintenance标准，maintenanceDescribe标准描述
     */
    Map getPiStandardData(String tableCode, String dataId, String fieldCode, String dataValue);

    /**
     * 获取PI的数据计算触发时间
     * @param dataId
     * @param tableCode
     * @param fieldCode
     * @return field\time\calculateSignalId
     */
    Map getTriggerTimeForPi(String dataId,String tableCode,String fieldCode);
}
