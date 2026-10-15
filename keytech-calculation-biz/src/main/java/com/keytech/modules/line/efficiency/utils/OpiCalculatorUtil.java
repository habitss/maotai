package com.keytech.modules.line.efficiency.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @FileName OPI计算工具类
 * @Description
 * @Author HQ
 * @date 2025-09-16
 **/
public class OpiCalculatorUtil {
    // 默认计算精度
    private static final int DEFAULT_SCALE = 6;
    // 默认计算精度8位
    private static final int DEFAULT_EIGHT_SCALE = 8;
    private static final int DEFAULT_TWO_SCALE = 2;
    private static final BigDecimal HOURS_CONVERSION = new BigDecimal("3600");
    private static final BigDecimal PERCENTAGE_CONVERSION = new BigDecimal("100");
    //定义 Double 值为0 的变量
    public static final Double ZERO = 0.0;

    /**
     * 计算OPI值（BigDecimal版本，可指定精度）
     */
    public static BigDecimal calculateOpi(Double outputDou, Double speedDou, Double runningTimeDou) {

        if(outputDou != null && speedDou != null && runningTimeDou != null){
            BigDecimal output = BigDecimal.valueOf(outputDou);
            BigDecimal speed = BigDecimal.valueOf(speedDou);
            BigDecimal runningTime = BigDecimal.valueOf(runningTimeDou);
            // 检查除零情况
            if (isZero(speed) || isZero(runningTime)) {
                return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
            }
            // OPI = 产量 / 速度 / 运行时长 * 3600
            BigDecimal stepa = output.divide(speed, DEFAULT_SCALE, RoundingMode.HALF_UP);
            BigDecimal stepb = stepa.divide(runningTime, DEFAULT_SCALE, RoundingMode.HALF_UP);
            BigDecimal stepc = stepb.multiply(HOURS_CONVERSION);
            return stepc.multiply(PERCENTAGE_CONVERSION)
                    .setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
        }else{
            return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * 产量除以速度
     */
    public static BigDecimal divide(Double outputDou, Double speedDou) {
        if(outputDou != null && speedDou != null){
            BigDecimal output = BigDecimal.valueOf(outputDou);
            BigDecimal speed = BigDecimal.valueOf(speedDou);
            if (isZero(speed)) {
                return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
            }
            return output.divide(speed, DEFAULT_SCALE, RoundingMode.HALF_UP);
        }else{
            return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * 检查BigDecimal是否为0
     */
    private static boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * SKU比例/运行时长
     */
    public static BigDecimal calculateOpi(BigDecimal sum, Double runningTimeDou) {
        if(sum != null && runningTimeDou != null){
            BigDecimal runningTime = BigDecimal.valueOf(runningTimeDou);
            // 检查除零情况
            if (isZero(sum) || isZero(runningTime)) {
                return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
            }
            // OPI = 产量 / 速度 / 运行时长 * 3600
            BigDecimal stepb = sum.divide(runningTime, DEFAULT_EIGHT_SCALE, RoundingMode.HALF_UP);
            BigDecimal stepc = stepb.multiply(HOURS_CONVERSION);
            return stepc.multiply(PERCENTAGE_CONVERSION)
                    .setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
        }else{
            return BigDecimal.ZERO.setScale(DEFAULT_TWO_SCALE, RoundingMode.HALF_UP);
        }
    }
}
