package com.keytech.modules.common;

import java.lang.reflect.Field;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/4/21 21:35
 */
public class ReflectionUtil {
    /**
     * 根据字符串获取对象中的字段值
     * @param obj
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        try {
            // 获取对象的Class
            Class<?> clazz = obj.getClass();
            // 根据字段名称获取字段
            Field field = clazz.getDeclaredField(fieldName);
            // 设置为可访问（如果字段是私有的）
            field.setAccessible(true);
            // 返回字段的值
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStringField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Object o = ReflectionUtil.getField(obj, fieldName);
        if(null == o){
            return null;
        }
        return o.toString();
    }

    public static Double getDoubleField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Object o = ReflectionUtil.getField(obj, fieldName);
        if(null == o){
            return 0.00;
        }
        return Double.valueOf(o.toString());
    }
}
