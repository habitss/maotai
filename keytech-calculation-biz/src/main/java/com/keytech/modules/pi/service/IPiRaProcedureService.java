package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiRaProcedure;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: RA-配方
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
public interface IPiRaProcedureService extends BaseService<PiRaProcedure> {

    /**
     * 配方数据抽取
     */
    void recipeExtract();

    /**
     * 配方数据抽取
     * @param offset
     * @param pageSize
     * @return
     */
    List<PiRaProcedure> recipeDataExtract(long offset, long pageSize);
}
