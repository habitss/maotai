package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.pi.dto.AdapterAlgorithmDataDto;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.entity.PiAdapterAlgorithm;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: Adapter-算法配置
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
public interface IPiAdapterAlgorithmService extends BaseService<PiAdapterAlgorithm> {

    /**
     * 添加算法
     * @param adapterAlgorithmDataDto
     */
    void addAlgorithm(AdapterAlgorithmDataDto adapterAlgorithmDataDto);

    /**
     * 获取算法数据
     * @param piAdapterAlgorithm
     * @return
     */
    AdapterAlgorithmDataDto adapterData(PiAdapterAlgorithm piAdapterAlgorithm);

    /**
     * Adapter计算
     * @param list
     */
    void calculate(List<PiSistarRtSteps> list, List<PiAdapterAlgorithm> listAlgorithm);
    /**
     * 添加信息
     * @param pageList
     * @return
     */
    IPage<PiAdapterAlgorithm> addInformation(IPage<PiAdapterAlgorithm> pageList);

    /**
     * 重算(页面)
     * @param
     */
    public void againPiCalculate(String tableName, List<String> listPI, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 重算(定时任务)
     * @param parameterObject
     */
    void taskRecalculate(String parameterObject);

    /**
     * 计算 RA
     * @param targetList
     * @param listAlgorithm
     */
    void calculateRa(List<PiRaStep> targetList, List<PiAdapterAlgorithm> listAlgorithm);

    /**
     * 重算RA(手动)
     * @param tableName
     * @param fieldList
     * @param startDateTime
     * @param endDateTime
     */
    void againPiCalculateRa(String tableName, List<String> fieldList, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 重算RA(定时任务)
     * @param parameterObject
     */
    void taskRecalculateRa(String parameterObject);

    /**
     * 删除数据
     * @param asList
     */
    void deleteDataBatch(List<String> asList);

    /**
     * 复制算法
     * @param piCustomizeTableField
     */
    void copyAdapterRaDb(PiCustomizeTableField piCustomizeTableField);
}
