package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.common.PiStandardSymboEnum;
import com.keytech.modules.common.DataBaseTypeUtil;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiCustomizeTableMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.BoxPlotCalculatorUtil;
import com.keytech.modules.pi.util.CustomizeTableUtil;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: pi自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
public class PiCustomizeTableServiceImpl extends BaseServiceImpl<PiCustomizeTableMapper, PiCustomizeTable> implements IPiCustomizeTableService {

    //租户
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Resource
    private PiCustomizeTableMapper piCustomizeTableMapper;
    @Resource
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;
    @Resource
    private IPiCalculateSignalService iPiCalculateSignalService;
    @Resource
    private PiStandardManageServiceImpl piStandardManageService;
    @Resource
    private PiStandardMaintenanceServiceImpl piStandardMaintenanceService;
    @Resource
    private IPiAdapterAlgorithmService iPiAdapterAlgorithmService;
    @Resource
    private IPiStandardDataService piStandardDataService;
    @Resource
    private IPiAllocationTableFieldService iPiAllocationTableFieldService;
    @Resource
    private IPiDfAlgorithmBaseinfoService piDfAlgorithmBaseinfoService;

    /**
     * 创建表,同时将记录存储报表主表
     * 1.查询mes3_basedata_表名数量
     * 2.生成mes3_basedata_N表名
     * 3.创建表同时写入自定义报表
     *
     * @param piCustomizeTable
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveTable(PiCustomizeTable piCustomizeTable) {
        if(StrUtil.isNotBlank(piCustomizeTable.getName())){
            // 获取数据库名称
            String dbName = DataBaseTypeUtil.getDbName();
            //String databaseId = DataBaseTypeUtil.getDbType().getDb();
            //表名自动生成(mes3_basedata_001)
            //查询库中mes3_basedata_的数量
            int num = piCustomizeTableMapper.getBasedataTableNum(dbName);
            //存在的数量+1,生成新表名
            String tableNameNew = String.format("mes3_basedata_%03d", (num+1));
            piCustomizeTable.setTableName(tableNameNew);
            //建表
            //防止sql注入
            //SqlInjectionUtil.filterContent(piCustomizeTable.getTableName());

            if(DbType.POSTGRE_SQL == DataBaseTypeUtil.getDbType()){
                //建表
                piCustomizeTableMapper.createNewTablePg(piCustomizeTable);
                //更新字段属性
                piCustomizeTableMapper.updateTableCommentsPg(piCustomizeTable);
            }else{
                piCustomizeTableMapper.createNewTable(piCustomizeTable);
            }

            //存储
//            piCustomizeTable.setViewName(viewName);
            piCustomizeTable.setSubtable(tableNameNew);
            save(piCustomizeTable);
            return Result.OK("保存成功!");
        }else{
            return Result.error("报表名称为空");
        }
    }


    /**
     * 获取指定报表数据
     * select {fieldList} from {table} where {conditionList}
     * 返回
     * List集合
     * @param tableQueryDto
     * @return
     */
    @Override
    public List<Map<String, Object>> getTableData(TableQueryDto tableQueryDto) {
//        List<Map<String, Object>> list = new ArrayList<>();

        String tableName = tableQueryDto.getTable();
        //判断查询表还是视图
        String viewName = tableOrView(tableName);
        if(StrUtil.isNotBlank(viewName)){
            tableName = viewName;
        }

        //获取字段列
        List<String> fieldList = tableQueryDto.getFieldList();
        String field = String.join(",", fieldList);
        //获取查询条件
        List<TableConditionDto> conditionList = tableQueryDto.getConditionList();
        //字符串"null"转为null，防止date字段拼成='null'导致报错
        conditionList.forEach(dto -> {
            if ("null".equals(dto.getVal())) {
                dto.setVal(null);
            }
        });
        //字符拼接'' 数字不拼接, null用IS NULL
        String condition = conditionList.stream()
                .map(obj -> {
                    if (obj.getVal() == null) {
                        return obj.getKey() + " IS NULL";
                    }
                    if (DataBaseTypeUtil.getDbType() == DbType.MYSQL && NumberUtil.isNumber(obj.getVal())) {
                        return obj.getKey() + " = " + obj.getVal();
                    }
                    return obj.getKey() + " = '" + obj.getVal() + "'";
                })
                .collect(Collectors.joining(" and "));

        //验证
//        SqlInjectionUtil.filterContent(field);
//        SqlInjectionUtil.filterContent(tableQueryDto.getTable());
//        SqlInjectionUtil.filterContent(condition);
        //TenantContext.setTenant("1006"); //租户id
        //获取数据
        return piCustomizeTableMapper.getTableData(field,tableName,condition,"",-1);
    }

    /**
     * 报表数据写入
     * INSERT INTO {table}(field1,field2) VALUES (fieldVal1,fieldVal2);
     * insert
     * @param tableSaveDto
     * @return 返回id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String tableSave(TableSaveDto tableSaveDto) {
        try {
            //当前数据id
            String id = "";

            //获取设置值字段集合
            //List<PiCustomizeTableField> setValueFieldList = getTableFieldList(tableSaveDto.getTable() ,PiCommonConstants.ONE_STR);

            //获取字段
            List<PiCustomizeTableField> tableFieldList = getTableFieldList(tableSaveDto.getTable() ,null);
            //遍历 setValueFieldList 获取字段名key 字段类型val map
            Map<String, String> fieldTypeMap = tableFieldList.stream()
                    .collect(Collectors.toMap(PiCustomizeTableField::getFieldName, PiCustomizeTableField::getType));
            //筛选tableFieldList 启用设定值集合
            List<PiCustomizeTableField> setValueFieldList = tableFieldList.stream()
                    .filter(field -> PiCommonConstants.ONE_STR.equals(field.getIsValueEnabled()))
                    .collect(Collectors.toList());

            //存储最新设定值集合(值有更新)
            List<PiCustomizeTableField> setValueUpdateList = new ArrayList<>();

            //字段值集合
            List<TableFieldValueDto> fieldList = tableSaveDto.getFieldList();
            //空字符串统一转为null，确保数据库存NULL而非''
            fieldList.forEach(dto -> {
                if (dto.getFieldVal() != null && dto.getFieldVal().isEmpty()) {
                    dto.setFieldVal(null);
                }
            });

            //获取约束条件
            List<TableConditionDto> conditionList = tableSaveDto.getConditionList();
            //字符串"null"转为null，防止date字段拼成='null'导致报错
            conditionList.forEach(dto -> {
                if ("null".equals(dto.getVal())) {
                    dto.setVal(null);
                }
            });
            //约束条件值不为空,有一条为空,不对数据库表进行操作
            if(null != conditionList && conditionList.size() > 0){
                //判断每个值是否为空
                if(conditionList.stream().anyMatch(dto -> dto.getVal() == null || dto.getVal().isEmpty())){
                    //约束条件存在空
                    return "-1";
                }
            }else{
                //约束条件为空
                return "-1";
            }

            // 约束条件转化成 key1=val1 and key2=val2 (数字不拼接'')
            StringJoiner sj = new StringJoiner(" and ");
            conditionList.stream()
                    //.map(dto -> dto.getKey() + " = '" + dto.getVal()+"' ")
//                    .map(dto -> dto.getKey() + " = " + (dto.getVal() == null? null : "'" + dto.getVal() + "' "))
                    .map(dto -> {
                        Object val = dto.getVal();
                        if (DataBaseTypeUtil.getDbType() == DbType.MYSQL) {
                            //判断是否是数字 且 类型为数字
                            if (val!= null && NumberUtil.isNumber(val.toString())
                             && PiCommonConstants.TWO_STR.equals(fieldTypeMap.getOrDefault(dto.getKey(),""))) {
                                return dto.getKey() + " = " + val;
                            }
                        } else if (DataBaseTypeUtil.getDbType() == DbType.POSTGRE_SQL) {
                            return dto.getKey() + " = '" + val+"' ";
                        }
                        return dto.getKey() + " = " + (val == null? null : "'" + val + "'");
                    })
                    .forEach(sj::add);

            //约束条件
            String condition = sj.toString();
            //表名
            String table = tableSaveDto.getTable();
            String tableName = table;
            //获取主表
            LambdaQueryWrapper<PiCustomizeTable> queryWrapper = new LambdaQueryWrapper<PiCustomizeTable>();
            queryWrapper.eq(PiCustomizeTable::getTableName,table);
            List<PiCustomizeTable> tableList = list(queryWrapper);
            PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
            if(tableList != null && !tableList.isEmpty()){
                piCustomizeTable = tableList.get(0);
            }
            boolean isView = false;
            //主表子表集合
            List<String> subtableList = new ArrayList<>();
            //表名,值/字段
            Map<String, Object> subtableMap = new HashMap<>();
            //判断表还是视图
            if(StrUtil.isNotBlank(piCustomizeTable.getViewName())){
                //视图
                table = piCustomizeTable.getViewName();
                isView = true;
                String subtable = piCustomizeTable.getSubtable();
                if(StrUtil.isNotBlank(subtable)){
//                  subtableList = Arrays.asList(subtable.split(","));
                    subtableMap = Arrays.stream(subtable.split(","))
                        .collect(Collectors.toMap(
                                key -> key, // Key: 拆分后的每个字符串（如 "table1"）
                                value -> "" // Value: 固定空字符串 ""
                        ));
                }
            }
            //验证sql注入
            //SqlInjectionUtil.filterContent(table);
            //SqlInjectionUtil.filterContent(condition);

            //更新修改标记 0新增 1修改
            String mark = "0";
            //判断新增还是修改(conditionStr为空默认新增)
            if(StrUtil.isNotBlank(condition)){
                //添加租户id
                condition+=" AND tenant_id = "+tenantId+" ";
                TenantContext.setTenant(tenantId+"");
                //判断新增修改
                List<Map<String, Object>> list = piCustomizeTableMapper.getTableData("id",table,condition,"",-1);
                if(null != list && list.size() > 0){
                    id = list.get(0).get("id").toString();
                    //存在数据做修改操作
                    mark = "1";
                    //id = list.get(0).get("id")+"";
                }
            }

            if("1".equals(mark)){
                //修改
                //设定值 处理
                //修改数据集合转map 字段-->对象
                Map<String, TableFieldValueDto> fieldMap = fieldList.stream().collect(Collectors.toMap(TableFieldValueDto::getField, dto -> dto));
                for (PiCustomizeTableField dto : setValueFieldList) {
                    //判断fieldMap 有无设定值
                    if(fieldMap.containsKey(dto.getFieldName())){
                        TableFieldValueDto tableFieldValueDto = fieldMap.get(dto.getFieldName());
                        //判断与设定值是否相同
                        if(tableFieldValueDto != null && StrUtil.isNotBlank(tableFieldValueDto.getFieldVal())
                                && !tableFieldValueDto.getFieldVal().equals(dto.getSetValue())){
                            //不同 更新设定值
                            dto.setSetValue(tableFieldValueDto.getFieldVal());
                            setValueUpdateList.add(dto);
                        }
                    }
                }
                if(!isView) {
                    //表更新
                    //字段值集合 (field = 'fieldVal', field = 'fieldVal') \ 处理成\\
                    String fieldValStr = fieldList.stream()
                            .map(dto -> dto.getField() + " = '" + CustomizeTableUtil.escapeSqlValue(dto.getFieldVal()) + "'")
                            .collect(Collectors.joining(", "));
                    //'null'替换成null
                    fieldValStr = fieldValStr.replaceAll("'null'", "null");
                    //验证sql注入
                    //SqlInjectionUtil.filterContent(fieldValStr);
                    //更新-更新时间
                    fieldValStr += " ,update_time =now() ";
                    piCustomizeTableMapper.tableUpdate(fieldValStr, table, condition);
                }else{
                    //多表更新
                    //字段表名对照(字段 ==> 表)
                    Map<String, String> map = tableCompare(tableName);

                    //将要更新的字段和值归类到指定表中(表-->字段A=值,字段B=值)
                    for (TableFieldValueDto dto : fieldList) {
                        String tn =  map.get(dto.getField());
                        if(tn == null){
                            tn = tableName;
                        }
                        String val = subtableMap.get(tn)+"";
                        // 值\ 处理成\\
                        String fieldValStr = dto.getField() + " = '"+ CustomizeTableUtil.escapeSqlValue(dto.getFieldVal()) + "'";
                        fieldValStr = fieldValStr.replaceAll("'null'","null");
                        if(StrUtil.isNotBlank(val)){
                            val = val + " , " +fieldValStr;
                        }else{
                            boolean isMatch = tn.matches("mes3_basedata_\\d+");
                            if(isMatch){
                                val = " update_time =now() ,";
                            }
                            val = val + fieldValStr;
                        }
                        subtableMap.put(tn, val);
                    }
                    //遍历表和字段值对照,同时给子表main_id赋值,执行更新
                    for (String tn : subtableMap.keySet()) {
                        boolean isMatch = tn.matches("mes3_basedata_\\d+");
                        if(isMatch){
                            condition = " id = '"+id+"' ";
                        }else{
                            Pattern pattern = Pattern.compile(".*_(\\d+)$");  // 匹配最后一个 _ 后的数字
                            Matcher matcher = pattern.matcher(tn);
                            if (matcher.find()) {
                                String lastNumber = matcher.group(1);
                                condition = " main_id_"+lastNumber+" = '"+id+"' ";
                            }
                        }
                        if(StrUtil.isNotBlank(subtableMap.get(tn)+"")){
                            piCustomizeTableMapper.tableUpdate(subtableMap.get(tn)+"",tn,condition);
                        }
                    }
                }
            }else {
                //新增
                Map<String, Long> mainIdMap = new HashMap<>();

                //设定值 遍历fieldList 找到设定值启用
                //新增数据集合转map 字段-->对象
                Map<String, TableFieldValueDto> fieldMap = fieldList.stream()
                        .collect(Collectors.toMap(TableFieldValueDto::getField, dto -> dto));
                //遍历设定值
                for (PiCustomizeTableField dto : setValueFieldList) {
                    //判断fieldMap 有无设定值
                    if (fieldMap.containsKey(dto.getFieldName())) {
                        TableFieldValueDto tableFieldValueDto = fieldMap.get(dto.getFieldName());
                        //判断与设定值是否相同
                        if (tableFieldValueDto != null && StrUtil.isNotBlank(tableFieldValueDto.getFieldVal()) &&
                                !tableFieldValueDto.getFieldVal().equals(dto.getSetValue())) {
                            //不同 更新设定值
                            dto.setSetValue(tableFieldValueDto.getFieldVal());
                            setValueUpdateList.add(dto);
                        }
                    } else {
                        //新增的数据没有设定值,将设定值添加到新增数据中
                        TableFieldValueDto setValueDto = new TableFieldValueDto();
                        setValueDto.setField(dto.getFieldName());
                        setValueDto.setFieldVal(dto.getSetValue());
                        fieldList.add(setValueDto);
                    }
                }
                if(!isView) {
                    //单表
                    //字段集合
                    List<String> fields = new ArrayList<>();
                    Map<String, String> existsFieldMap = new HashMap<>();
                    //值
                    StringBuffer fieldValBuff = new StringBuffer();
                    //获取字段和值的集合
                    for (TableFieldValueDto dto : fieldList) {
                        fields.add(dto.getField());
                        existsFieldMap.put(dto.getField(), "");
                        if (fieldValBuff.length() > 0) {
                            if (dto.getFieldVal() == null) {
                                fieldValBuff.append(" , ").append(" ").append(dto.getFieldVal()).append(" ");
                            } else {
                                fieldValBuff.append(" , ").append(" '").append(CustomizeTableUtil.escapeSqlValue(dto.getFieldVal())).append("' ");
                            }
                        } else {
                            if (dto.getFieldVal() == null) {
                                fieldValBuff.append(" ").append(dto.getFieldVal()).append(" ");
                            } else {
                                fieldValBuff.append(" '").append(CustomizeTableUtil.escapeSqlValue(dto.getFieldVal())).append("' ");
                            }

                        }
                    }
                    //约束条件也需要写入
                    for (TableConditionDto dto : conditionList) {
                        if(existsFieldMap.containsKey(dto.getKey())){
                            continue;
                        }
                        fields.add(dto.getKey());
                        if (fieldValBuff.length() > 0) {
                            fieldValBuff.append(" , ").append(" '").append(dto.getVal()).append("' ");
                        } else {
                            fieldValBuff.append(" '").append(dto.getVal()).append("' ");
                        }
                    }
                    //添加租户 ID
                    if (fieldValBuff.length() > 0) {
                        fieldValBuff.append(",'" + tenantId + "'");
                    }
                    String fieldStr = "";
                    if (null != fields && fields.size() > 0) {
                        //字段 field1,field2,field3
                        //添加租户 ID
                        fields.add("tenant_id");
                        fieldStr = String.join(",", fields);
                    }
                    //值 'fieldVal1','fieldVal2','fieldVal3'
                    String fieldValStr = fieldValBuff.toString();

                    if (StrUtil.isNotBlank(fieldStr) && StrUtil.isNotBlank(fieldValStr)) {
                        //验证sql注入
                        //SqlInjectionUtil.filterContent(fieldStr);
                        //SqlInjectionUtil.filterContent(fieldValStr);
                        //添加 新增时间/修改时间
                        if (fieldStr.contains("create_time")) {
                            fieldStr += ",update_time";
                            fieldValStr += ",now()";
                        } else {
                            fieldStr += ",create_time,update_time";
                            fieldValStr += ",now(),now()";
                        }
                        piCustomizeTableMapper.tableSave(fieldStr, fieldValStr, table, mainIdMap);
                        id = String.valueOf(mainIdMap.get("id"));
                    }
                }else{
                    //多表新增
                    //字段表名对照字段 ==> 表
                    Map<String, String> map = tableCompare(tableName);
                    //约束条件
                    for (TableConditionDto dto : conditionList) {
                        TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                        tableFieldValueDto.setField(dto.getKey());
                        tableFieldValueDto.setFieldVal(dto.getVal());
                        fieldList.add(tableFieldValueDto);
                    }
                    //遍历字段
                    for (TableFieldValueDto dto : fieldList) {
                        //字段集合
                        List<String> fields = new ArrayList<>();
                        //值
                        StringBuffer fieldValBuff = new StringBuffer();
                        //字段对应表
                        String tn =  map.get(dto.getField());
                        if(tn == null){
                            tn = tableName;
                        }
                        //表写入内容
                        // subtableMap 表-->字段({filed 字段,val 值})
                        Map<String,String> fieldValMap = null;
                        if(subtableMap.get(tn) instanceof String){
                            fieldValMap = new HashMap<>();
                            subtableMap.put(tn, subtableMap);
                        }else{
                            fieldValMap = (Map)subtableMap.get(tn);
                        }
                        //获取字段,值集合
                        fields.add(dto.getField());
                        if (fieldValBuff.length() > 0) {
                            if (dto.getFieldVal() == null) {
                                fieldValBuff.append(" , ").append(" ").append(dto.getFieldVal()).append(" ");
                            } else {
                                fieldValBuff.append(" , ").append(" '").append(CustomizeTableUtil.escapeSqlValue(dto.getFieldVal())).append("' ");
                            }
                        } else {
                            if (dto.getFieldVal() == null) {
                                fieldValBuff.append(" ").append(dto.getFieldVal()).append(" ");
                            } else {
                                fieldValBuff.append(" '").append(CustomizeTableUtil.escapeSqlValue(dto.getFieldVal())).append("' ");
                            }
                        }

                        String fieldValStr = fieldValBuff.toString();
                        String fieldTmp = fieldValMap.getOrDefault("field","");
                        String fieldValTmp = fieldValMap.getOrDefault("val","");

                        fieldValMap.put("field", fieldTmp + (fieldTmp.isEmpty() ? "" : ",") + String.join(",", fields));
                        fieldValMap.put("val",  fieldValTmp + (fieldValTmp.isEmpty() ? "" : ",")  + fieldValStr);
                        subtableMap.put(tn, fieldValMap);

                    }

                    //先主表数据写入获取id
                    Map<String,String> fieldValMap = (Map) subtableMap.get(tableName);
                    if(fieldValMap != null){
                        String fieldStr = fieldValMap.get("field");
                        String fieldValStr = fieldValMap.get("val");

                        if (fieldStr.contains("create_time")) {
                            fieldStr += ",tenant_id,update_time";
                            fieldValStr += ","+tenantId+",now()";
                        } else {
                            fieldStr += ",tenant_id,create_time,update_time";
                            fieldValStr += ","+tenantId+",now(),now()";
                        }
                        piCustomizeTableMapper.tableSave(fieldStr, fieldValStr, table, mainIdMap);
                        id = String.valueOf(mainIdMap.get("id"));
                    }

                    for (String tn : subtableMap.keySet()) {
                        //排除主表
                        if(!tableName.equals(tn)){
                            if(subtableMap.get(tn) instanceof String){
                                fieldValMap = new HashMap<>();
                                subtableMap.put(tn, subtableMap);
                            }else{
                                fieldValMap = (Map)subtableMap.get(tn);
                            }

                            //获取报表序号
                            Pattern pattern = Pattern.compile(".*_(\\d+)$");  // 匹配最后一个 _ 后的数字
                            Matcher matcher = pattern.matcher(tn);
                            String lastNumber = "";
                            if (matcher.find()) {
                                lastNumber = matcher.group(1);
                            }

                            String fieldStr = fieldValMap.get("field");
                            String fieldValStr = fieldValMap.get("val");
                            if(StrUtil.isNotBlank(fieldStr)){
                                fieldStr+= " ,main_id_"+lastNumber;
                            }else{
                                fieldStr = " main_id_"+lastNumber;
                            }
                            if(StrUtil.isNotBlank(fieldValStr)){
                                fieldValStr += " ,'"+id+"' ";
                            }else{
                                fieldValStr = " '"+id+"' ";
                            }

                            piCustomizeTableMapper.tableSave(fieldStr, fieldValStr, tn, mainIdMap);
                            //id = String.valueOf(mainIdMap.get("id"));
                        }
                    }
                }
            }
            //更新 设定值
            if(setValueUpdateList != null && !setValueUpdateList.isEmpty()){
                //更新
                iPiCustomizeTableFieldService.updateBatchById(setValueUpdateList);
                //更新分配 - 使用UpdateWrapper只更新setValue字段，避免IGNORED策略导致其他字段被清空
                for (PiCustomizeTableField piCustomizeTableField : setValueUpdateList) {
                    LambdaUpdateWrapper<PiAllocationTableField> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(PiAllocationTableField::getTableName, piCustomizeTableField.getTableName())
                                  .set(PiAllocationTableField::getSetValue, piCustomizeTableField.getSetValue());
                    iPiAllocationTableFieldService.update(updateWrapper);
                }


            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    /**
     * 处理字段字符串，清理数字字段中多余的.000
     * @param input 输入字符串，如 "field_0002 = '4.000', field_0005 = '生产信息'"
     * @return 处理后的字符串
     */
    public static String processFieldString(String input) {
        // 正则匹配字段名和值
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*'([^']*)'");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = matcher.group(2);

            // 处理数字字段
            if (fieldValue.matches("^\\d+\\.0+$")) {
                String cleanedValue = fieldValue.replaceAll("\\.0+$", "");
                matcher.appendReplacement(result, fieldName + " = '" + cleanedValue + "'");
            }
            // 其他字段保持原样
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 获取报表集合
     * @param piCustomizeTable
     * @return
     */
    @Override
    public List<PiCustomizeTable> getTableList(PiCustomizeTable piCustomizeTable) {
        LambdaQueryWrapper<PiCustomizeTable> queryWrapper = new LambdaQueryWrapper<PiCustomizeTable>();
        //queryWrapper.eq(PiCustomizeTable::getTableName,piCustomizeTable.getTableName());
        return list(queryWrapper);
    }


    /**
     * 报表数据查询(自定义查询条件)
     * @param tableConditionQueryDto
     * table 表名,
     * fieldList 指定字段集合,为空查询所有(*)
     * condition 指定义条件(条件1=val1 and 条件2=val2)
     * sortField 排序字段
     * sortMode 排序方式
     * rowsNumber 获取行数
     * @return
     */
    @Override
    public List<Map<String, Object>> getTableByCondition(TableConditionQueryDto tableConditionQueryDto) {

        //获取字段列,为空查询所有
        List<String> fieldList = tableConditionQueryDto.getFieldList();
        String field = " * ";
        if(null != fieldList && !fieldList.isEmpty()){
            field = " id,tenant_id,factory_code,dept_code,line_code,sort,del_flag,create_time,update_time,create_by,update_by, ";
            field += String.join(",", fieldList);
        }

        //获取查询条件
        String condition = tableConditionQueryDto.getCondition();

        //表名
        String tableName = tableConditionQueryDto.getTable();
        //判断查询表还是视图
        String viewName = tableOrView(tableName);
        if(StrUtil.isNotBlank(viewName)){
            tableName = viewName;
        }

        //排序字段
        String sortField = tableConditionQueryDto.getSortField();
        //排序方式
        String sortMode = tableConditionQueryDto.getSortMode();
        //行数
        int rowsNumber= tableConditionQueryDto.getRowsNumber() != null?tableConditionQueryDto.getRowsNumber():-1;

        //验证
        SqlInjectionUtil.filterContent(field);
//        SqlInjectionUtil.filterContent(condition);
        SqlInjectionUtil.filterContent(tableName);
        //TenantContext.setTenant("1006"); //租户id
        SqlInjectionUtil.filterContent(sortField);
        SqlInjectionUtil.filterContent(sortMode);
        String sort = null;

        if(null != sortField && null != sortMode && StrUtil.isNotBlank(sortField) && StrUtil.isNotBlank(sortMode)){
            sort = sortField+" "+sortMode;
        }

        if(null != tableConditionQueryDto.getIsIndex() && tableConditionQueryDto.getIsIndex()){
            //获取数据
            return piCustomizeTableMapper.getDataByJson(field, tableName, condition, sort, rowsNumber);
        } else {
            //获取数据
            return piCustomizeTableMapper.getTableData(field,tableName,condition,sort,rowsNumber);
        }
    }


    /**
     * 统计算法配置数量
     * @param list
     * @return
     */
    @Override
    public IPage<PiCustomizeTable> statisticsData(IPage<PiCustomizeTable> list) {
        //
        if(null != list && list.getRecords().size()>0){
            //获取表名集合
            List<PiCustomizeTable> tableList = list.getRecords();
            List<String> tableNameList = new ArrayList<>();
            tableList.stream()
                    .map(PiCustomizeTable::getTableName)
                    .forEach(tableNameList::add);

            //获取字段 获取报表字段
            LambdaQueryWrapper<PiCustomizeTableField> computationModelWrapper = Wrappers.lambdaQuery(PiCustomizeTableField.class);
            computationModelWrapper.in(PiCustomizeTableField::getTableName,tableNameList);
            computationModelWrapper.eq(PiCustomizeTableField::getDelFlag,0);
            List<PiCustomizeTableField> fieldList = iPiCustomizeTableFieldService.list(computationModelWrapper);
            //字段集合
            List<String> fieldNameList = new ArrayList<>();
            fieldList.stream()
                    .map(PiCustomizeTableField::getFieldName)
                    .forEach(fieldNameList::add);

            //获取字段数(表中字段数)
            Map<String, Integer> tableFieldCountMap = fieldList.stream()
                    .collect(Collectors.toMap(
                            PiCustomizeTableField::getTableName,
                            v -> 1,
                            Integer::sum
                    ));

            //获取配置的算法
            //获取算法主表
            List<PiCalculateSignal> calculateSignalList = new ArrayList<>();
            //获取Adapter算法
            List<PiAdapterAlgorithm> adapterAlgorithmList = new ArrayList<>();
            //获取Adapter算法
            List<PiDfAlgorithmBaseinfo> piDfAlgorithmBaseinfoList = new ArrayList<>();
            if(tableNameList != null && tableNameList.size()>0 && fieldNameList != null && fieldNameList.size()>0){
                LambdaQueryWrapper<PiCalculateSignal> queryWrapper = new LambdaQueryWrapper<PiCalculateSignal>();
                queryWrapper.in(PiCalculateSignal::getPiTable,tableNameList);
                queryWrapper.in(PiCalculateSignal::getPiCode,fieldNameList);
                queryWrapper.eq(PiCalculateSignal::getDelFlag,0);
                calculateSignalList = iPiCalculateSignalService.list(queryWrapper);

                //Adapter算法
                LambdaQueryWrapper<PiAdapterAlgorithm> adapterqueryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
                adapterqueryWrapper.in(PiAdapterAlgorithm::getPiTable,tableNameList);
                adapterqueryWrapper.in(PiAdapterAlgorithm::getPiCode,fieldNameList);
                adapterqueryWrapper.eq(PiAdapterAlgorithm::getDelFlag,0);
                adapterAlgorithmList = iPiAdapterAlgorithmService.list(adapterqueryWrapper);

                //合并到基础算法
                for (PiAdapterAlgorithm piAdapterAlgorithm : adapterAlgorithmList) {
                    PiCalculateSignal piCalculateSignal = new PiCalculateSignal();
                    piCalculateSignal.setPiTable(piAdapterAlgorithm.getPiTable());
                    piCalculateSignal.setPiCode(piAdapterAlgorithm.getPiTable()+"-"+piAdapterAlgorithm.getPiCode());
                    calculateSignalList.add(piCalculateSignal);
                }

                //Df算法
                LambdaQueryWrapper<PiDfAlgorithmBaseinfo> dfWrapper = new LambdaQueryWrapper<PiDfAlgorithmBaseinfo>();
                dfWrapper.in(PiDfAlgorithmBaseinfo::getPiTableName,tableNameList);
                dfWrapper.in(PiDfAlgorithmBaseinfo::getPiFieldName,fieldNameList);
                dfWrapper.eq(PiDfAlgorithmBaseinfo::getDelFlag,0);
                piDfAlgorithmBaseinfoList = piDfAlgorithmBaseinfoService.list(dfWrapper);
                //合并到基础算法
                for (PiDfAlgorithmBaseinfo piDfAlgorithmBaseinfo : piDfAlgorithmBaseinfoList) {
                    PiCalculateSignal piCalculateSignal = new PiCalculateSignal();
                    piCalculateSignal.setPiTable(piDfAlgorithmBaseinfo.getPiTableName());
                    piCalculateSignal.setPiCode(piDfAlgorithmBaseinfo.getPiTableName()+"-"+piDfAlgorithmBaseinfo.getPiFieldName());
                    calculateSignalList.add(piCalculateSignal);
                }
            }


            for (PiCalculateSignal piCalculateSignal : calculateSignalList) {
                piCalculateSignal.setPiCode(piCalculateSignal.getPiTable()+"-"+piCalculateSignal.getPiCode());
            }
            Map<String, Integer> tableAlgorithmCountMap = calculateSignalList.stream()
                    .collect(Collectors.groupingBy(
                            PiCalculateSignal::getPiTable,
                            Collectors.mapping(
                                    PiCalculateSignal::getPiCode,
                                    Collectors.collectingAndThen(
                                            Collectors.toSet(),
                                            Set::size
                                    )
                            )
                    ));


            for (PiCustomizeTable piCustomizeTable : tableList) {
                //配置字段数
                piCustomizeTable.setConfigureField(0);
                if(tableFieldCountMap.containsKey(piCustomizeTable.getTableName())){
                    piCustomizeTable.setConfigureField(tableFieldCountMap.get(piCustomizeTable.getTableName()));
                }
                //配置算法数
                piCustomizeTable.setConfigureAlgorithm(0);
                if(tableAlgorithmCountMap.containsKey(piCustomizeTable.getTableName())){
                    piCustomizeTable.setConfigureAlgorithm(tableAlgorithmCountMap.get(piCustomizeTable.getTableName()));
                }
                //未配置算法数
                piCustomizeTable.setNoConfigureAlgorithm(piCustomizeTable.getConfigureField()- piCustomizeTable.getConfigureAlgorithm());
            }
        }

        return list;
    }

    @Override
    public Map getPointChartData(PointChartDto dto) {
        //验证sql注入
        SqlInjectionUtil.filterContent(dto.getTableCode());
        SqlInjectionUtil.filterContent(dto.getFieldCodes());
        SqlInjectionUtil.filterContent(dto.getTimeCode());
        //根据表名获取字段数据
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiCustomizeTableField::getTableName,dto.getTableCode());
        List<PiCustomizeTableField> fieldList = iPiCustomizeTableFieldService.list(queryWrapper);
        //字段集合
        Map<String, Object> fieldMap = fieldList.stream()
                .collect(Collectors.toMap(PiCustomizeTableField::getFieldName, PiCustomizeTableField::getName, (oldValue, newValue) -> oldValue));
        //根据PI字段code转换名称
        String[] arr = dto.getFieldCodes().split(",");
        List<Map> newFieldList = new ArrayList<>();
        for(int i = 0; i < arr.length; i++){
            Map m = new HashMap();
            m.put("code", arr[i]);
            m.put("name", MapUtil.getStr(fieldMap, arr[i]));
            newFieldList.add(m);
        }
        Map map = new HashMap();
        map.put("fieldList", newFieldList);
        map.put("data", piCustomizeTableMapper.getPointChartData(dto));
        return map;
    }

    @Override
    public Map<String, Object> getPointDiagramData(PointDiagramDto dto) {
        //获取字段标准配置，查询sql等数据
        Map<String, PiStandardMaintenance> maintenanceMap = getPiStandardMaintenance(dto);
        //查询并解析数据
        List<PointDiagramDto> list = piCustomizeTableMapper.queryPointDiagram(dto);
        if(CollUtil.isEmpty(list)){
            return null;
        }
        List<String> idList = list.stream()
                .map(PointDiagramDto::getId)
                .toList();
        //获取数据的计算标准
        List<PiStandardData> standardDataList = piStandardDataService.lambdaQuery()
                .in(PiStandardData::getDataId, idList)
                .eq(PiStandardData::getTableName, dto.getTableName())
                .orderByAsc(PiStandardData::getDataId)
                .orderByDesc(PiStandardData::getUpdateTime)
                .list();
        //将数据计算标准转换为map
        Map<String, PiStandardData> standardDataMap  = standardDataList.stream()
                .collect(Collectors.toMap(
                        PiStandardData::getDataId,
                        v -> v,
                        (existing, replacement) -> existing ));

        //x轴数据
        List<String> xAxisArr = new ArrayList<>();
        //y轴数据
        List<String> yAxisArr = new ArrayList<>();
        //红上数据
        List<String> redUpArr = new ArrayList<>();
        //黄上数据
        List<String> yellowUpArr = new ArrayList<>();
        //标准数据
        List<String> standardValueArr = new ArrayList<>();
        //黄下数据
        List<String> yellowDownArr = new ArrayList<>();
        //红下数据
        List<String> redDownArr = new ArrayList<>();
        //标准名称数据
        List<String> manageNameArr = new ArrayList<>();

        List<PointDiagramDto> resultList = new ArrayList<>();
        //获取各数据的红黄标准
        for(PointDiagramDto diagramDto : list){
            //判断是否有对应标准
            boolean haveStandard = false;
            //是否有对应计算数据
            if(standardDataMap.containsKey(diagramDto.getId())){
                PiStandardData piStandardData = standardDataMap.get(diagramDto.getId());
                //是否有计算数据json
                if(StrUtil.isNotBlank(piStandardData.getStandardJson())){
                    JSONArray jsonArray = JSONUtil.parseArray(piStandardData.getStandardJson());
                    //根据计算数据获取循环判断是否有对应的用户选择的查询标准
                    for(Object obj : jsonArray){
                        try{
                            if (obj instanceof JSONObject jsonObject && null != jsonObject.get("field") && jsonObject.get("field").equals(dto.getFieldName())) {
                                String key = jsonObject.getStr("maintenance_id")+"_"+jsonObject.getStr("manage_id");
                                //判断计算数据存的标准是否在用户所选标准中
                                if(maintenanceMap.containsKey( key)){
                                    haveStandard = true;
                                    //获取标准配置
                                    PiStandardMaintenance piStandardMaintenance = maintenanceMap.get(key);
                                    Map<String, Object> markLineMap = getMarkLine(piStandardMaintenance);
                                    redUpArr.add(MapUtil.getStr(markLineMap, "redUp"));
                                    yellowUpArr.add(MapUtil.getStr(markLineMap, "yellowUp"));
                                    standardValueArr.add(MapUtil.getStr(markLineMap, "standardValue"));
                                    yellowDownArr.add(MapUtil.getStr(markLineMap, "yellowDown"));
                                    redDownArr.add(MapUtil.getStr(markLineMap, "redDown"));
                                    manageNameArr.add(MapUtil.getStr(markLineMap, "manageName"));
                                    break;
                                }
                            }
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            //有对应标准的数据显示
            if(haveStandard){
                xAxisArr.add(diagramDto.getXFieldName());
                yAxisArr.add(StrUtil.isNotBlank(diagramDto.getFieldName()) ? diagramDto.getFieldName() : "0");
            }
        }
        //返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("xAxisArr", xAxisArr);
        resultMap.put("yAxisArr", yAxisArr);
        resultMap.put("redUpArr", redUpArr);
        resultMap.put("yellowUpArr", yellowUpArr);
        resultMap.put("standardValueArr", standardValueArr);
        resultMap.put("yellowDownArr", yellowDownArr);
        resultMap.put("redDownArr", redDownArr);
        resultMap.put("manageNameArr", manageNameArr);
        return resultMap;
    }

    @Override
    public Map<String, Object> getBoxDiagramData(PointDiagramDto dto) {
        //获取报表唯一标识字段,类型为时间，多个取第一个
        List<PiCustomizeTableField> piFieldList = iPiCustomizeTableFieldService.lambdaQuery()
                .eq(PiCustomizeTableField::getTableName, dto.getTableName())
                .eq(PiCustomizeTableField::getIsUnique, 1)
                .eq(PiCustomizeTableField::getType, 6)
                .orderByAsc(PiCustomizeTableField::getSort)
                .list();
        if(CollUtil.isEmpty(piFieldList)){
            throw new JeecgBootException("唯一标识不存在！");
        }
        PiCustomizeTableField piField = piFieldList.get(0);
        dto.setUniqueField(piField.getFieldName());
        //查询并解析数据
        List<PointDiagramDto> list = piCustomizeTableMapper.queryBoxDiagram(dto);
        Map<String, List<Double>> groupedByUniqueFieldMap = list.stream()
                .filter(item -> {
                    try {
                        Double.parseDouble(item.getFieldName().trim());
                        return true;
                    } catch (NumberFormatException | NullPointerException e) {
                        return false;
                    }
                })
                .collect(Collectors.groupingBy(
                        PointDiagramDto::getUniqueField,
                        LinkedHashMap::new,
                        Collectors.mapping(item -> Double.parseDouble(item.getFieldName()), Collectors.toList())
                ));
        //散点数据
        List<List<String>> scatterList = new ArrayList<>();
        //箱图数据
        List<List<Double>> boxList = new ArrayList<>();
        //名称数据
        List<String> nameList = new ArrayList<>();
        //计算箱图数据
        for (Map.Entry<String, List<Double>> entry : groupedByUniqueFieldMap.entrySet()) {
            //计算箱图数据
            List<Double> tempList = BoxPlotCalculatorUtil.calculateBoxPlotData(entry.getValue());
            //添加箱图数据
            boxList.add(tempList);
            //添加名称数据
            nameList.add(entry.getKey());
            //处理散点数据
            getScatterList(scatterList, entry.getKey(), tempList);
        }
        //返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("boxList", boxList);
        resultMap.put("nameList", nameList);
        resultMap.put("scatterList", scatterList);

        //获取字段标准配置
        Map<String, PiStandardMaintenance> maintenanceMap = getPiStandardMaintenance(dto);
        if (!CollUtil.isEmpty(maintenanceMap)) {
            resultMap.putAll(getMarkLine(maintenanceMap.values().iterator().next()));
        }
        return resultMap;
    }

    /**
     * 获取标准数据
     * @param dto 参数
     * @return 结果
     */
    private Map<String, PiStandardMaintenance> getPiStandardMaintenance(PointDiagramDto dto){
        //验证sql注入
        SqlInjectionUtil.filterContent(dto.getTableName());
        SqlInjectionUtil.filterContent(dto.getFieldName());
        SqlInjectionUtil.filterContent(dto.getXFieldName());
        if(StrUtil.isBlank(dto.getStandardId())){
            throw new JeecgBootException("未选择标准！");
        }
        //拆分标准id
        List<String> standardIdList = Arrays.asList(dto.getStandardId().split(","));
        //查询标准
        List<PiStandardManage> piStandardManageList = piStandardManageService.lambdaQuery()
                .in(PiStandardManage::getId, standardIdList).orderByDesc(PiStandardManage::getCreateTime).list();
        if(CollUtil.isEmpty(piStandardManageList)){
            throw new JeecgBootException("标准不存在！");
        }
        //查询字段标准
        List<PiStandardMaintenance> piStandardMaintenanceList = piStandardMaintenanceService.lambdaQuery()
                .in(PiStandardMaintenance::getManageId, standardIdList)
                .eq(PiStandardMaintenance::getFieldName, dto.getFieldName())
                .orderByDesc(PiStandardMaintenance::getCreateTime)
                .list();
        if(CollUtil.isEmpty(piStandardMaintenanceList)){
            throw new JeecgBootException("标准字段不存在！");
        }
        //sql查询条件
        StringBuilder sqlStr = new StringBuilder();
        //循环标准拼接查询条件
        for(int i = 0; i < piStandardManageList.size(); i++){
            PiStandardManage piStandardManage = piStandardManageList.get(i);
            //查询条件非空的情况下才拼接
            if(StrUtil.isNotBlank(piStandardManage.getConditionJson())){
                //查询条件json
                JSONArray conditionJsonArray = JSONUtil.parseArray(piStandardManage.getConditionJson());
                if(CollUtil.isEmpty(conditionJsonArray)){
                    continue;
                }
                //判断是否拼接了其他标准的查询条件
                if(i > 0 && !sqlStr.isEmpty()){
                    sqlStr.append(" or ");
                }
                sqlStr.append("(");
                //拼接标识查询条件
                for(int j = 0; j < conditionJsonArray.size(); j++){
                    JSONObject jsonObject = JSONUtil.parseObj(conditionJsonArray.get(j));
                    if(j > 0){
                        sqlStr.append(" and ");
                    }
                    sqlStr.append(jsonObject.getStr("field")).append(" ")
                            .append(PiStandardSymboEnum.getStrByCode(jsonObject.getStr("symbol"))).append(" '")
                            .append(jsonObject.getStr("value")).append("' ");
                }
                sqlStr.append(")");
            }
        }
        //不为空设置查询条件
        if(!sqlStr.isEmpty()){
            dto.setStrSql(" and ("+sqlStr+")");
        }
        //转换pi字典标准数据后返回，key：id+标准id，value：对象
        return piStandardMaintenanceList.stream().collect(
                Collectors.toMap(
                        item -> item.getId() + "_" + item.getManageId(),
                        item -> item
                )
        );
    }

    /**
     * 获取散点数据
     * @param scatterList 散点图数据
     * @param name 名称
     * @param list 值
     */
    private void getScatterList(List<List<String>> scatterList, String name, List<Double> list){
        for(int i = 0; i < list.size(); i++){
            if(i != 1 && i != 3){
                List<String> arr = new ArrayList<>();
                arr.add(name);
                arr.add(list.get(i).toString());
                scatterList.add(arr);
            }
        }
    }

    /**
     * 获取点图标准线
     * @param piStandardMaintenance pi红黄标准
     * @return 结果
     */
    private Map<String, Object> getMarkLine(PiStandardMaintenance piStandardMaintenance){
        Double redUp = piStandardMaintenance.getRedUp();
        Double yellowUp = piStandardMaintenance.getYellowUp();
        Double standardValue = piStandardMaintenance.getStandardValue();
        Double yellowDown = piStandardMaintenance.getYellowDown();
        Double redDown = piStandardMaintenance.getRedDown();
        //如果是偏差值需要用标准值计算红黄区
        if("2".equals(piStandardMaintenance.getIndicatorType())){
            redUp = standardValue + redUp;
            yellowUp = standardValue + yellowUp;
            yellowDown = standardValue + yellowDown;
            redDown = standardValue + redDown;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("redUp", redUp);
        resultMap.put("yellowUp", yellowUp);
        resultMap.put("standardValue", standardValue);
        resultMap.put("yellowDown", yellowDown);
        resultMap.put("redDown", redDown);
        resultMap.put("manageName", piStandardMaintenance.getManageName());
        return resultMap;
    }


    //获取住户
    public SysUser getAdminUser(){
        return piCustomizeTableMapper.getAdminUser();
    }

    /**
     * 获取查询 表还是视图
     */
    public String tableOrView(String tableName) {
        String name = "";
        LambdaQueryWrapper<PiCustomizeTable> queryWrapper = new LambdaQueryWrapper<PiCustomizeTable>();
        queryWrapper.eq(PiCustomizeTable::getTableName,tableName);
        List<PiCustomizeTable> tableList = list(queryWrapper);
        if(tableList !=null && !tableList.isEmpty()){
            String viewName = tableList.get(0).getViewName();
            if(StrUtil.isNotBlank(viewName)){
                name = viewName;
            }
        }
        return name;
    }


    /**
     * 获取字段表对照
     */
    public Map<String, String> tableCompare(String tableName){
        LambdaQueryWrapper<PiCustomizeTableField> fieldWrapper = new LambdaQueryWrapper<PiCustomizeTableField>();
        fieldWrapper.eq(PiCustomizeTableField::getTableName,tableName);
        Map<String, String> map = iPiCustomizeTableFieldService.list(fieldWrapper).stream()
                .collect(Collectors.toMap(
                        PiCustomizeTableField::getFieldName,  // Key: fieldName
                        field -> Optional.ofNullable(field.getSubtable())  // 先尝试取 subtable
                                .filter(subtable -> !subtable.isEmpty())   // 过滤掉空字符串
                                .orElse(field.getTableName())              // 如果 subtable 为空，则取 tableName
                ));
        return map;
    }


    /**
     * 获取报表拥有的字段
     * @param tableName 表名
     * param isValueEnabled 启用设定值字段(1)
     */
    public List<PiCustomizeTableField> getTableFieldList(String tableName, String isValueEnabled){
        //查询启用设定值字段
        List<PiCustomizeTableField> piCustomizeTableFieldList = iPiCustomizeTableFieldService.lambdaQuery()
                .eq(PiCustomizeTableField::getTableName, tableName)
                //启用设定值字段
                .eq(StrUtil.isNotBlank(isValueEnabled), PiCustomizeTableField::getIsValueEnabled, isValueEnabled)
                .list();
        return piCustomizeTableFieldList;
    }


}
