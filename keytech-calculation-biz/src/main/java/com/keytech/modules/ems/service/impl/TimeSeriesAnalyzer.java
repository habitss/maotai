package com.keytech.modules.ems.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 时序数据分析，获取最新有效值及小时增量值
 * @since 2025.05.11
 */
@Component
@Slf4j
public class TimeSeriesAnalyzer {
    // 可配置参数
    private static final double IQR_MULTIPLIER = 1.5;
    private static final int RESET_ANOMALY_STREAK = 5;
    private static final int MAX_SAMPLE_SPAN = 60;
    private static final int SAMPLE_BLOCKS = 10;
    // IQR计算所需最小数据点
    private static final int MIN_POINTS_FOR_IQR = 4;

    public static class RegressionResult {
        public final BigDecimal slope;
        public final BigDecimal intercept;
        public final BigDecimal valueIncrement;

        public RegressionResult(BigDecimal slope, BigDecimal intercept, int grainMinutes) {
            this.slope = slope.setScale(10, RoundingMode.HALF_UP);
            this.intercept = intercept.setScale(10, RoundingMode.HALF_UP);
            this.valueIncrement = slope.multiply(BigDecimal.valueOf(grainMinutes))
                    .setScale(4, RoundingMode.HALF_UP);
        }
    }

    /**
     * 分析线性数组
     * @param rawData 已按时间顺序排好的数组
     * @param grainMinutes 用量粒度 小时-12 半小时-6
     */
    public AnalysisResult analyze(List<Double> rawData, int grainMinutes) {
        if (rawData == null || rawData.isEmpty()) {
            log.warn("输入数据为空，返回默认值");
            return new AnalysisResult(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<BigDecimal> filteredData = preprocessData(rawData);

        // 预处理后数据检查
        if (filteredData.isEmpty()) {
            BigDecimal lastRawValue = BigDecimal.valueOf(rawData.get(rawData.size()-1));
            log.warn("过滤后数据为空，使用原始数据最后一个值: {}", lastRawValue);
            return new AnalysisResult(lastRawValue, BigDecimal.ZERO);
        }

        RegressionResult regression = robustLinearRegression(filteredData, grainMinutes);
        BigDecimal result = dynamicDetection(filteredData, regression);
        return new AnalysisResult(result, regression.valueIncrement);
    }

    // 数据预处理（修复排序问题）
    private List<BigDecimal> preprocessData(List<Double> rawData) {
        // 原始数据转换
        List<BigDecimal> original = rawData.stream()
                .map(d -> BigDecimal.valueOf(d).setScale(10, RoundingMode.HALF_UP))
                .toList();

        // 数据量不足直接返回
        if (original.size() < MIN_POINTS_FOR_IQR) {
            return new ArrayList<>(original);
        }

        // 创建排序副本用于计算IQR
        List<BigDecimal> sorted = new ArrayList<>(original);
        Collections.sort(sorted);

        // 计算IQR
        int q1Index = sorted.size() / 4;
        int q3Index = (3 * sorted.size()) / 4;
        BigDecimal q1 = sorted.get(q1Index);
        BigDecimal q3 = sorted.get(q3Index);
        BigDecimal iqr = q3.subtract(q1);
        BigDecimal lowerBound = q1.subtract(iqr.multiply(BigDecimal.valueOf(IQR_MULTIPLIER)));
        BigDecimal upperBound = q3.add(iqr.multiply(BigDecimal.valueOf(IQR_MULTIPLIER)));

        // 过滤异常值（保持原始顺序）
        return original.stream()
                .filter(d -> d.compareTo(lowerBound) >= 0 && d.compareTo(upperBound) <= 0)
                .toList();
    }

    // 增强的鲁棒回归（修复索引问题）
    private RegressionResult robustLinearRegression(List<BigDecimal> points, int grainMinutes) {
        // 空数据检查
        if (points.isEmpty()) {
            return new RegressionResult(BigDecimal.ZERO, BigDecimal.ZERO, grainMinutes);
        }

        // 单点数据返回
        if (points.size() == 1) {
            return new RegressionResult(BigDecimal.ZERO, points.get(0), grainMinutes);
        }

        List<BigDecimal> slopes = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // 动态分块
        int totalPoints = points.size();
        int blocks = Math.min(SAMPLE_BLOCKS, totalPoints);
        int blockSize = (int) Math.ceil((double) totalPoints / blocks);

        for (int block = 0; block < blocks; block++) {
            int start = block * blockSize;
            int end = Math.min(start + blockSize, totalPoints);

            if (start >= end) {
                continue;
            }

            List<BigDecimal> segment = points.subList(start, end);
            int segmentSize = segment.size();

            for (int k = 0; k < 100; k++) {
                if (segmentSize < 2) {
                    break;
                }

                int idx1 = rand.nextInt(segmentSize);
                int maxStep = Math.min(MAX_SAMPLE_SPAN, segmentSize - idx1 - 1);
                if (maxStep <= 0) {
                    continue;
                }

                int idx2 = idx1 + rand.nextInt(maxStep) + 1;
                if (idx2 >= segmentSize) {
                    idx2 = segmentSize - 1;
                }

                BigDecimal deltaY = segment.get(idx2).subtract(segment.get(idx1));
                int deltaIndex = idx2 - idx1;
                if (deltaIndex == 0) {
                    continue;
                }

                BigDecimal slope = deltaY.divide(BigDecimal.valueOf(deltaIndex), 10, RoundingMode.HALF_UP);
                slopes.add(slope);
            }
        }

        // 斜率计算
        BigDecimal medianSlope = slopes.isEmpty() ? BigDecimal.ZERO : median(slopes);

        // 截距计算（使用显式索引）
        List<BigDecimal> intercepts = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            BigDecimal predicted = medianSlope.multiply(BigDecimal.valueOf(i));
            BigDecimal intercept = points.get(i).subtract(predicted);
            intercepts.add(intercept);
        }

        return new RegressionResult(medianSlope, median(intercepts), grainMinutes);
    }

    // 动态检测（含安全修复）
    private BigDecimal dynamicDetection(List<BigDecimal> points, RegressionResult regression) {
        if (points.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算残差
        BigDecimal[] residuals = new BigDecimal[points.size()];
        for (int i = 0; i < points.size(); i++) {
            BigDecimal predicted = regression.slope.multiply(BigDecimal.valueOf(i))
                    .add(regression.intercept);
            residuals[i] = points.get(i).subtract(predicted).abs();
        }

        int anomalyStreak = 0;
        BigDecimal lastNormal = points.get(0);
        int currentWindowStart = 0;

        for (int i = 1; i < residuals.length; i++) {
            // 动态窗口
            int windowSize = i - currentWindowStart;
            if (windowSize < 3) { // 窗口太小跳过
                if (residuals[i].compareTo(residuals[i-1].multiply(BigDecimal.valueOf(3))) > 0) {
                    anomalyStreak++;
                } else {
                    lastNormal = points.get(i);
                    anomalyStreak = 0;
                }
                continue;
            }

            // 窗口数据
            List<BigDecimal> window = new ArrayList<>(windowSize);
            for (int j = currentWindowStart; j < i; j++) {
                window.add(residuals[j]);
            }

            // 阈值计算
            BigDecimal medianResidual = median(window);
            BigDecimal mad = medianAbsoluteDeviation(window, medianResidual);
            BigDecimal threshold = medianResidual.add(mad.multiply(BigDecimal.valueOf(3)));

            // 异常判断
            if (residuals[i].compareTo(threshold) > 0) {
                anomalyStreak++;
                if (anomalyStreak >= RESET_ANOMALY_STREAK) {
                    currentWindowStart = Math.max(0, i - 10);
                    anomalyStreak = 0;
                }
            } else {
                lastNormal = points.get(i);
                anomalyStreak = 0;
            }
        }

        return lastNormal.setScale(4, RoundingMode.HALF_UP);
    }

    // 辅助方法（优化空处理）
    private BigDecimal median(List<BigDecimal> data) {
        if (data.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> sorted = new ArrayList<>(data);
        Collections.sort(sorted);

        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(mid);
        } else {
            return sorted.get(mid).add(sorted.get(mid - 1))
                    .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal medianAbsoluteDeviation(List<BigDecimal> data, BigDecimal median) {
        if (data.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> deviations = data.stream()
                .map(d -> d.subtract(median).abs())
                .sorted()
                .toList();

        return median(deviations);
    }

    // 结果封装
    public static class AnalysisResult {
        public final BigDecimal latestNormalValue;
        public final BigDecimal valueIncrement;

        public AnalysisResult(BigDecimal latest, BigDecimal increment) {
            this.latestNormalValue = latest;
            this.valueIncrement = increment;
        }
    }

    // 示例用法
    public static void main(String[] args) {
        // 1. 生成测试数据（1000个点，含5%异常值）
        List<Double> testData = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            double baseValue = i * 0.1;
            // 5%异常概率
            if (Math.random() < 0.05) {
                baseValue = (Math.random() > 0.5 ? 99999999 : 0.000000011);
            }
            // 添加微小噪声
            testData.add(baseValue + (Math.random() * 0.02));
        }
        log.info(String.valueOf(testData));
        //执行检测
        TimeSeriesAnalyzer analyzer = new TimeSeriesAnalyzer();
        AnalysisResult result = analyzer.analyze(testData, 12);

        log.info("最新正常值: {}", result.latestNormalValue);
        log.info("增量: {}", result.valueIncrement);

    }
}