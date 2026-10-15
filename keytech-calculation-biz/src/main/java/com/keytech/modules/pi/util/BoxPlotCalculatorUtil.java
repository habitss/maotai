package com.keytech.modules.pi.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BoxPlotCalculatorUtil {

    /**
     * 计算箱图所需的统计数据
     * @param data 输入数据列表
     * @return 包含箱图数据的Map
     */
    public static List<Double> calculateBoxPlotData(List<Double> data) {
        // 验证输入数据
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        //返回数据
        List<Double> result = new ArrayList<>();
        // 排序数据
        List<Double> sortedData = new ArrayList<>(data);
        Collections.sort(sortedData);
        //数据长度
        int n = sortedData.size();
        //使用依赖库
        DescriptiveStatistics stats = new DescriptiveStatistics();
        sortedData.forEach(stats::addValue);
        //下四分位数（q1）
        double q1 = stats.getPercentile(25);
        //中位数
        double median = stats.getPercentile(50);
        //上四分位数（q3）
        double q3 = stats.getPercentile(75);
        //最小值
        double min = sortedData.get(0);
        //最大值
        double max = sortedData.get(n - 1);

        //添加顺序为最小值、下四分位数Q1、中位数、上四分位数Q3、最大值
        result.add(roundValue(min));
        result.add(roundValue(q1));
        result.add(roundValue(median));
        result.add(roundValue(q3));
        result.add(roundValue(max));
        return result;
    }

    private static double roundValue(Double value) {
        BigDecimal bd = new BigDecimal(value.toString());
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
