package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiRaUnit;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: RA-设备字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
public interface IPiRaUnitService extends BaseService<PiRaUnit> {

    /**
     * RA抽取数据
     */
    void extractData();

    /**
     * 设备字典数据抽取 sqlServer
     * @param offset
     * @param pageSize
     * @return
     */
    List<PiRaUnit> deviceDataExtract(long offset, long pageSize);


    /**
     * 获取设备
     * @return
     */
    Object getUnitByAreaId(String areaId);
}
