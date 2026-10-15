package com.keytech.modules.cpk.util;
import com.keytech.modules.cpk.entity.CpkConfigCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * 过程能力指数（CP 和 CPK）计算工具类
 */
public class CpkCalculatorUtil {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static CpkConfigCalc calculateCpk(List<Double> data, double usl, double lsl) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据列表不能为空");
        }
        if (usl <= lsl) {
            throw new IllegalArgumentException("USL 必须大于 LSL");
        }

        int n = data.size();

        // 计算总和
        BigDecimal sum = data.stream()
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 平均值
        BigDecimal mean = sum.divide(BigDecimal.valueOf(n), SCALE, ROUNDING_MODE);

        // 计算样本标准差（n-1）
        BigDecimal varianceSum = data.stream()
                .filter(Objects::nonNull)
                .map(x -> BigDecimal.valueOf(x).subtract(mean))
                .map(diff -> diff.multiply(diff))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (n == 1) {
            throw new IllegalArgumentException("至少需要两个数据点才能计算标准差");
        }

        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(n - 1), SCALE + 4, ROUNDING_MODE);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("标准差为零，无法计算 CPK");
        }

        BigDecimal uslBd = BigDecimal.valueOf(usl);
        BigDecimal lslBd = BigDecimal.valueOf(lsl);

        // CP = (USL - LSL) / (6 * σ)
        BigDecimal cp = uslBd.subtract(lslBd)
                .divide(stdDev.multiply(BigDecimal.valueOf(6)), SCALE + 4, ROUNDING_MODE);

        // CPU = (USL - mean) / (3 * σ)
        BigDecimal cpu = uslBd.subtract(mean)
                .divide(stdDev.multiply(BigDecimal.valueOf(3)), SCALE + 4, ROUNDING_MODE);

        // CPL = (mean - LSL) / (3 * σ)
        BigDecimal cpl = mean.subtract(lslBd)
                .divide(stdDev.multiply(BigDecimal.valueOf(3)), SCALE + 4, ROUNDING_MODE);

        // CPK = min(CPU, CPL)
        BigDecimal cpk = cpu.compareTo(cpl) < 0 ? cpu : cpl;

        // 构建结果并保留两位小数
        CpkConfigCalc result = new CpkConfigCalc();
        result.setRecordNum(data.size()+"");
        result.setRecordSum(round(sum).toString());
        result.setRecordMean(round(mean).toString());
        result.setCp(round(cp).toString());
        result.setCpk(round(cpk).toString());
        result.setCpku(round(cpu).toString());
        result.setCpkl(round(cpl).toString());

        return result;
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}
