package com.keytech.modules.kpi.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 时间工具类
 */
public class DateUtils {

    public static final SimpleDateFormat YEARMONTH_FORMAT = new SimpleDateFormat("yyyyMM");
    public static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static final String DATE_TIME_STRING = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将日期字符串按照 "yyyy-MM-dd" 格式解析为 Date 对象
     * @param dateStr 日期字符串
     * @return 解析后的 Date 对象
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr) throws ParseException {
        return DATE_FORMAT.parse(dateStr);
    }

    /**
     * 将日期字符串按照 "yyyy-MM-dd HH:mm:ss" 格式解析为 Date 对象
     * @param datetimeStr 日期时间字符串
     * @return 解析后的 Date 对象
     * @throws ParseException 解析异常
     */
    public static Date parseDateTime(String datetimeStr) throws ParseException {
        return DATETIME_FORMAT.parse(datetimeStr);
    }

    /**
     * 将 Date 对象按照 "yyyy-MM-dd" 格式格式化为字符串
     * @param date Date 对象
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /**
     * 将 Date 对象按照 "yyyy-MM-dd HH:mm:ss" 格式格式化为字符串
     * @param date Date 对象
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateTime(Date date) {
        return DATETIME_FORMAT.format(date);
    }

    /**
     * 将 Date 对象按照指定格式格式化为字符串
     * @param date Date 对象
     * @param pattern 指定的日期时间格式
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateByPattern(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 将 Date 对象按照指定格式格式化为字符串
     *
     * @param date 需要格式化的 Date 对象
     * @param simpleDateFormat 指定的日期时间格式化器
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateByPattern(Date date, SimpleDateFormat simpleDateFormat) {
        return simpleDateFormat.format(date);
    }


    /**
     * 时间差计算
     * @param startTime
     * @param endTime
     * @return
     */
    public static String calculateTimeDifferenceInHours(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return "";
        }
        long diffInMillis = endTime.getTime() - startTime.getTime();
        double diffInHours = (double) diffInMillis / (1000 * 60 * 60);
//        DecimalFormat df = new DecimalFormat("#.0");
//        return df.format(diffInHours);
        //保留1位小数
        return String.format("%.1f", diffInHours);
    }



    /**
     * 获取两个日期字符串之间的所有月份（包含起始和结束月份）
     * @param startDate 起始日期字符串，格式：yyyy-MM-dd
     * @param endDate 结束日期字符串，格式：yyyy-MM-dd
     * @return 包含所有月份的有序列表（按时间顺序排列）
     */
    public static List<String> getMonthsBetween(String startDate, String endDate) {
        // 解析日期字符串
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        // 获取月份部分
        YearMonth startMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        // 验证起始月份是否小于等于结束月份
        if (startMonth.isAfter(endMonth)) {
            throw new IllegalArgumentException("起始日期不能晚于结束日期");
        }

        // 生成月份流并收集到列表中
        return Stream.iterate(startMonth, ym -> ym.plusMonths(1))
                .takeWhile(ym -> !ym.isAfter(endMonth))
                .map(ym -> ym.format(MONTH_FORMATTER))
                .collect(Collectors.toList());
    }


    /**
     * 年份对应月份(1-12)
     *  [2024,2025]
     *  [2024-01, 2024-02, ..., 2025-11, 2025-12]
     * @param years
     * @return
     */
    public static List<String> generateYearMonthListStream(List<Integer> years) {
        return years.stream()
                .flatMap(year ->
                        java.util.stream.IntStream.rangeClosed(1, 12)
                                .mapToObj(month -> String.format("%d-%02d", year, month))
                )
                .collect(Collectors.toList());
    }


    /**
     * 获取指定年月最后一天的日期字符串。
     *
     * @param yearMonth 年月字符串，格式为"yyyy-MM"
     * @return 指定年月最后一天的日期字符串，格式为"yyyy-MM-dd"；如果解析年月字符串失败，则返回空字符串
     */
    public static String getLastDayOfMonth(String yearMonth) {
        try {
            // 解析输入的年月字符串
            LocalDate date = LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 获取当月最后一天
            LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());

            // 格式化输出
            return lastDayOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return "";
        }
    }


    /**
     * 获取指定时间段内的所有月份列表
     * @param startTime 起始时间
     * @param eddTime 结束时间
     * @return 列表，包含所有月份
     */
    public static List<String> getMonthList(String startTime, String eddTime) {
        List<String> monthList = new ArrayList<>();
        for (LocalDate date = LocalDate.parse(startTime, DATE_FORMATTER);
             date.isBefore(LocalDate.parse(eddTime, DATE_FORMATTER));
             date = date.plusMonths(1)) {
            monthList.add(date.format(MONTH_FORMATTER));
        }
        return monthList;
    }


    /**
     * 将Instant格式化为字符串 '2025-08-16 02:25:00'
     */
    public static String formatInstantToString(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 将Date对象转换为LocalDateTime对象
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
}
