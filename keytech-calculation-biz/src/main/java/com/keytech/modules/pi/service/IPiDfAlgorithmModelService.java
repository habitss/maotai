package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiDfAlgorithmModel;
import com.keytech.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * @Description: DF算法-计算模型
 * @Author: jeecg-boot
 * @Date:   2026-01-21
 * @Version: V1.0
 */
public interface IPiDfAlgorithmModelService extends BaseService<PiDfAlgorithmModel> {
    /**
     * 查询所有主数据表名
     * @return 主数据表名
     */
    List<Map<String, String>> queryMdmTableName();

    /**
     * 查询主数据字段
     * @return 主数据字段
     */
    List<Map<String, String>> queryMdmTableFieldName(String tableName);

}
