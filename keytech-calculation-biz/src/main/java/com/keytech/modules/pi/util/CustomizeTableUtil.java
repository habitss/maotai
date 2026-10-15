package com.keytech.modules.pi.util;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeTableUtil {

    /**
     * 获取字段类型[表对应类型]
     * @param type
     * @return
     */
    public static String getFieldType(String type){
        String str = "";
        if("2".equals(type)){
            str = "int";
        }else if("3".equals(type)){
            str = "float";
        }else if("4".equals(type)){
            str = "date";
        }else if("5".equals(type)) {
            str = "Text";
        }else if("6".equals(type)) {
            str = "datetime";
        }else{
            str = "varchar";
        }
        return str;
    }

    /**
     * 获取字段类型[表对应类型](PostgreSQL)
     * @param type
     * @return
     */
    public static String getFieldTypePg(String type){
        String str = "";
        if("2".equals(type)){
            str = "int4";
        }else if("3".equals(type)){
            str = "real";
        }else if("4".equals(type)){
            str = "date";
        }else if("5".equals(type)) {
            str = "text";
        }else if("6".equals(type)) {
            str = "timestamp";
        }else{
            str = "varchar";
        }
        return str;
    }


    /**
     * 转义SQL字段值中的特殊字符
     * 防止 \ 和 ' 等字符破坏SQL语法
     * @param value 原始值
     * @return 转义后的值
     */
    public static String escapeSqlValue(Object value) {
        if (value == null) {
            //return "null";
            return null;
        }
        return String.valueOf(value)
                .replace("\\", "\\\\")
                .replace("'", "''");
    }

    /**
     * 获取动态查询参数
     * @param req 请求
     * @param fieldList 字段数据
     * @param tableName 需要查询的表名
     * @param selectFields 指定的查询字段
     */
    public static Map<String, Object> getParamMap(HttpServletRequest req, List<PiCustomizeTableField> fieldList, String tableName, String selectFields) {
        //参数
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                paramMap.put(key, values[0]);
            } else {
                paramMap.put(key, values);
            }
        }

        //查询条件
        StringBuilder conditionBuff = new StringBuilder();
        //返回字段
        StringBuilder fieldBuff = new StringBuilder();
        fieldBuff.append(" id ");

        //排序集合
        StringBuilder sortBuff = new StringBuilder();

        //遍历字段
        for (PiCustomizeTableField tableField : fieldList) {
            String fieldName = tableField.getFieldName();
            //过滤查询条件
            if(StrUtil.isNotBlank(paramMap.get(fieldName)+"") && !"null".equals(paramMap.get(fieldName)+"") && tableField.getIsQuery() == 1){
                //sql注入检查
                SqlInjectionUtil.filterContent(paramMap.get(fieldName).toString());
                //4 日期(Date)  6时间(DateTime)
                if("4".equals(tableField.getType()) || "6".equals(tableField.getType())){
                    //时间
                    String[] datas = paramMap.get(fieldName).toString().split(",");
                    if(datas.length == 2){
                        conditionBuff.append(" and ").append(fieldName).append(" >= '").append(datas[0]).append("' ");
                        conditionBuff.append(" and ").append(fieldName).append(" <= '").append(datas[1]).append("' ");
                    }
                }else{
                    conditionBuff.append(" and ").append(fieldName).append(" like '%").append(paramMap.get(fieldName)).append("%' ");
                }
            }
            //获取需要显示的字段
            //获取需要格式化数据(日期/时间/代码)
            if("4".equals(tableField.getType())){
                if(StrUtil.isNotBlank(selectFields)){
                    if(selectFields.contains(fieldName)){
                        //日期
                        fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d') as ").append(fieldName);
                    }
                } else {
                    //日期
                    fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d') as ").append(fieldName);
                }
            }else if("6".equals(tableField.getType())){
                if(StrUtil.isNotBlank(selectFields)){
                    if(selectFields.contains(fieldName)){
                        //时间
                        fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d %H:%i:%s') as ").append(fieldName);
                    }
                } else {
                    //时间
                    fieldBuff.append(" , ").append(" DATE_FORMAT( ").append(fieldName).append(", '%Y-%m-%d %H:%i:%s') as ").append(fieldName);
                }
            }else{
                if(StrUtil.isNotBlank(selectFields)){
                    if(selectFields.contains(fieldName)){
                        //其他类型
                        fieldBuff.append(" , ").append(fieldName);
                    }
                } else {
                    //其他类型
                    fieldBuff.append(" , ").append(fieldName);
                }
            }
            //获取排序集合
            if(1 == tableField.getIsSort()){
                //排序方式
                String sortType = tableField.getSortType() == 1?" asc ":" desc ";
                if(sortBuff.isEmpty()){
                    sortBuff.append(" order by ").append(fieldName).append(sortType);
                }else{
                    sortBuff.append(" , ").append(fieldName).append(sortType);
                }
            }
        }
        //排序为空设置默认值
        if(sortBuff.isEmpty()){
            sortBuff.append(" order by  create_time desc ");
        }

        //查询字段
        paramMap.put("query",fieldBuff.toString());
        //查询条件
        paramMap.put("condition",conditionBuff.toString());
        //查询表
        paramMap.put("tableName", tableName);
        //排序(默认创建时间)
        paramMap.put("sort",sortBuff.toString());
        return paramMap;
    }
}

