package com.keytech.modules.pi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @FileName DateConvertUtil
 * @Description 时间转换工具类
 * @Author HQ
 * @date 2025-11-05
 **/
public class DateConvertUtil {

    // 转换类型常量
    public static final int TO_MINUTES = 1;    // 转换为分钟
    public static final int TO_HOURS = 2;      // 转换为小时
    public static final int TO_DAYS = 3;       // 转换为天数

    // 单位换算常量
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = 86400;

    // 小数位数
    private static final int DECIMAL_PLACES = 4;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");

    /**
     * @Description 时间戳转换成LocalDateTime
     * @param timestampString 时间戳
     * @return
     */
    public static LocalDateTime timestampToLocalDate(Date timestampString) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampString.getTime()), ZoneId.systemDefault());
            return dateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Instant格式化为字符串 '2025-08-16 02:25:00'
     */
    public static String formatInstantToString(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 将秒数转换为指定的时间单位
     *
     * @param seconds 秒数字符串
     * @param formatType 转换类型：1=分钟，2=小时，3=天数
     * @return 转换后的值，保留4位小数。如果输入无效返回null
     */
    public static String convertSeconds(String seconds, int formatType) {
        if (seconds == null || seconds.trim().isEmpty()) {
            return null;
        }
        try {
            double secondsValue = Double.parseDouble(seconds.trim());
            return convertSeconds(secondsValue, formatType);
        } catch (Exception e) {
            // 处理数字格式异常
            return seconds;
        }
    }

    /**
     * 将秒数转换为指定的时间单位
     *
     * @param seconds 秒数值
     * @param formatType 转换类型：1=分钟，2=小时，3=天数
     * @return 转换后的值，保留4位小数
     */
    public static String convertSeconds(double seconds, int formatType) {
        double result;

        switch (formatType) {
            case TO_MINUTES:
                result = seconds / SECONDS_PER_MINUTE;
                break;
            case TO_HOURS:
                result = seconds / SECONDS_PER_HOUR;
                break;
            case TO_DAYS:
                result = seconds / SECONDS_PER_DAY;
                break;
            default:
                throw new IllegalArgumentException("无效的转换类型: " + formatType +
                        "，有效值为：1(分钟), 2(小时), 3(天数)");
        }

        return formatDecimal(result);
    }

    /**
     * 格式化小数，保留4位
     */
    private static String formatDecimal(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    /**
     * 将分钟字符串转换为毫秒（四舍五入）
     * @param minuteStr 分钟字符串，如 "0.41"
     * @return 四舍五入后的毫秒数
     * @throws NumberFormatException 如果字符串格式错误
     */
    public static long convertToMillis(String minuteStr) {
        double minutes = Double.parseDouble(minuteStr);
        double millis = minutes * 60 * 1000;
        return Math.round(millis);
    }

    /**
     * 计算结束时间 - 使用毫秒
     * @param startTime 开始时间
     * @param durationInMillis 持续时间（毫秒）
     * @return 结束时间
     */
    public static Date calculateEndTime(Date startTime, long durationInMillis) {
        if (startTime == null) {
            return null;
        }
        long startMillis = startTime.getTime();
        long endMillis = startMillis + durationInMillis;
        return new Date(endMillis);
    }

    public static void main(String[] args) {
        long start = 60*60*24*2;
        System.out.println(convertSeconds(start+"", 3));
    }
}
