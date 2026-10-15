package com.keytech.modules.pi.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @FileName ConditionParser
 * @Description 条件解析器
 * @Author HQ
 * @date 2025-10-09
 **/
public class ConditionParser {
    private static final Map<String, String> OPERATOR_MAP = new HashMap<>();

    static {
        OPERATOR_MAP.put("1", ">");
        OPERATOR_MAP.put("2", ">=");
        OPERATOR_MAP.put("3", "=");
        OPERATOR_MAP.put("4", "!=");
        OPERATOR_MAP.put("5", "<");
        OPERATOR_MAP.put("6", "<=");
        OPERATOR_MAP.put("7", "LIKE");
    }

    public static String parseCondition(String jsonCondition) {
        StringBuffer conditionBuff = new StringBuffer();
        try {
            // 使用 Hutool 解析 JSON 数组
            JSONArray conditions = JSONUtil.parseArray(jsonCondition);
            conditionBuff.append(" ( ");
            for (int i = 0; i < conditions.size(); i++) {
                JSONObject condition = conditions.getJSONObject(i);
                // 关系 or / and
                String relationship = condition.getStr("relationship");
                String field = condition.getStr("field");
                String value = condition.getStr("value");
                String symbolKey = condition.getStr("symbol");

                // 获取对应的SQL操作符
                String sqlOperator = OPERATOR_MAP.get(symbolKey);
                if (sqlOperator == null) {
                    sqlOperator = "="; // 默认操作符
                }

                // 构建查询条件
                if (i > 0 && conditionBuff.length() > 0) {
                    if(relationship.equals("1")){
                        //与(and)
                        conditionBuff.append(" and ");
                    }else{
                        //或(or)
                        conditionBuff.append(" or ");
                    }
                }
                conditionBuff.append(field)
                        .append(" ")
                        .append(sqlOperator)
                        .append(" ");

                // 特殊处理 LIKE 操作符
                if ("LIKE".equals(sqlOperator)) {
                    conditionBuff.append("'%")
                            .append(value)
                            .append("%'");
                } else {
                    conditionBuff.append("'")
                            .append(value)
                            .append("'");
                }
            }
            conditionBuff.append(" ) ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String condition = conditionBuff.toString();
        SqlInjectionUtil.specialFilterContent(condition);
        return condition;
    }
}
