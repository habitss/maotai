package com.keytech.modules.pi.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/**
 * @FileName AdvancedDateTimeCalculator
 * @Description
 * @Author HQ
 * @date 2025-10-05
 **/
public class AdvancedDateTimeCalculator {

    /**
     * 计算Date类型时间段内可以分成多少个6分钟间隔，返回间隔计数数组
     * @param startDate 开始时间 (Date类型)
     * @param endDate 结束时间 (Date类型)
     * @return 间隔计数数组，如 [1, 7, 13, 19, 25, 31]
     */
    public static List<Integer> calculate6MinuteIntervals(Date startDate, Date endDate) {
        List<Integer> intervals = new ArrayList<>();

        if (startDate == null || endDate == null) {
            return intervals;
        }

        long durationMillis = endDate.getTime() - startDate.getTime();
        if (durationMillis <= 0) {
            return intervals;
        }

        long intervalMillis = TimeUnit.MINUTES.toMillis(6);
        int intervalCount = (int) Math.ceil((double) durationMillis / intervalMillis);

        for (int i = 0; i < intervalCount; i++) {
            intervals.add(i * 6 + 1);
        }

        return intervals;
    }

    /**
     * 计算时间间隔内的区间列表（动态间隔）
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param intervalMinutes 间隔分钟数
     * @return 区间编号列表（从1开始）
     */
    public static List<Integer> calculateDynamicIntervals(Date startDate, Date endDate, int intervalMinutes) {
        List<Integer> intervals = new ArrayList<>();

        if (startDate == null || endDate == null || intervalMinutes <= 0) {
            return intervals;
        }

        long durationMillis = endDate.getTime() - startDate.getTime();
        if (durationMillis <= 0) {
            return intervals;
        }

        long intervalMillis = TimeUnit.MINUTES.toMillis(intervalMinutes);
        int intervalCount = (int) Math.ceil((double) durationMillis / intervalMillis);

        for (int i = 0; i < intervalCount; i++) {
            intervals.add(i + 1); // 区间编号从1开始
        }

        return intervals;
    }


    /**
     * 计算Date类型时间段内可以分成多少个指定分钟间隔
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param intervalMinutes 间隔分钟数
     * @return 间隔计数数组
     */
    public static List<Integer> calculateMinuteIntervals(Date startDate, Date endDate, int intervalMinutes) {
        List<Integer> intervals = new ArrayList<>();

        if (startDate == null || endDate == null || intervalMinutes <= 0) {
            return intervals;
        }

        long durationInMillis = endDate.getTime() - startDate.getTime();

        if (durationInMillis <= 0) {
            return intervals;
        }

        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
        int intervalCount = (int) (totalMinutes / intervalMinutes);

        for (int i = 0; i < intervalCount; i++) {
            intervals.add(i * intervalMinutes + 1);
        }

        return intervals;
    }

    /**
     * 获取详细的时间段信息
     */
    public static void printDateRangeInfo(Date startDate, Date endDate) {
        List<Integer> intervals = calculate6MinuteIntervals(startDate, endDate);

        long durationInMillis = endDate.getTime() - startDate.getTime();
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
        long totalHours = TimeUnit.MILLISECONDS.toHours(durationInMillis);

        System.out.println("时间段: " + startDate + " - " + endDate);
        System.out.println("总时长: " + totalHours + "小时 " + (totalMinutes % 60) + "分钟");
        System.out.println("总分钟数: " + totalMinutes);
        System.out.println("6分钟间隔数量: " + intervals.size());
        System.out.println("间隔数组: " + intervals);
        System.out.println("------------------------");
    }

    /**
     * 获取每个间隔对应的具体时间点
     */
    public static List<Date> getIntervalDatePoints(Date startDate, Date endDate, int intervalMinutes) {
        List<Date> datePoints = new ArrayList<>();

        if (startDate == null || endDate == null || intervalMinutes <= 0) {
            return datePoints;
        }

        long durationInMillis = endDate.getTime() - startDate.getTime();
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
        int intervalCount = (int) (totalMinutes / intervalMinutes);

        for (int i = 0; i <= intervalCount; i++) {
            long pointInMillis = startDate.getTime() + TimeUnit.MINUTES.toMillis(i * intervalMinutes);
            datePoints.add(new Date(pointInMillis));
        }

        return datePoints;
    }

}
