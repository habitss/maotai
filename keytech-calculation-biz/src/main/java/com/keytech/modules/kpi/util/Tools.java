package com.keytech.modules.kpi.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 工具类
 *
 * @author hzy
 * @date 2023-08-30
 */
public class Tools {

    public static void main(String[] args) {
        String str = "1693372318";

        boolean isDouble = false;
        try {
            double num = Double.parseDouble(str);
            isDouble = true;
        } catch (NumberFormatException e) {
            // 解析失败，字符串不是 double 类型
        }

        System.out.println("Is double? " + isDouble);

        boolean aDouble = isDouble("-0");
    }

    /**
     * 判断是否为 double 类型
     *
     * @param str
     * @return
     */
    public static boolean isDouble(String str) {
        if (isTimestampString(str)) {
            return false;
        }

        try {
            double num = Double.parseDouble(str) + 0.0;
            return true;
        } catch (NumberFormatException e) {
            // 解析失败，字符串不是 double 类型
            return false;
        }
    }

    /**
     * 判断字符串是否为时间戳
     *
     * @param timestampString
     * @return
     */
    public static boolean isTimestampString(String timestampString) {
        return ObjectUtil.isNotEmpty(timestampString) &&
                NumberUtil.isNumber(timestampString) &&
                timestampString.length() == 10;
    }

    /**
     * 时间戳转 LocalDateTime
     *
     * @param timestampString
     * @return
     */
    public static LocalDateTime timestampToLocalDate(String timestampString) {
        try {
            long timestamp = Long.parseLong(timestampString);

            if (timestamp <= 0) return null;

            Instant instant = Instant.ofEpochSecond(timestamp);

            LocalDateTime dateTime = instant.atZone(ZoneId.of("UTC")).toLocalDateTime(); // Convert to UTC LocalDateTime

            return dateTime;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 时间戳转日期字符串
     *
     * @param timestampString
     * @return
     */
    public static String timestampToDateString(String timestampString) {
        try {
            LocalDateTime dateTime = timestampToLocalDate(timestampString);
            return LocalDateTimeUtil.format(dateTime, DatePattern.NORM_DATETIME_PATTERN);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object replaceValue(String[] replace, Object result, boolean multiReplace) {
        if (result == null) {
            return "";
        }
        if (replace == null || replace.length <= 0) {
            return result;
        }
        String temp = String.valueOf(result);
        String backValue = "";
        if (temp.indexOf(",") > 0 && multiReplace) {
            //原值中带有逗号，认为他是多值的
            String[] multiReplaces = temp.split(",");
            for (String str : multiReplaces) {
                backValue = backValue.concat(replaceSingleValue(replace, str) + ",");
            }
            if (backValue.equals("")) {
                backValue = temp;
            } else {
                backValue = backValue.substring(0, backValue.length() - 1);
            }
        } else {
            backValue = replaceSingleValue(replace, temp);
        }
        //update-begin-author:liusq date:20210204 for:字典替换失败提示日志
        if (backValue.equals(temp)) {

        }
        //update-end-author:liusq date:20210204 for:字典替换失败提示日志
        return backValue;
    }

    /**
     * 单值替换 ,若没找到则原值返回
     */
    private static String replaceSingleValue(String[] replace, String temp) {
        String[] tempArr;
        for (int i = 0; i < replace.length; i++) {
            tempArr = replace[i].split("_");
            if (tempArr.length < 2) {
                continue;
            }
            if (temp.equals(tempArr[1])) {
                return tempArr[0];
            }
        }
        return temp;
    }

    /**
     * 将数字转换为对应的字母（0→a，1→b，...）
     * @param num
     * @return
     */
    public static char getLetter(int num) {
        if (num < 0 || num > 25) {
            throw new IllegalArgumentException("输入数字必须在0-25之间");
        }
        return (char) ('a' + num);
    }

}
