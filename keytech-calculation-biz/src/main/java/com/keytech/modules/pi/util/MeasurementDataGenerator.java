package com.keytech.modules.pi.util;


import com.keytech.modules.pi.common.MeasurementData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeasurementDataGenerator {

    // 用于为不同点位生成不同基值的计数器
    private static final AtomicLong pointCounter = new AtomicLong(0);



    /**
     * 按分钟生成点位测量数据
     */
    /**
     * 生成时间区间内，每隔一分钟一个值的测量数据（按 time 升序）
     */
    public static List<MeasurementData> generateMeasurementData(Date startTime, Date endTime, String pointCode) {
        if (startTime == null || endTime == null){
            return new ArrayList<>();
        }
        List<MeasurementData> list = new ArrayList<>();

        // 转换为 Instant
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();

        // 遍历每分钟
        for (Instant t = start; !t.isAfter(end); t = t.plusSeconds(60)) {
            double value = Math.random() * 100; // 模拟随机值，可替换为实际计算逻辑
            list.add(new MeasurementData(t, pointCode, value));
        }

        // 确保按时间升序排序（虽然上面逻辑已经是升序，但这里加保险）
        return list.stream()
                .sorted(Comparator.comparing(MeasurementData::getTime))
                .collect(Collectors.toList());
    }





    /**
     * 生成基础时间序列的MeasurementData列表
     * @param startDate 开始时间 (Date类型)
     * @param endDate 结束时间 (Date类型)
     * @param intervalMinutes 时间间隔（分钟）
     * @param cnlNum 点位编号
     * @return MeasurementData列表
     */
    public static List<MeasurementData> generateMeasurementData(Date startDate, Date endDate,
                                                                int intervalMinutes, String cnlNum) {
        return generateMeasurementData(startDate, endDate, intervalMinutes, cnlNum, null);
    }

    /**
     * 生成带自定义值生成器的MeasurementData列表
     * @param startDate 开始时间 (Date类型)
     * @param endDate 结束时间 (Date类型)
     * @param intervalMinutes 时间间隔
     * @param cnlNum 点位编号
     * @param valueGenerator 值生成函数
     * @return MeasurementData列表
     */
    public static List<MeasurementData> generateMeasurementData(Date startDate, Date endDate,
                                                                int intervalMinutes, String cnlNum,
                                                                Function<LocalDateTime, Double> valueGenerator) {
        List<MeasurementData> result = new ArrayList<>();

        // 将Date转换为LocalDateTime
        LocalDateTime start = toLocalDateTime(startDate);
        LocalDateTime end = toLocalDateTime(endDate);

        LocalDateTime current = start;
        long index = 0;

        while (!current.isAfter(end)) {
            // 转换为Instant格式 (2025-09-21T16:01:00Z)
            Instant instant = current.atZone(ZoneId.systemDefault()).toInstant();

            // 生成值
            Double value;
            if (valueGenerator != null) {
                value = valueGenerator.apply(current);
            } else {
                // 默认使用平滑正弦波
                value = generateDefaultValue(current, start, index);
            }

            MeasurementData data = new MeasurementData(instant, cnlNum, value);
            result.add(data);

            current = current.plusMinutes(intervalMinutes);
            index++;
        }

        return result;
    }

    /**
     * 生成平滑正弦波数据
     */
    public static List<MeasurementData> generateSineWaveData(Date startDate, Date endDate,
                                                             int intervalMinutes, String cnlNum,
                                                             double baseValue, double amplitude, double frequency) {
        return generateMeasurementData(startDate, endDate, intervalMinutes, cnlNum, current -> {
            long minutesFromStart = ChronoUnit.MINUTES.between(
                    toLocalDateTime(startDate), current
            );
            double value = baseValue + amplitude * Math.sin(minutesFromStart * frequency);
            return Math.round(value * 100.0) / 100.0; // 保留两位小数
        });
    }

    /**
     * 生成模拟CPU使用率数据
     */
    public static List<MeasurementData> generateCPUUsageData(Date startDate, Date endDate,
                                                             int intervalMinutes, String cnlNum) {
        return generateMeasurementData(startDate, endDate, intervalMinutes, cnlNum, current -> {
            int hour = current.getHour();
            long minutesFromStart = ChronoUnit.MINUTES.between(
                    toLocalDateTime(startDate), current
            );

            double baseUsage = 40.0;
            double dailyCycle = 15 * Math.sin(hour * Math.PI / 12);
            double hourlyCycle = 8 * Math.cos(minutesFromStart * 0.3);
            double noise = Math.random() * 4 - 2;

            double usage = baseUsage + dailyCycle + hourlyCycle + noise;
            usage = Math.max(5, Math.min(95, usage));
            return Math.round(usage * 10.0) / 10.0; // 保留一位小数
        });
    }



    /**
     * 生成温度数据（每个点位不同）
     */
    public static List<MeasurementData> generateTemperatureData(Date startDate, Date endDate,
                                                                int intervalMinutes, String cnlNum) {
        return generateMeasurementData(startDate, endDate, intervalMinutes, cnlNum, current -> {
            int hour = current.getHour();
            long minutesFromStart = ChronoUnit.MINUTES.between(
                    toLocalDateTime(startDate), current
            );

            // 基于点位名称生成不同的基值，确保每个点位数据不同
            long pointBase = generatePointBase(cnlNum);

            double baseTemp = 20.0 + (pointBase % 10); // 基值在20-29之间变化
            double amplitude = 8.0 + (pointBase % 5);  // 振幅在8-12之间变化
            double frequency = 0.1 + (pointBase % 3) * 0.05; // 频率在0.1-0.25之间变化

            double dailyVariation = amplitude * Math.sin((hour - 6) * Math.PI / 12);
            double smoothNoise = 2 * Math.sin(minutesFromStart * frequency + pointBase);

            double temperature = baseTemp + dailyVariation + smoothNoise;
            return Math.round(temperature * 10.0) / 10.0; // 保留一位小数
        });
    }

    /**
     * 基于点位名称生成唯一基值
     */
    private static long generatePointBase(String cnlNum) {
        // 使用点位名称的hashCode和计数器确保唯一性
        return Math.abs(cnlNum.hashCode()) + pointCounter.getAndIncrement();
    }

    /**
     * 生成压力数据
     */
    public static List<MeasurementData> generatePressureData(Date startDate, Date endDate,
                                                             int intervalMinutes, String cnlNum) {
        return generateMeasurementData(startDate, endDate, intervalMinutes, cnlNum, current -> {
            long minutesFromStart = ChronoUnit.MINUTES.between(
                    toLocalDateTime(startDate), current
            );

            double basePressure = 1013.25; // 标准大气压
            double trend = minutesFromStart * 0.01; // 缓慢上升趋势
            double wave = 5 * Math.sin(minutesFromStart * 0.05); // 周期性波动
            double noise = (Math.random() - 0.5) * 2; // 小范围噪声

            double pressure = basePressure + trend + wave + noise;
            return Math.round(pressure * 100.0) / 100.0; // 保留两位小数
        });
    }

    /**
     * 默认值生成函数 - 平滑正弦波
     */
    private static Double generateDefaultValue(LocalDateTime current, LocalDateTime start, long index) {
        long minutesFromStart = ChronoUnit.MINUTES.between(start, current);
        double value = 50.0 + 30.0 * Math.sin(minutesFromStart * 0.1);
        return Math.round(value * 100.0) / 100.0; // 保留两位小数
    }

    /**
     * 将Date转换为LocalDateTime
     */
    private static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 将LocalDateTime转换为Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 转换为ECharts数据格式
     */
    public static List<Object[]> toEChartsData(List<MeasurementData> measurementData) {
        List<Object[]> result = new ArrayList<>();
        for (MeasurementData data : measurementData) {
            // 格式化为字符串显示
            String timeStr = data.getTime().toString().replace("T", " ").replace("Z", "");
            result.add(new Object[]{timeStr, data.getValue()});
        }
        return result;
    }

    /**
     * 转换为ECharts数据格式（使用原始Instant）
     */
    public static List<Object[]> toEChartsDataWithInstant(List<MeasurementData> measurementData) {
        List<Object[]> result = new ArrayList<>();
        for (MeasurementData data : measurementData) {
            result.add(new Object[]{data.getTime().toString(), data.getValue()});
        }
        return result;
    }

    /**
     * 获取数据统计信息
     */
    public static void printStatistics(List<MeasurementData> dataList) {
        if (dataList.isEmpty()) {
            System.out.println("数据列表为空");
            return;
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        for (MeasurementData data : dataList) {
            double value = data.getValue();
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
        }

        double average = sum / dataList.size();

        System.out.println("=== 数据统计 ===");
        System.out.println("数据条数: " + dataList.size());
        System.out.println("时间范围: " + dataList.get(0).getTime() + " 到 " +
                dataList.get(dataList.size() - 1).getTime());
        System.out.println("最小值: " + min);
        System.out.println("最大值: " + max);
        System.out.println("平均值: " + Math.round(average * 100.0) / 100.0);
    }
}
