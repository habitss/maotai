package com.keytech.modules.pi.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @FileName TimeRangeUtils
 * @Description
 * @Author HQ
 * @date 2025-10-07
 **/
public class TimeRangeUtils {
    /**
     * 获取时间范围内的每分钟时间集合
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 时间集合，格式为 yyyy-MM-dd HH:mm:ss
     */
    public static List<String> getMinuteTimeRange(String startTime, String endTime) {
        List<String> timeList = new ArrayList<>();

        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析开始时间和结束时间
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        // 遍历每分钟
        LocalDateTime current = start;
        while (!current.isAfter(end)) {
            timeList.add(current.format(formatter));
            current = current.plusMinutes(1);
        }

        return timeList;
    }

    /**
     * 重载方法，支持 LocalDateTime 参数
     */
    public static List<String> getMinuteTimeRange(LocalDateTime start, LocalDateTime end) {
        List<String> timeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime current = start;
        while (!current.isAfter(end)) {
            timeList.add(current.format(formatter));
            current = current.plusMinutes(1);
        }

        return timeList;
    }

    /**
     * 重载方法，返回 LocalDateTime 集合
     */
    public static List<LocalDateTime> getMinuteTimeRangeAsLocalDateTime(String startTime, String endTime) {
        List<LocalDateTime> timeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        LocalDateTime current = start;
        while (!current.isAfter(end)) {
            timeList.add(current);
            current = current.plusMinutes(1);
        }

        return timeList;
    }

    // 测试示例
    public static void main(String[] args) {
        String startTime = "2024-01-15 10:30:00";
        String endTime = "2024-01-15 10:35:00";

        List<String> timeRange = getMinuteTimeRange(startTime, endTime);

        System.out.println("时间范围集合 (yyyy-MM-dd HH:mm:ss):");
        for (String time : timeRange) {
            System.out.println(time);
        }

        // 测试跨天的情况
        System.out.println("\n跨天测试:");
        String crossDayStart = "2024-01-15 23:59:00";
        String crossDayEnd = "2024-01-16 00:02:00";

        List<String> crossDayRange = getMinuteTimeRange(crossDayStart, crossDayEnd);
        for (String time : crossDayRange) {
            System.out.println(time);
        }
    }
}
