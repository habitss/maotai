package com.keytech.modules.custom.report.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.custom.report.dto.CustomReportFillInDto;
import com.keytech.modules.custom.report.mapper.CustomReportFillInMapper;
import com.keytech.modules.custom.report.service.ICustomReportFillInService;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: 自定义报表填报
 * @Author: sjw
 * @Date: 2024/12/9 17:42
 */
@Service
public class CustomReportFillInServiceImpl implements ICustomReportFillInService {

    @Resource
    private CustomReportFillInMapper customReportFillInMapper;

    @Override
    public void addData(CustomReportFillInDto customReportFillInDto) {
        //Sql所需数据，key为列名，value为对应值
        Map<String, Object> params = processingData(customReportFillInDto);
        customReportFillInMapper.addData(customReportFillInDto.getTableName(), params);
    }

    @Override
    public void updateData(CustomReportFillInDto customReportFillInDto) {
        //Sql所需数据，key为列名，value为对应值
        Map<String, Object> params = processingData(customReportFillInDto);
        customReportFillInMapper.updateData(customReportFillInDto.getTableName(), customReportFillInDto.getRecordId(), params);
    }

    @Override
    public void removeById(CustomReportFillInDto customReportFillInDto) {
        //SQL注入检测
        SqlInjectionUtil.filterContent(customReportFillInDto.getTableName());
        customReportFillInMapper.removeById(customReportFillInDto.getTableName(), customReportFillInDto.getRecordId());
    }

    /**
     * 数据处理
     */
    private Map<String, Object> processingData(CustomReportFillInDto customReportFillInDto){
        //Sql所需数据，key为列名，value为对应值
        Map<String, Object> params = new HashMap<>();
        //SQL注入检测
        SqlInjectionUtil.filterContent(customReportFillInDto.getTableName());
        //解析json
        JSONArray jsonArray = JSONUtil.parseArray(customReportFillInDto.getFillInList());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            //SQL注入检测
            SqlInjectionUtil.filterContent(jsonObject.getStr("mappingColumn"));
            String value = jsonObject.getStr("value");
            //判断字典项是否为空，非空则保存字典值
            if(StrUtil.isNotBlank(jsonObject.getStr("dictKey"))){
                value = jsonObject.getStr("dictKey");
            }
            params.put(jsonObject.getStr("mappingColumn"), value);
        }
        params.put("template_id", customReportFillInDto.getTemplateId());
        return params;
    }
}
