package com.keytech.modules.df.mdm.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.df.mdm.entity.DfTableInfo;

/**
 * @Description: DF数据库表基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
public interface IDfTableInfoService extends BaseService<DfTableInfo> {
    /**
     * 新增基础表信息
     * @param dfTableInfo 基础表信息
     */
    void saveData(DfTableInfo dfTableInfo);
}
