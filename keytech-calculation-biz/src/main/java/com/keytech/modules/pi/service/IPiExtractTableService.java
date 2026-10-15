package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiExtractTable;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.vo.TableInfoVo;

import java.util.List;

/**
 * @Description: PI-数据抽取配置主表
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
public interface IPiExtractTableService extends BaseService<PiExtractTable> {
    /**
     * 数据抽取测试
     */
    void extractTest();

    /**
     * 获取库表信息(来源表)
     * @return
     */
    List<TableInfoVo> getTable();

    /**
     * 获取库表信息(目标表)
     * @return
     */
    List<TableInfoVo> getTargetTable();

    /**
     * 存储分配信息
     * @param piExtractTable
     * @return
     */
    void add(PiExtractTable piExtractTable);

    /**
     * 数据抽取
     */
    void extractData();
}
