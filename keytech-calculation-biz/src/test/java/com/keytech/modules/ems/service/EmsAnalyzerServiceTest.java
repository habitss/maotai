package com.keytech.modules.ems.service;

import com.alibaba.fastjson.JSON;
import com.keytech.modules.ems.service.impl.RefValueUsageCalculator;
import com.keytech.modules.ems.service.impl.TimeSeriesAnalyzer;
import com.keytech.modules.ems.service.impl.UsageCalculator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 能源模块 分析算法测试类
 * @author yepeng
 * @since 2025.05.11
 */
@Slf4j
class EmsAnalyzerServiceTest {

    @Test
    void timeSeriesAnalyzerTest() {
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
        // 2. 执行检测
        TimeSeriesAnalyzer analyzer = new TimeSeriesAnalyzer();
        TimeSeriesAnalyzer.AnalysisResult result = analyzer.analyze(testData, 12);

        log.info("最新正常值: {}", result.latestNormalValue);
        log.info("小时增量: {}/小时", result.valueIncrement);
        assertNotNull(result);
    }

    @Test
    void refValueUsageCalculatorTest() {
        RefValueUsageCalculator service = new RefValueUsageCalculator();
        List<Double> refValues = Arrays.asList(990.0, 990.0, 990.0,110990.0,110990.0,110990.0,110990.0,16.3421,16.3421);
        List<List<Double>> sampleDatas = Arrays.asList(
                //正常增长（结果为：45.2）
                Arrays.asList(990.0, 1000.0, 1010.0, 1010.0, 1010.0, 1019.0, 1027.1, 1035.2),
                //异常连续跳大（结果为：45.2）
                Arrays.asList(990.0, 1000.0, 9999999.0, 9999991.0, 999999.1, 1019.0, 1027.1, 1035.2),
                //首位0打头，中间异常连续跳大（结果为：45.2）
                Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 990.0, 1000.0, 9999999.0, 9999991.0, 999999.1, 1019.0, 1027.1, 1035.2),
                //异常连续跳小（结果为：45.2）
                Arrays.asList(110990.0, 111000.0, 0.00000001, 0.00000001, 0.00000002, 111019.0, 111027.1, 111035.2),
                //首位0打头，中间异常连续跳小（结果为：45.2）
                Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 110990.0, 111000.0, 0.00000001, 0.00000001, 0.00000002, 111019.0, 111027.1, 111035.2),
                //手动清零（结果为：11.389）
                Arrays.asList(110990.0, 111000.0, 0.0, 0.111, 0.3421, 0.8123, 1.2222, 1.5),
                //手动清零（结果为：19.389）
                Arrays.asList(110990.0, 111000.0, 0.0, 12.111, 16.3421, 18.8123, 19.2222, 21.5),
                //全是异常跳大，只有一个正常值（结果为：0）
                Arrays.asList(9999999.0, 9999991.0, 9999999.0, 16.3421, 9999991.0, 9999999.0, 9999991.0, 9999999.0, 9999991.0),
                //全是异常跳小，只有一个正常值（结果为：0）
                Arrays.asList(0.000000001, 0.000000002, 0.000000003, 16.3421, 0.000000004, 0.000000001, 0.000000001, 0.000000001, 0.000000001)
        );
        for(int i = 0; i < sampleDatas.size(); i++) {
            List<Double> data = sampleDatas.get(i);
            log.info(JSON.toJSONString(service.calculateUsage(data, BigDecimal.valueOf(refValues.get(i)))));
        }
        assertNotNull(refValues);
    }

    @Test
    void usageCalculatorTest() {
        UsageCalculator service = new UsageCalculator();
        List<List<Double>> sampleDatas = Arrays.asList(
                //正常增长（结果为：45.2）
                Arrays.asList(990.0, 1000.0, 1010.0, 1010.0, 1010.0, 1019.0, 1027.1, 1035.2),
                //异常连续跳大（结果为：45.2）
                Arrays.asList(990.0, 1000.0, 9999999.0, 9999991.0, 999999.1, 1019.0, 1027.1, 1035.2),
                //首位0打头，中间异常连续跳大（结果为：45.2）
                Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 990.0, 1000.0, 9999999.0, 9999991.0, 999999.1, 1019.0, 1027.1, 1035.2),
                //异常连续跳小（结果为：45.2）
                Arrays.asList(110990.0, 111000.0, 0.00000001, 0.00000001, 0.00000002, 111019.0, 111027.1, 111035.2),
                //首位0打头，中间异常连续跳小（结果为：45.2）
                Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 110990.0, 111000.0, 0.00000001, 0.00000001, 0.00000002, 111019.0, 111027.1, 111035.2),
                //手动清零（结果为：11.389）
                Arrays.asList(110990.0, 111000.0, 0.0, 0.111, 0.3421, 0.8123, 1.2222, 1.5),
                //手动清零（结果为：19.389）
                Arrays.asList(110990.0, 111000.0, 0.0, 12.111, 16.3421, 18.8123, 19.2222, 21.5)
                //全是异常跳大，只有一个正常值（结果为：0）
                //Arrays.asList(0.00000001, 0.00000001, 0.00000001, 16.3421, 0.00000001, 0.00000001, 0.00000001, 0.00000001, 0.00000001)
        );
        for(List<Double> data : sampleDatas) {
            log.info(String.valueOf(service.calculateUsage(data)));
        }
        assertNotNull(sampleDatas);
    }

}