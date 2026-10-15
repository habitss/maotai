package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.vo.ColumnInfoVo;

import java.util.List;

/**
 * @Description: PI-数据抽取配置字段表
 * @Author: HQ
 * @Date:   2025-08-26
 * @Version: V1.0
 */
public interface IPiExtractTableFieldService extends BaseService<PiExtractTableField> {

    List<ColumnInfoVo> getTableField(String tableName);

    List<ColumnInfoVo> getTargetTableField(PiExtractTableField piExtractTableField);
}
