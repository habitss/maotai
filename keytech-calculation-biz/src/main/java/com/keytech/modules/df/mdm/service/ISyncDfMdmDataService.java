package com.keytech.modules.df.mdm.service;

import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

/**
 * MDM点位实例 接口
 * @author Rocye
 * @since 2025.12.09
 */
public interface ISyncDfMdmDataService {

    /**
     * 同步MDM点位实例数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 IND_STAND
     */
    void syncMdmIndicatorData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes);

    /**
     * 同步MDM指标标准数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCode 同步模型编码 IND_STAND
     */
    void syncMdmIndStandData(Integer tenantId, String startTime, String endTime, int size, String modelCode);

    /**
     * 同步MDM功能位置数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCode 同步模型编码 FUNLOC_BASIC_INFO2
     */
    void syncMdmFunlocData(Integer tenantId, String startTime, String endTime, int size, String modelCode);

    /**
     * 同步MDM配方数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 FAC_BASIC_INFO_RECIPE_NEW
     */
    void syncMdmRecipeData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes);

    /**
     * 同步MDM报警主数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 TAG_DICTIONARY
     */
    void syncMdmAlarmtagData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes);

    /**
     * 同步MDM设备数据
     */
    ReturnT<String> syncMdmDevice(Integer tenantId);

    /**
     * 重新转换报警点位的 factoryCode、deptCode、lineCode 和 deviceId
     */
    void refreshAlarmPointMapping();

}
