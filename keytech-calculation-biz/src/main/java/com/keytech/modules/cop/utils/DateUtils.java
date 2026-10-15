package com.keytech.modules.cop.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName DateUtils
 * @Description 时间日期工具类
 * @createTime 2025年04月12日 21:49:00
 */
public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static final String DATE_MONTH_DAY = "M.d";

    /**
     * String日期转Date
     * @param dateStr
     * @return
     */
    public static Date stringDate2Date(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date stringDateTime2Date(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        LocalDate localDate = LocalDate.parse(dateTimeStr, formatter);
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    // 转换Date到LocalDate
    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static String getMonthMap(int month){
        Map<Integer,String> map = new HashMap<>();
        map.put(1, "Jan");
        map.put(2, "Feb");
        map.put(3, "Mar");
        map.put(4, "Apr");
        map.put(5, "May");
        map.put(6, "Jun");
        map.put(7, "Jul");
        map.put(8, "Aug");
        map.put(9, "Sep");
        map.put(10, "Oct");
        map.put(11, "Nov");
        map.put(12, "Dec");
        return map.get(month);
    }


    public static List<Integer> getMonth(Date start, Date end) {
            // 解析日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.systemDefault());
            LocalDate startDate = LocalDate.parse(formatter.format(start.toInstant()));
            LocalDate endDate = LocalDate.parse(formatter.format(end.toInstant()));

            // 转换为年月对象
            YearMonth startYm = YearMonth.from(startDate);
            YearMonth endYm = YearMonth.from(endDate);

            // 检查时间顺序
            if (startYm.isAfter(endYm)) {
                throw new IllegalArgumentException("开始时间不能晚于结束时间");
            }

            // 生成月份列表（int 格式）
            List<Integer> months = new ArrayList<>();
            for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                int monthValue =ym.getMonthValue();
                months.add(monthValue);
            }
        return months;
    }

    public static Date getPreviousHour(Date date) {
        if (date == null) return null;
        Instant instant = date.toInstant();
        Instant newInstant = instant.minus(1, ChronoUnit.HOURS); // 减去1小时
        return Date.from(newInstant);
    }


    public static int getMonthByDate(Date date) {
        // 1. 将Date转换为Instant
        Instant instant = date.toInstant();

        // 2. 转换为系统默认时区的ZonedDateTime
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        // 或者直接转换为LocalDate
        LocalDate localDate = zonedDateTime.toLocalDate();

        // 3. 获取月份的数字（1-12）
        return  localDate.getMonthValue();
    }

    /**
     * 判断当前日期是否是今天
     * @param date
     * @return
     */
    public static boolean isToday(Date date) {
        // 转换为系统默认时区的LocalDate
        LocalDate inputDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return inputDate.isEqual(LocalDate.now());
    }

    /**
     * 判断当前输入时间是否是半小时之前
     * @param time
     * @return
     */
    public static boolean isHalfHour(String time) {
        // 解析输入时间为LocalTime对象
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime inputTime = LocalTime.parse(time, formatter);

        // 获取当前时间（不含日期）
        LocalTime currentTime = LocalTime.now();

        // 计算输入时间+30分钟（自动处理跨天）
        LocalTime thresholdTime = inputTime.plusMinutes(30);

        // 判断当前时间是否超过阈值
        return currentTime.isBefore(thresholdTime) || currentTime.equals(thresholdTime);
    }

    public static Date addHalfHour(String timeStr) throws ParseException {
        // 创建日期格式化器（注意：12小时制需要添加a表示AM/PM，但输入没有提供，因此按24小时制处理更安全）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // 将输入字符串直接按24小时制解析（避免12小时制歧义）
        Date date = sdf.parse(timeStr); // 确保格式兼容

        // 使用Calendar增加30分钟
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, 30); // 增加30分钟

        return calendar.getTime();
    }

    public static String date2String(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static Date string2Date(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return Date.from(LocalDate.parse(dateStr, formatter).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取当天日期
     * @return
     */
    public static String getCurrentDateString() {
        LocalDate now = LocalDate.now();
        return now.toString();
    }

    public static Date getCurrentDate() {
        LocalDate now = LocalDate.now();
        return localDate2Date(now);
    }

    /**
     * 获取当前年份
     * @return
     */
    public static int getCurrentYear() {
        LocalDate now = LocalDate.now();
        return now.getYear();
    }

    /**
     * 获取当前月份
     * @return
     */
    public static int getCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue();
    }

    public static int getCurrentOfWeek() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 定义 ISO 周标准（周一为第一天，第一周至少4天）
        WeekFields weekFields = WeekFields.ISO;

        // 获取当前周数
        return currentDate.get(weekFields.weekOfYear());
    }

    public static int getCurrentOfWeek(LocalDate date) {

        // 定义 ISO 周标准（周一为第一天，第一周至少4天）
        WeekFields weekFields = WeekFields.ISO;

        // 获取当前周数
        return date.get(weekFields.weekOfYear());
    }

    /**
     * 获取当前日期
     * @return
     */
    public static int getCurrentDay() {
        LocalDate now = LocalDate.now();
        return now.getDayOfMonth();
    }

    /**
     * 获取当月第一天日期
     * @return
     */
    public static String getFirstDayOfMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        return firstDayOfMonth.toString();
    }

    /**
     * 获取当月最后一天日期
     * @return
     */
    public static String getLastDayOfMonth() {
        LocalDate now = LocalDate.now();
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return lastDayOfMonth.toString();
    }

    /**
     * 获取一年的第一天日期
     * @return
     */
    public static String getFirstDayOfYear(int year) {
        LocalDate firstDay = Year.of(year).atDay(1);
        return firstDay.toString();
    }

    /**
     * 获取一年的最后一天日期
     * @return
     */
    public static String getLastDayOfYear(int year) {
        LocalDate lastDay = YearMonth.of(year, 12).atEndOfMonth();
        return lastDay.toString();
    }

    /**
     * LocalDate转Date
     * @param localDate
     * @return
     */
    public static Date localDate2Date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate字符串转Date
     * @param localDate
     * @return
     */
    public static Date localDate2Date(String localDate) {
        return localDate2Date(LocalDate.parse(localDate));
    }

    /**
     * 获取当前日期是当年的第几周
     * @return
     */
    public static int getDayOfWeek(){
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 定义周的开始（例如周一）和结束（例如周日）
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        // 获取当前日期是当年的第几周
        return today.get(weekFields.weekOfWeekBasedYear());
    }

    /**
     * 根据周数和星期获取日期
     * @param weekNumber
     * @param dayOfWeek
     * @return
     */
    public static LocalDate getDateByWeekAndDay(int weekNumber, DayOfWeek dayOfWeek) {
        int year = LocalDate.now().getYear();
        String formattedDate = String.format("%d-W%02d-%d", year, weekNumber, dayOfWeek.getValue());
        return LocalDate.parse(formattedDate, DateTimeFormatter.ISO_WEEK_DATE);
    }

    /**
     * 获取指定年份的最大周数
     * @param year
     * @return
     */
    public static int getMaxWeekOfYear(int year) {
        LocalDate date = LocalDate.of(year, 12, 31);
        WeekFields weekFields = WeekFields.ISO;
        // 确保日期属于当前年的周
        while (date.get(weekFields.weekBasedYear()) > year) {
            date = date.minusWeeks(1);
        }
        return date.get(weekFields.weekOfYear());
    }

    /**
     * 根据年月周和星期获取日期
     * @param year
     * @param month
     * @param week
     * @param dayOfWeek
     * @return
     */
    public static LocalDate getDateFromMonthWeekAndDay(int year, int month, int week, DayOfWeek dayOfWeek) {
        LocalDate baseDate = LocalDate.of(year, month, 1);
        // 使用 TemporalAdjusters 定位月份内的第 N 个星期几
        return baseDate.with(TemporalAdjusters.dayOfWeekInMonth(week, dayOfWeek));
    }

    /**
     * 获取该日期是该月的第几周
     * @param year
     * @param week
     * @return
     */
    public static int getMonthOfWeek(int year, int week){
        // 使用系统默认的地区设置，也可以指定如 Locale.US 或 Locale.CHINA
        Locale locale = Locale.getDefault();
        // 获取当前地区的周字段设置
        WeekFields weekFields = WeekFields.of(locale);

        // 计算全年周数对应的日期
        LocalDate date = LocalDate.of(year, 1, 1) // 从年初开始
                .plusWeeks(week - 1); // 加上周数以找到确切的日期

        // 获取该日期是该月的第几周
        return date.get(weekFields.weekOfMonth());
    }

    /**
     * 解析周数字符串
     * @param input
     */
    public static int parseWeekNumber(String input) {
        // 定义正则表达式匹配模式
        Pattern pattern = Pattern.compile("^(\\d{4})-(\\d{1,2})周$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        } else {
            throw new IllegalArgumentException("格式不正确 - " + input);
        }
    }

    public static Date parseDate(String input) {
        // 解析字符串为LocalDateTime
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(input, inputFormatter);

        // 增加10分钟
        LocalDateTime newTime = localDateTime.plusMinutes(10);

        // 转换为Date（需要指定时区）
        Date date = Date.from(newTime.atZone(ZoneId.systemDefault()).toInstant());

        return date;
    }

    public static String getWeekByDate(String date){
        LocalDate parsedDate = LocalDate.parse(date);
        return  parsedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
    }

    public static List<Integer> getWeekNumberByDate(Date start,Date end){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.systemDefault());
        LocalDate startDate = LocalDate.parse(formatter.format(start.toInstant()));
        LocalDate endDate = LocalDate.parse(formatter.format(end.toInstant()));


        YearMonth startYm = YearMonth.from(startDate);
        YearMonth endYm = YearMonth.from(endDate);

        // 确保开始年月不晚于结束年月
        if (startYm.isAfter(endYm)) {
            YearMonth temp = startYm;
            startYm = endYm;
            endYm = temp;
        }

        List<YearMonth> months = new ArrayList<>();
        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            if (ym.getYear() == getCurrentYear()) {
                months.add(ym);
            }
        }
        return months.stream()
                .map(YearMonth::getMonthValue)
                .collect(Collectors.toList());
    }


    public static void main(String[] args) {
        String startStr = "2025-05-19";
        String endStr = "2026-05-21";

//        LocalDate startDate = LocalDate.parse(startStr);
//        LocalDate endDate = LocalDate.parse(endStr);
//
//        YearMonth startYm = YearMonth.from(startDate);
//        YearMonth endYm = YearMonth.from(endDate);
//
//        // 确保开始年月不晚于结束年月
//        if (startYm.isAfter(endYm)) {
//            YearMonth temp = startYm;
//            startYm = endYm;
//            endYm = temp;
//        }
//
//        List<YearMonth> months = new ArrayList<>();
//        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
//            if (ym.getYear() == 2025) {
//                months.add(ym);
//            }
//        }
//
//        // 格式化为字符串列表
//        List<Integer> result = months.stream()
//                .map(YearMonth::getMonthValue)
//                .collect(Collectors.toList());

        try {
            System.out.println(addHalfHour("2025-05-21 12:12"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
