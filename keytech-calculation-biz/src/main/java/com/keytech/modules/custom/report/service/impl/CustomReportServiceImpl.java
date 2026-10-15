package com.keytech.modules.custom.report.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.custom.report.entity.CustomReport;
import com.keytech.modules.custom.report.mapper.CustomReportMapper;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableQueryDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.custom.report.service.ICustomReportService;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @Description:自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
public class CustomReportServiceImpl extends BaseServiceImpl<CustomReportMapper, CustomReport> implements ICustomReportService {

    @Resource
    private CustomReportMapper customReportMapper;

    /**
     *
     * @param customReport
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveTable(CustomReport customReport) {
        //获取已经建表的数量
        int tableTotal = customReportMapper.getTableTotal()-2;
        customReport.setTableName("mes3_custom_"+String.format("%0" + 3 + "d", tableTotal));
        customReportMapper.createNewTable(customReport);
        //存储
        save(customReport);
        return Result.OK("保存成功!");
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
        List<Map<String, Object>> list = new ArrayList<>();
        //获取字段列
        List<String> fieldList = tableQueryDto.getFieldList();
        String field = String.join(",", fieldList);
        //获取查询条件
        List<TableConditionDto> conditionList = tableQueryDto.getConditionList();
        String condition = conditionList.stream()
                .map(obj -> obj.getKey() + " = " + obj.getVal())
                .collect(Collectors.joining(" and "));

        //验证
//        SqlInjectionUtil.filterContent(field);
//        SqlInjectionUtil.filterContent(tableQueryDto.getTable());
//        SqlInjectionUtil.filterContent(condition);
        TenantContext.setTenant("1004"); //租户id
        //获取数据
        list = customReportMapper.getTableData(field,tableQueryDto.getTable(),condition);

        return list;
    }

    /**
     * INSERT INTO {table}(field1,field2) VALUES (fieldVal1,fieldVal2);
     * insert
     * @param tableSaveDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String tableSave(TableSaveDto tableSaveDto) {
        try {
            //字段值集合
            List<TableFieldValueDto> fieldList = tableSaveDto.getFieldList();

            //获取约束条件
            List<TableConditionDto> conditionList = tableSaveDto.getConditionList();
            // 约束条件转化成 key1=val1 and key2=val2
            StringJoiner sj = new StringJoiner(" and ");
            conditionList.stream()
                    .map(dto -> dto.getKey() + " = " + dto.getVal())
                    .forEach(sj::add);
            //约束条件
            String condition = sj.toString();
            //表名
            String table = tableSaveDto.getTable();
            //验证sql注入
            //SqlInjectionUtil.filterContent(table);
            //SqlInjectionUtil.filterContent(condition);

            //更新修改标记 0新增 1修改
            String mark = "0";
            //判断新增还是修改(conditionStr为空默认新增)
            if(StrUtil.isNotBlank(condition)){
                //添加租户id
                condition+=" AND tenant_id = 1004 ";
                TenantContext.setTenant("1004");
                //判断新增修改
                List<Map<String, Object>> list = customReportMapper.getTableData("id",table,condition);
                if(null != list && list.size() > 0){
                    //存在数据做修改操作
                    mark = "1";
                }
            }

            if("1".equals(mark)){
                //修改
                //字段值集合 (field = 'fieldVal', field = 'fieldVal')
                String fieldValStr = fieldList.stream()
                        .map(dto -> dto.getField() + " = '"+ dto.getFieldVal() + "'")
                        .collect(Collectors.joining(", "));
                //验证sql注入
                //SqlInjectionUtil.filterContent(fieldValStr);

                customReportMapper.tableUpdate(fieldValStr,table,condition);
            }else{
                //新增
                //字段
                List<String> fields = new ArrayList<>();
                //值
                StringBuffer fieldValBuff = new StringBuffer();
                //获取字段和值的集合
                for (TableFieldValueDto dto : fieldList) {
                    fields.add(dto.getField());
                    if(fieldValBuff.length()>0){
                        fieldValBuff.append(" , ").append(" '").append(dto.getFieldVal()).append("' ");
                    }else{
                        fieldValBuff.append(" '").append(dto.getFieldVal()).append("' ");
                    }
                }
                //约束条件也需要写入
                for (TableConditionDto dto : conditionList) {
                    fields.add(dto.getKey());
                    if(fieldValBuff.length()>0){
                        fieldValBuff.append(" , ").append(" '").append(dto.getVal()).append("' ");
                    }else{
                        fieldValBuff.append(" '").append(dto.getVal()).append("' ");
                    }
                }
                //添加租户 ID
                if(fieldValBuff.length()>0){
                    fieldValBuff.append(",'1004'");
                }
                String fieldStr = "";
                if(null != fields && fields.size()>0){
                    //字段 field1,field2,field3
                    //添加租户 ID
                    fields.add("tenant_id");
                    fieldStr = String.join(",", fields);
                }
                //值 'fieldVal1','fieldVal2','fieldVal3'
                String fieldValStr = fieldValBuff.toString();

                if(StrUtil.isNotBlank(fieldStr) && StrUtil.isNotBlank(fieldValStr)){
                    //验证sql注入
                    //SqlInjectionUtil.filterContent(fieldStr);
                    //SqlInjectionUtil.filterContent(fieldValStr);
                    customReportMapper.tableSave(fieldStr,fieldValStr,table);
                }
            }
            return "0";
        } catch (Exception e) {
            return "1";
        }
    }

    /**
     * 获取报表集合
     * @param customReport
     * @return
     */
    @Override
    public List<CustomReport> getTableList(CustomReport customReport) {
        LambdaQueryWrapper<CustomReport> queryWrapper = new LambdaQueryWrapper<CustomReport>();
        return list(queryWrapper);
    }
}
