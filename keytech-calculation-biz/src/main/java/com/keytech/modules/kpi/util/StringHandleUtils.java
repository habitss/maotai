package com.keytech.modules.kpi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @FileName StringUtils
 * @Description
 * @Author HQ
 * @date 2025-07-31
 **/
public class StringHandleUtils {

    /**
     * 移除字符串中所有换行符
     * @param input 原始字符串
     * @return 清除换行符后的字符串
     */
    public static String removeAllLineBreaks(String input) {
        if (input == null) {
            return null;
        }
        // 同时处理 \n, \r, \r\n 等各种换行符
        return input.replaceAll("\\r|\\n", "");
    }

    /**
     * 提取字符串末尾的数字
     * @param input 输入字符串
     * @return 末尾的数字字符串，如果找不到返回空字符串
     */
    public static String getLastNumber(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        Pattern pattern = Pattern.compile("(\\d+)$");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : "";
    }
}
