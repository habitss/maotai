package com.keytech.modules.pi.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.service.BaseService;

import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.entity.PiCalculateSignal;

import java.util.List;

/**
 * <p>
 * pi-计算信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 22:34:22
 */
public interface IPiCalculateSignalService extends BaseService<PiCalculateSignal> {

    /**
     * 根据picode集合，查询pi对应的所有计算信号对象
     * @param tableName pi存储表名
     * @param piCodes pi code集合
     * @return
     */
    List<PiCalculateSignal> queryListByPiCodes(String tableName,List<String> piCodes);


    /**
     * 获取算法数据
     * @param piCalculateSignal
     * @return
     */
    AlgorithmDataDto getAlgorithmData(PiCalculateSignal piCalculateSignal);

    /**
     * 算法批量删除
     * @param asList
     */
    void deleteDataBatch(List<String> asList);

    /**
     * 添加信息
     * @param pageList
     * @return
     */
    IPage<PiCalculateSignal> addInformation(IPage<PiCalculateSignal> pageList);
    /**
     * 判断pi是否是PI间算法计算
     * @param tableName
     * @param fieldName
     */
    boolean isPiOtherCalculation(String tableName, String fieldName);
}
