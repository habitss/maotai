package com.keytech.modules.ems.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过给定的一组底数求用量的算法
 * 兼容异常跳大，异常跳小，手动清零的情况
 * @author yepeng
 * @since 2025.05.10
 */
@Component
@Slf4j
public class UsageCalculator {

    private static final BigDecimal MAX_ABSOLUTE = new BigDecimal("10000");
    private static final BigDecimal MAX_RATIO = new BigDecimal("100");
    private static final BigDecimal RESET_THRESHOLD = new BigDecimal("0.1");
    private static final BigDecimal ZERO_THRESHOLD = new BigDecimal("0.001");

    public BigDecimal calculateUsage(List<Double> data) {
        if (data == null || data.size() < 2) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> values = data.stream()
                .map(BigDecimal::valueOf)
                .collect(Collectors.toList());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal currentSeries = BigDecimal.ZERO;
        BigDecimal lastValid = findFirstValid(values);

        if (lastValid == null) {
            return BigDecimal.ZERO;
        }

        int startIndex = values.indexOf(lastValid) + 1;
        for (int i = startIndex; i < values.size(); i++) {
            BigDecimal curr = values.get(i);

            // 重置触发逻辑
            if (lastValid != null && isResetTrigger(lastValid, curr)) {
                total = total.add(currentSeries);
                currentSeries = BigDecimal.ZERO;
                // 保存触发重置时的前一个有效值
                BigDecimal resetPrev = lastValid;
                lastValid = resetHandler(values, i);

                if (lastValid != null) {
                    // 计算触发重置后的第一个有效点与触发前有效值的差
                    BigDecimal postResetDiff = lastValid.subtract(resetPrev);
                    if (isValid(postResetDiff, resetPrev, lastValid)) {
                        total = total.add(postResetDiff);
                    }
                    // 更新i到新有效点的索引
                    i = values.indexOf(lastValid);
                }
                continue;
            }

            // 有效增长计算
            BigDecimal diff = curr.subtract(lastValid);
            if (isValid(diff, lastValid, curr)) {
                currentSeries = currentSeries.add(diff);
                lastValid = curr;
            }
        }

        return total.add(currentSeries).stripTrailingZeros();
    }

    private BigDecimal resetHandler(List<BigDecimal> data, int resetIndex) {
        for (int i = resetIndex + 1; i < data.size(); i++) {
            BigDecimal val = data.get(i);
            if (val.compareTo(ZERO_THRESHOLD) > 0) {
                // 返回重置后的第一个有效值
                return val;
            }
        }
        return null;
    }

    private boolean isResetTrigger(BigDecimal prev, BigDecimal curr) {
        return prev.compareTo(ZERO_THRESHOLD) > 0 &&
                curr.compareTo(prev.multiply(RESET_THRESHOLD)) <= 0;
    }

    private boolean isValid(BigDecimal diff, BigDecimal base, BigDecimal current) {
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (diff.abs().compareTo(MAX_ABSOLUTE) > 0) {
            return false;
        }

        BigDecimal ratio = base.compareTo(BigDecimal.ZERO) != 0 ?
                current.divide(base, 10, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return ratio.compareTo(MAX_RATIO) <= 0;
    }

    private BigDecimal findFirstValid(List<BigDecimal> data) {
        for (BigDecimal val : data) {
            if (val.compareTo(ZERO_THRESHOLD) > 0) {
                return val;
            }
        }
        return null;
    }


    public static void main(String[] args) {
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
    }

}