package com.keytech.modules.ems.service.impl;

import com.alibaba.fastjson.JSON;
import com.keytech.modules.ems.constants.CollectErrorTypeEnum;
import com.keytech.modules.ems.vo.CalcUsageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于参考值过滤异常跳大值通过给定底数求用量的算法
 * 兼容异常跳大，异常跳小，手动清零的情况
 *
 * 主要功能：
 * 1. 过滤异常跳大值（超过参考值10倍）
 * 2. 检测并处理手动清零操作
 * 3. 计算有效用量累计值
 * 4. 识别异常类型（跳大、跳小、清零等）
 *
 * @author yepeng
 * @since 2025.05.10
 */
@Component
@Slf4j
public class RefValueUsageCalculator {
    // 最大允许单次增量（绝对值限制）
    private static final BigDecimal MAX_ABSOLUTE = new BigDecimal("10000");
    // 相邻数据最大增长倍数（比率限制）
    private static final BigDecimal MAX_RATIO = new BigDecimal("100");
    // 重置触发阈值（当前值<=前值的10%时认为发生重置）
    private static final BigDecimal RESET_THRESHOLD = new BigDecimal("0.1");
    // 零值阈值（小于此值视为0）
    private static final BigDecimal ZERO_THRESHOLD = new BigDecimal("0.001");
    // 参考值最大倍数（当前值超过参考值此倍数视为异常）
    private static final BigDecimal MAX_REF_RATIO = new BigDecimal("10");

    /**
     * 计算用量主方法
     * @param data 原始数据列表（Double类型）
     * @param initReference 初始参考值
     * @return CalcUsageResult 包含总用量、最后一个有效底数和异常类型
     */
    public CalcUsageResult calculateUsage(List<Double> data, BigDecimal initReference) {
        // 基础校验：数据不能为空且至少2个点
        if (data == null || data.size() < 2) {
            return new CalcUsageResult(BigDecimal.ZERO, BigDecimal.ZERO, CollectErrorTypeEnum.NULL_DATA_ERROR.getVal());
        }

        // 转换为BigDecimal列表便于精确计算
        List<BigDecimal> values = data.stream().map(BigDecimal::valueOf).toList();
        // 累计总量
        BigDecimal total = BigDecimal.ZERO;
        // 当前连续增长序列值
        BigDecimal currentSeries = BigDecimal.ZERO;
        // 异常类型标记
        Integer anomalyType = CollectErrorTypeEnum.NORMAL.getVal();
        // 是否发生过重置
        boolean hasReset = false;

        // 初始化第一个有效值（同时过滤异常跳大）
        BigDecimal lastValid = findFirstValid(values, initReference);
        // 动态参考值（随数据演进更新）
        BigDecimal dynamicRef = initReference;
        BigDecimal historicalPeak = lastValid != null ? lastValid : BigDecimal.ZERO;

        // 如果找不到有效初始值，进行全零或全负异常检测
        if (lastValid == null) {
            return detectFullAnomaly(values, initReference);
        }

        // 从第一个有效值后开始处理
        int startIndex = values.indexOf(lastValid) + 1;
        for (int i = startIndex; i < values.size(); i++) {
            BigDecimal curr = values.get(i);

            // 重置条件检测（当前值 <= 前值10%）
            if (isResetTrigger(lastValid, curr)) {
                hasReset = true;
                // 累计当前序列值
                total = total.add(currentSeries);
                // 重置当前序列
                currentSeries = BigDecimal.ZERO;
                // 寻找重置后的新基准值
                BigDecimal resetPrev = lastValid;
                // 修复：从下一个位置开始寻找
                BigDecimal newBase = findPostResetValue(values, i, dynamicRef);
                if (newBase != null) {
                    int newBaseIndex = values.indexOf(newBase);
                    // 防止死循环：确保新的索引大于当前位置
                    if (newBaseIndex <= i) {
                        log.warn("发现死循环风险: i={}, newBaseIndex={}, 跳过此值", i, newBaseIndex);
                        // 跳过这个值，继续处理下一个
                        continue;
                    }

                    // 判断重置类型
                    if (newBase.compareTo(historicalPeak) > 0) {
                        anomalyType = CollectErrorTypeEnum.JUMP_MIN_ERROR.getVal();
                    } else {
                        anomalyType = CollectErrorTypeEnum.MANUAL_ZERO_ERROR.getVal();
                    }
                    historicalPeak = newBase;

                    // 更新参考值并处理重置后的差值
                    dynamicRef = newBase;
                    BigDecimal postDiff = newBase.subtract(resetPrev);
                    if (isValid(postDiff, resetPrev, newBase, dynamicRef)) {
                        total = total.add(postDiff);
                    }

                    // 跳转到新基准值位置 使用正确的索引
                    i = newBaseIndex;
                    lastValid = newBase;
                    continue;
                } else {
                    // 找不到有效新基准值，终止处理
                    break;
                }
            }

            // 检测异常跳小
            if (curr.compareTo(lastValid.multiply(new BigDecimal("0.01"))) < 0) {
                anomalyType = CollectErrorTypeEnum.JUMP_MIN_ERROR.getVal();
            }

            // 计算差值
            BigDecimal diff = curr.subtract(lastValid);
            if (isValid(diff, lastValid, curr, dynamicRef)) {
                currentSeries = currentSeries.add(diff);
                lastValid = curr;
                dynamicRef = curr;

                if (curr.compareTo(historicalPeak) > 0) {
                    historicalPeak = curr;
                }
            } else if (curr.compareTo(dynamicRef.multiply(MAX_REF_RATIO)) > 0) {
                anomalyType = CollectErrorTypeEnum.JUMP_MAX_ERROR.getVal();
            }
        }

        // 最终处理
        if (CollectErrorTypeEnum.NORMAL.getVal() == anomalyType && hasReset) {
            anomalyType = lastValid.compareTo(historicalPeak) < 0 ?
                    CollectErrorTypeEnum.MANUAL_ZERO_ERROR.getVal() :
                    CollectErrorTypeEnum.JUMP_MIN_ERROR.getVal();
        }

        BigDecimal resultTotal = total.add(currentSeries).setScale(6, RoundingMode.HALF_UP);
        BigDecimal resultPeak = historicalPeak.setScale(6, RoundingMode.HALF_UP);

        log.debug("计算完成: 总量={}, 峰值={}, 异常类型={}",
                resultTotal, resultPeak, anomalyType);

        return new CalcUsageResult(resultTotal, resultPeak, anomalyType);
    }

    /**
     * 寻找重置后的有效值（用于处理清零后的新基准值）
     * @param data 数据列表
     * @param startIdx 开始查找的索引
     * @param ref 当前参考值
     * @return 找到的有效值或null
     */
    private BigDecimal findPostResetValue(List<BigDecimal> data, int startIdx, BigDecimal ref) {
        for (int i = startIdx + 1; i < data.size(); i++) {
            BigDecimal val = data.get(i);
            if (val.compareTo(ZERO_THRESHOLD) > 0 &&
                    val.compareTo(ref.multiply(MAX_REF_RATIO)) <= 0) {
                return val;
            }
        }
        return null;
    }

    /**
     * 全量异常检测（当找不到初始有效值时使用）
     * @param values 数据列表
     * @param ref 参考值
     * @return 包含异常类型的结果
     */
    private CalcUsageResult detectFullAnomaly(List<BigDecimal> values, BigDecimal ref) {
        // 确定异常类型优先级：全零错误 > 全负错误 > 跳大 > 跳小 > 全零错误
        Integer type = CollectErrorTypeEnum.ALL_ZERO_ERROR.getVal();

        //判断这组数是否全部小于零
        boolean hasNegative = values.stream().allMatch(v -> v.compareTo(BigDecimal.ZERO) < 0);
        if (hasNegative) {
            type = CollectErrorTypeEnum.ALL_NEGATE_ERROR.getVal();
            return new CalcUsageResult(BigDecimal.ZERO, BigDecimal.ZERO, type);
        }

        // 检查是否存在超大值（超过参考值10倍）
        boolean hasHuge = values.stream().anyMatch(v -> v.compareTo(ref.multiply(MAX_REF_RATIO)) > 0);

        // 检查是否存在过小值（大于零但小于参考值1%）
        boolean hasTiny = values.stream().anyMatch(v -> v.compareTo(ZERO_THRESHOLD) > 0 && v.compareTo(ref.multiply(new BigDecimal("0.01"))) < 0);

        if (hasHuge) {
            type = CollectErrorTypeEnum.JUMP_MAX_ERROR.getVal();
        }
        if (hasTiny) {
            type = CollectErrorTypeEnum.JUMP_MIN_ERROR.getVal();
        }

        return new CalcUsageResult(BigDecimal.ZERO, BigDecimal.ZERO, type);
    }

    /**
     * 寻找首个有效值
     * @param data 数据列表
     * @param reference 参考值
     * @return 第一个有效的值或null
     */
    private BigDecimal findFirstValid(List<BigDecimal> data, BigDecimal reference) {
        for (BigDecimal val : data) {
            // 有效条件：大于零阈值且不超过参考值10倍
            if (val.compareTo(ZERO_THRESHOLD) > 0 &&
                    val.compareTo(reference.multiply(MAX_REF_RATIO)) <= 0) {
                return val;
            }
        }
        return null;
    }

    /**
     * 重置触发条件判断
     * @param prev 前一个值
     * @param curr 当前值
     * @return 是否触发重置（当前值 <= 前值的10%）
     */
    private boolean isResetTrigger(BigDecimal prev, BigDecimal curr) {
        return prev.compareTo(ZERO_THRESHOLD) > 0 &&
                curr.compareTo(prev.multiply(RESET_THRESHOLD)) <= 0;
    }

    /**
     * 数据有效性校验方法（同时检测异常跳大值）
     *
     * 该方法用于校验数据的有效性，主要检查以下几个方面：
     * 1. 是否为负增长
     * 2. 单次增量是否超过最大绝对值限制
     * 3. 相邻数据增长率是否超过最大比率
     * 4. 当前值是否超过参考值的最大倍数
     *
     * @param diff 当前值与前一值的差值（curr - prev）
     * @param prev 前一个有效值
     * @param curr 当前待校验的值
     * @param reference 动态参考值（通常为前一有效值或基准值）
     * @return boolean 数据是否有效（true表示有效，false表示无效）
     */
    private boolean isValid(BigDecimal diff, BigDecimal prev, BigDecimal curr, BigDecimal reference) {
        // 1. 负增长检查：差值小于等于0视为无效
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 2. 单次增量绝对值检查：超过MAX_ABSOLUTE（10000）视为无效
        if (diff.abs().compareTo(MAX_ABSOLUTE) > 0) {
            return false;
        }

        // 3. 相邻数据增长率检查：
        //    - 如果前值不为0，计算增长率（当前值/前值）
        //    - 如果前值为0，则比率为0（避免除零错误）
        //    - 增长率超过MAX_RATIO（100倍）视为无效
        BigDecimal ratio = prev.compareTo(BigDecimal.ZERO) != 0 ? curr.divide(prev, 10, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        if (ratio.compareTo(MAX_RATIO) > 0) {
            return false;
        }

        // 4. 参考值检查：当前值不超过参考值的MAX_REF_RATIO（10）倍
        //    - 这是最后一道校验，只有通过前面所有检查才会执行
        return curr.compareTo(reference.multiply(MAX_REF_RATIO)) <= 0;
    }

    public static void main(String[] args) {
        RefValueUsageCalculator service = new RefValueUsageCalculator();
        List<Double> refValues = Arrays.asList(990.0, 990.0, 990.0,110990.0,110990.0,110990.0,110990.0,16.3421,16.3421,-5738.0);
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
                Arrays.asList(0.000000001, 0.000000002, 0.000000003, 16.3421, 0.000000004, 0.000000001, 0.000000001, 0.000000001, 0.000000001),
                //全是负数
                Arrays.asList(-5738.0,-5738.0,-5738.0,-5738.0,-5738.0,-5738.0,-5738.0,-5739.0,-5739.0)
        );
        for(int i = 0; i < sampleDatas.size(); i++) {
            List<Double> data = sampleDatas.get(i);
            log.info(JSON.toJSONString(service.calculateUsage(data, BigDecimal.valueOf(refValues.get(i)))));
        }
    }
}