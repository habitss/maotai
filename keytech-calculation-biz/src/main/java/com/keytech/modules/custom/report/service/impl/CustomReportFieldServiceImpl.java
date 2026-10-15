package com.keytech.modules.custom.report.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.custom.report.entity.CustomReport;
import com.keytech.modules.custom.report.entity.CustomReportField;
import com.keytech.modules.custom.report.entity.CustomReportTemplateEntity;
import com.keytech.modules.custom.report.mapper.CustomReportFieldMapper;
import com.keytech.modules.custom.report.mapper.CustomReportMapper;
import com.keytech.modules.custom.report.mapper.CustomReportTemplateMapper;
import com.keytech.modules.custom.report.service.ICustomReportFieldService;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.util.CustomizeTableUtil;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
@Slf4j
public class CustomReportFieldServiceImpl extends BaseServiceImpl<CustomReportFieldMapper, CustomReportField> implements ICustomReportFieldService {
    @Resource
    private CustomReportMapper customReportMapper;
    @Resource
    private CustomReportFieldMapper customReportFieldMapper;
    @Resource
    private CustomReportTemplateMapper customReportTemplateMapper;
    @Resource
    private IMdmDictService imdmDictService;


    /**
     * 存储字段同时建表
     * @param customReportField
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveField(CustomReportField customReportField) {
        List<CustomReportField> list = customReportFieldMapper.getFieldByTableId(customReportField);

        //获取表名称
        CustomReport customizeTable= customReportMapper.selectById(customReportField.getTableId());

        if(null != customizeTable){
            processingSaveData(customReportField, list.size(), customizeTable.getTableName());
            //添加表字段
            customReportFieldMapper.addTableField(customReportField);
            //保存记录
            save(customReportField);
            return Result.OK("保存成功!");
        }else{
            return Result.error("表不存在!");
        }
    }

    /**
     * 根据模板id获取表头
     * @param templateId 模版id
     * @return
     */
    @Override
    public List<CustomReportField> queryHeaderByTemplateId(String templateId) {
        CustomReportTemplateEntity customReportTemplate = customReportTemplateMapper.selectById(templateId);
        LambdaQueryWrapper<CustomReportField>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomReportField::getTableId, customReportTemplate.getTableId())
                .eq(CustomReportField::getIsList, 1)
                .orderByAsc(CustomReportField::getSort);
        List<CustomReportField> list = customReportFieldMapper.selectList(queryWrapper);
        return list;
    }

    @Override
    public Map<String, Object> queryData(Integer pageNo, Integer pageSize, String orderField, String orderType, String templateId, HttpServletRequest req) {
        //mapping查询中需要用到的参数
        Map<String,Object> params = new HashMap<>();
        //需要返回的数据
        Map<String,Object> resultMap = new HashMap<>();
        //分页数据集合
        List<Map<String,Object>> list = new ArrayList<>();
        //数据总数
        int total = 0;
        //获取配置表中的查询字段
        List<CustomReportField> searchFieldLis = queryHeaderByTemplateId(templateId);
        //获取模板配置数据
        CustomReportTemplateEntity customReportTemplate = customReportTemplateMapper.selectById(templateId);
        //获取请求中的查询参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //查询条件
        StringBuffer conditionBuffer = processingParam(searchFieldLis, parameterMap);
        //显示字段
        StringBuffer fieldBuffer = processingField(searchFieldLis);
        //排序字段
        StringBuffer sortBuffer = processingSortField(searchFieldLis);

        //查询字段
        params.put("field", fieldBuffer.toString());
        //查询条件
        params.put("condition", conditionBuffer.toString());
        //查询表
        params.put("tableName", customReportTemplate.getTableName());
        //排序字段
        params.put("sortField", sortBuffer.toString());

        //查询总数
        total = customReportFieldMapper.getDynamicDataTotal(params);
        if(total > 0){
            //分页查询
            params.put("pageNo", (pageNo-1) * pageSize);
            params.put("pageSize", pageSize);
            list = processingResultList(customReportFieldMapper.getDynamicDataPage(params), customReportTemplate.getReportConfig());
        }

        resultMap.put("records",list);
        resultMap.put("total",total);
        return resultMap;
    }

    @Override
    public Double getMaxSort(String tableId) {
        return baseMapper.getMaxSort(tableId);
    }

    @Override
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Class<CustomReportField> customReportFieldClass) throws IOException {
        long start = System.currentTimeMillis();
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据
                List<CustomReportField> list = ExcelImportUtil.importExcel(file.getInputStream(), customReportFieldClass, params);
                //关联表主键
                String tableId = list.get(0).getTableId();
                //获取关联表名称
                CustomReport customizeTable = customReportMapper.selectById(tableId);
                if(null != customizeTable){
                    String tableName = customizeTable.getTableName();
                    //所有要操作的数据
                    List<CustomReportField> allList = new ArrayList<>();
                    //根据关联表主键获取数据
                    List<CustomReportField> customReportFieldList = baseMapper.getFieldByTableId(list.get(0));
                    //获取总数用作列名
                    int total = customReportFieldList.size();

                    for(int i = 0; i < list.size(); i++){
                        CustomReportField importData = list.get(i);
                        //判断导入数据表是否一致
                        if(tableId.equals(importData.getTableId())){
                            //判断是否为新增数据
                            if(StrUtil.isNotBlank(importData.getId())){
                                boolean isExists = false;
                                for(CustomReportField historyData : customReportFieldList){
                                    //如果id是否存在
                                    if(importData.getId().equals(historyData.getId())){
                                        //如果存在则更新字段信息，英文字段、类型、长度不能修改用原始数据
                                        importData.setType(historyData.getType());
                                        importData.setFieldLength(historyData.getFieldLength());
                                        importData.setFieldName(historyData.getFieldName());
                                        isExists = true;
                                        successLines++;
                                        allList.add(importData);
                                        break;
                                    }
                                }
                                if (!isExists){
                                    errorLines++;
                                    errorMessage.add("第"+(i+1)+"行，id不存在!");
                                }
                            } else {
                                processingSaveData(importData, total, tableName);
                                allList.add(importData);
                                baseMapper.addTableField(importData);
                                successLines++;
                                total++;
                            }
                        } else {
                            errorLines++;
                            errorMessage.add("第"+(i+1)+"行，自定义表id错误!");
                        }
                    }
                    //批量操作所有数据
                    this.saveOrUpdateBatch(allList);
                    log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                } else {
                    errorLines++;
                    errorMessage.add("自定义表id不存在!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    /**
     * 处理显示字段
     * @return
     */
    private StringBuffer processingField(List<CustomReportField> searchFieldLis){
        //显示字段
        StringBuffer fieldBuffer = new StringBuffer();
        fieldBuffer.append(" id ");
        //循环配置拼接列表显示字段和sql查询参数
        for(CustomReportField customReportField : searchFieldLis){
            //字段
            String field = customReportField.getFieldName();
            //字段类型
            String type = customReportField.getType();
            fieldBuffer.append(" , ");
            if("4".equals(type)){
                //日期
                fieldBuffer.append(" DATE_FORMAT( ").append(field).append(", '%Y-%m-%d') as ").append(field);
            }else if("6".equals(type)){
                //时间
                fieldBuffer.append(" DATE_FORMAT( ").append(field).append(", '%Y-%m-%d %H:%i:%s') as ").append(field);
            }else{
                //其他类型
                fieldBuffer.append(field);
            }
        }
        return fieldBuffer;
    }

    /**
     * 处理查询参数
     * @return
     */
    private StringBuffer processingParam(List<CustomReportField> searchFieldLis, Map<String, String[]> parameterMap){
        //查询条件
        StringBuffer conditionBuffer = new StringBuffer();
        // 转换配置表集合为map用于比较
        Map<String, String> searchMap = searchFieldLis.stream()
                .collect(Collectors.toMap(CustomReportField::getFieldName, CustomReportField::getType));
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            String type = MapUtil.getStr(searchMap, key);
            //SQL注入检测
            SqlInjectionUtil.filterContent(value);

            if("4".equals(type) || "6".equals(type)){
                //处理时间类型参数，拼接sql
                String[] dateValue = value.split(",");
                conditionBuffer.append(" and ").append(key).append(" >= '").append(dateValue[0]).append("' ");
                conditionBuffer.append(" and ").append(key).append(" <= '").append(dateValue[1]).append("' ");
            }else if(StrUtil.isNotBlank(type)){
                //默认参数模糊查询，拼接sql
                conditionBuffer.append(" and ").append(key).append(" like '%")
                        .append(value).append("%' ");
            }
        }
        return conditionBuffer;
    }

    /**
     * 处理排序字段
     * @return
     */
    private StringBuffer processingSortField(List<CustomReportField> searchFieldLis){
        //排序字段
        StringBuffer sortBuffer = new StringBuffer();
        //循环配置拼接列表显示字段和sql查询参数
        for(CustomReportField customReportField : searchFieldLis){
            if(null != customReportField.getIsSort() && 1 == customReportField.getIsSort()){
                sortBuffer.append(customReportField.getFieldName());
                if(null == customReportField.getSortType() || 0 == customReportField.getSortType()){
                    sortBuffer.append(" desc , ");
                } else {
                    sortBuffer.append(" asc , ");
                }
            }
        }
        sortBuffer.append(" id desc ");
        return sortBuffer;
    }

    /**
     * 处理返回数据
     * @return
     */
    private List<Map<String,Object>> processingResultList(List<Map<String,Object>> list, String reportConfig){
        //获取所有字典
        List<MdmDict> dictList = imdmDictService.list();
        //根据groupCode分组
        Map<String, List<MdmDict>> dictGroup = dictList.stream().collect(Collectors.groupingBy(MdmDict::getGroupCode));
        //模版配置
        JSONObject jsonConfig = JSONUtil.parseObj(reportConfig);
        //模版填报项配置
        JSONArray jsonArray = JSONUtil.parseArray(jsonConfig.get("cellComponentsList"));
        //解析数据
        for(Map<String,Object> map : list){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                //解析模版填报项配置
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //判断列名是否相等且是否为字典
                    if(entry.getKey().equals(jsonObject.getStr("mappingColumn"))
                        && StrUtil.isNotBlank(jsonObject.getStr("dictType"))){
                        //获取字典数据
                        List<MdmDict> groupList = dictGroup.get(jsonObject.getStr("dictType"));
                        if(CollectionUtil.isNotEmpty(groupList)){
                            //将字典数据转换为map
                            Map<String, String> dMap = groupList.stream()
                                    .collect(Collectors.toMap(
                                            MdmDict::getItemVal, //提取键
                                            MdmDict::getItemKey  //提取值
                                    ));
                            if(null != entry.getValue()){
                                //获取字典值
                                map.put(entry.getKey(), dMap.get(entry.getValue().toString()));
                            }
                        }
                        break;
                    }
                }
            }
        }
        return list;
    }

    private CustomReportField processingSaveData(CustomReportField customReportField,int total, String tableName){
        //生成指定字段名称(已有数量+1)
        customReportField.setFieldName("field_"+(total+1));
        //设置表名
        customReportField.setTableName(tableName);
        //获取类型
        customReportField.setFieldType(CustomizeTableUtil.getFieldType(customReportField.getType()));
        //防止sql注入
        //SqlInjectionUtil.filterContent(customReportField.getFieldName());
        SqlInjectionUtil.filterContent(customReportField.getFieldLength()+"");
        if("1".equals(customReportField.getType())){
            if(null == customReportField.getFieldLength() || 0 == customReportField.getFieldLength()){
                //判断是否填写长度，默认给50
                customReportField.setFieldLength(50);
            } else if (500 < customReportField.getFieldLength()){
                //长度大于500，设置为500
                customReportField.setFieldLength(500);
            }
        }
        return customReportField;
    }
}
