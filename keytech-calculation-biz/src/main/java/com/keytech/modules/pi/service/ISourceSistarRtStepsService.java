package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter-步骤
 * @Author: HQ
 * @Date:   2025-08-28
 * @Version: V1.0
 */
public interface ISourceSistarRtStepsService extends BaseService<SourceSistarRtSteps> {

    /**
     * 分页查询增量数据
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    List<SourceSistarRtSteps> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);

    /**
     * 获取字典-设备
     * @return
     */
    List<PiSistarEngReccats> getReccats();
    /**
     * 删除数据
     * @param tableName
     */
    void deleteData(String tableName);
    /**
     * 获取字典-PCU
     * @return
     */
    List<PiSistarEngPcus> getPcus();

    /**
     * 获取字典-设备
     * @return
     */
    List<PiSistarEngUnits> getUnits();

    /**
     * 获取字典-步骤
     * @return
     */
    List<PiSistarEngEops> getEops();

    /**
     * 获取字典-配方
     * @return
     */
    List<PiSistarEngRecipes> getRecipes();
}
