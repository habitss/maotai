package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.common.DataBaseTypeUtil;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.mapper.PiExtractTableFieldMapper;
import com.keytech.modules.pi.service.IPiExtractTableFieldService;
import com.keytech.modules.pi.vo.ColumnInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: PI-数据抽取配置字段表
 * @Author: HQ
 * @Date:   2025-08-26
 * @Version: V1.0
 */
@Service
public class PiExtractTableFieldServiceImpl extends BaseServiceImpl<PiExtractTableFieldMapper, PiExtractTableField> implements IPiExtractTableFieldService {

    @Autowired
    private PiExtractTableFieldMapper piExtractTableFieldMapper;


    /**
     * 获取指定数据源和表的字段信息列表
     *
     * @param tableName 表名称
     * @return 表字段信息列表，包含每个字段的详细信息
     */
    @Override
    public List<ColumnInfoVo> getTableField(String tableName) {
        List<ColumnInfoVo> list = null;
        try {
            list = piExtractTableFieldMapper.getTableField(tableName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * 获取目标表字段信息
     * @return 目标表字段信息
     */
    @Override
    public List<ColumnInfoVo> getTargetTableField(PiExtractTableField piExtractTableField) {
        // 获取数据库名称
        piExtractTableField.setDbName(DataBaseTypeUtil.getDbName());
        List<ColumnInfoVo> list = piExtractTableFieldMapper.getTargetTableField(piExtractTableField);
        for (ColumnInfoVo columnInfoVO : list) {
            if (StrUtil.isBlank(columnInfoVO.getColumnComment())){
                columnInfoVO.setColumnComment(columnInfoVO.getColumnName());
            }
        }
        return list;
    }
}
