package com.keytech.modules.df.mdm.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.df.mdm.entity.DfTableFieldInfo;
import com.keytech.modules.df.mdm.mapper.DfTableFieldInfoMapper;
import com.keytech.modules.df.mdm.service.IDfTableFieldInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: DF数据库表字段信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
@Service
public class DfTableFieldInfoServiceImpl extends BaseServiceImpl<DfTableFieldInfoMapper, DfTableFieldInfo> implements IDfTableFieldInfoService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DS("ccrDg")
    @Override
    public List<Map<String, Object>> queryDfFieldList(String dfTableName) {
        String sql = " SELECT " +
                " column_name, data_type,  is_nullable,  column_default,  ordinal_position, udt_name " +
                " FROM information_schema.columns " +
                " WHERE table_schema = 'ccr_dg' " +
                " AND table_name = '"+dfTableName+"' " +
                " ORDER BY column_name ";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    @Transactional
    public void syncDfTableField(DfTableFieldInfo dfTableFieldInfo, List<Map<String, Object>> dfFieldList) {
        List<DfTableFieldInfo> list = this.lambdaQuery()
                .eq(DfTableFieldInfo::getDfTableId, dfTableFieldInfo.getDfTableId())
                .list();
        Map<String, DfTableFieldInfo> mappingMap = list.stream()
                .collect(Collectors.toMap(
                        DfTableFieldInfo::getDfFieldName,
                        item -> item,
                        (existing, replacement) -> existing
                ));
        List<DfTableFieldInfo> saveList = new ArrayList<>();
        for (Map<String, Object> dfField : dfFieldList) {
            if(!mappingMap.containsKey(dfField.get("column_name").toString())){
                DfTableFieldInfo newMapping = new DfTableFieldInfo();
                newMapping.setFactoryCode(dfTableFieldInfo.getFactoryCode());
                newMapping.setDeptCode(dfTableFieldInfo.getDeptCode());
                newMapping.setLineCode(dfTableFieldInfo.getLineCode());
                newMapping.setDfTableId(dfTableFieldInfo.getDfTableId());
                newMapping.setDfTableName(dfTableFieldInfo.getDfTableName());
                newMapping.setDfFieldName(dfField.get("column_name").toString());
                newMapping.setDfFieldType(dfField.get("udt_name").toString());
                saveList.add(newMapping);
            }
        }
        this.saveBatch(saveList);
    }
}
