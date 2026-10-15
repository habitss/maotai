package com.keytech.modules.custom.report.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.custom.report.dto.CustomQueryFieldDto;
import com.keytech.modules.custom.report.dto.SysUserDto;
import com.keytech.modules.custom.report.entity.CustomReportTemplateEntity;
import com.keytech.modules.custom.report.mapper.CustomReportTemplateMapper;
import com.keytech.modules.custom.report.service.ICustomReportTemplateService;
import com.keytech.modules.pi.util.SqlInjectionUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @Description: 针对表【mes3_custom_report_template(自定义报表模板配置)】的数据库操作Service实现
* @Author: sjw
* @CreateDate: 2024/12/03 14:49:00
*/
@Service
public class CustomReportTemplateServiceImpl extends BaseServiceImpl<CustomReportTemplateMapper, CustomReportTemplateEntity>
    implements ICustomReportTemplateService {
    private static final String START_TEXT = "@#";
    private static final String END_TEXT = "#@";
    private static final String JOIN_TEXT = "&&";
    @Override
    public List<SysUserDto> getUserAll() {
        return baseMapper.getUserAll();
    }

    @Override
    public List<LinkedHashMap<String, Object>> queryByTemplateConfig(Map<String, String> params) {
        //根据模版id获取配置数据
        CustomReportTemplateEntity customReportTemplateEntity = this.getById(params.get("templateId"));
        if(StrUtil.isBlank(customReportTemplateEntity.getReportConfig())){
            return null;
        }
        //获取模版配置
        JSONObject configJson = JSONUtil.parseObj(customReportTemplateEntity.getReportConfig());
        //获取模版中的表单配置
        JSONObject configFormJson = configJson.getJSONObject("configForm");
        //获取自定义sql
        String sql = configFormJson.getStr("querySql");

        //页面上的查询表单数据
        String queryFormStr = params.get("queryFiledData");
        JSONObject queryFormJson = JSONUtil.parseObj(queryFormStr);
        //获取自定义查询字段
        List<CustomQueryFieldDto> queryList = configFormJson.getBeanList("queryField", CustomQueryFieldDto.class);
        //获取条件匹配map用于替换查询条件
        Map<String, List<String>> matchMap = findAllMatches(sql);

        for(CustomQueryFieldDto dto : queryList){
            String formData = queryFormJson.getStr(dto.getConditionField());
            List<String> sqlFieldList = matchMap.get(dto.getConditionField());
            if(CollUtil.isNotEmpty(sqlFieldList)){
                for(String sqlField : sqlFieldList){
                    sql = getSqlString(sql, sqlField, dto.getConditionField(), dto.getConditionType(), formData);
                }
            }
        }
        if(queryFormJson.isEmpty()){
            sql = "select * from ("+sql+") t limit 100";
        }
        SqlInjectionUtil.customFilterContent(sql, true);
        return baseMapper.queryDataBySql(sql);
    }

    /**
     * 转换为sql条件
     * @param sql sql语句
     * @param sqlField 查询字段
     * @param conditionField 查询条件
     * @param conditionType 查询条件类型
     * @param data 表单数据
     * @return 结果
     */
    private String getSqlString(String sql, String sqlField, String conditionField, String conditionType, String data){
        String condition = " ";
        if(StrUtil.isNotBlank(data)){
            if(conditionType.equals("1")){
                condition = " and " + sqlField + " LIKE '%" + data + "%'";
            } else if(conditionType.equals("2") || conditionType.equals("3")){
                String[] arr = data.split(",");
                condition = " and " + sqlField + " >= '"+ arr[0] + "' and " + sqlField + " <= '" + arr[1]+"'";
            }
        }
        sql = sql.replaceAll(START_TEXT+sqlField+JOIN_TEXT+conditionField+END_TEXT, condition);
        return sql;
    }

    /**
     * 获取所有需要匹配查询条件的位置
     * @param input 输入的字符串
     * @return 结果
     */
    public static Map<String, List<String>> findAllMatches(String input) {
        Map<String, List<String>> resultMap = new HashMap<>();
        // 使用转义字符以匹配实际的{}符号，并且只捕获中间的内容
        String patternString = Pattern.quote(START_TEXT) + "(.*?)" + Pattern.quote(END_TEXT);
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            // 获取匹配到的内容
            String content = matcher.group(1);
            // 将内容按&&进行分割，下标0为查询字段，下标1为匹配的条件字段
            String[] arr = content.split(JOIN_TEXT);
            List<String> list = resultMap.get(arr[1]);
            if(CollUtil.isEmpty(list)){
                list = new ArrayList<>();
            }
            list.add(arr[0]);
            resultMap.put(arr[1], list);
        }
        return resultMap;
    }
}




