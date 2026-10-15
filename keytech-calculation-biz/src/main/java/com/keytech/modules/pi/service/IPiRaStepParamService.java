package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiRaStepParam;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: RA-步骤参数
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
public interface IPiRaStepParamService extends BaseService<PiRaStepParam> {

    /**
     * 步骤参数数据抽取
     */
    void extractData();
    /**
     * 步骤参数数据分页查询
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param batchSize
     * @return
     */
    List<PiRaStepParam> stepParamDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, int batchSize);

    /**
     * 根据条件查询
     * @param dataList
     * @return
     */
    List<PiRaStepParam> selectByConditions(List<PiRaStep> dataList);

    /**
     * 根据时间(监控)条件查询
     * @param dataList
     * @return
     */
    List<PiRaStepParam> selectByTimeConditions(List<PiRaStep> dataList);
}
