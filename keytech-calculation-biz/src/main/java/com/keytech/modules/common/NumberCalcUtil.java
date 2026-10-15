package com.keytech.modules.common;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberCalcUtil {
    /**
     * 计算百分比并格式化为保留两位小数的百分比字符串
     */
    public static String calculatePercentageWithTwoDecimal(String valueStr, String totalStr, boolean needSuffix) {
        String suffix = needSuffix ? "%" : "";
        if (StrUtil.isBlank(valueStr) || StrUtil.isBlank(totalStr)) {
            return "0.00"+suffix;
        }

        NumberFormat format = NumberFormat.getInstance();

        try {
            // 转换为数字
            Number valueNum = format.parse(valueStr);
            Number totalNum = format.parse(totalStr);

            double value = valueNum.doubleValue();
            double total = totalNum.doubleValue();

            if (total == 0) {
                return "0.00"+suffix;
            }

            // 计算百分比
            double percentage = (value / total) * (needSuffix ? 1 : 100);

            // 格式化输出（保留两位小数，并加 % 符号）
            DecimalFormat df = new DecimalFormat("0.00"+suffix);
            return df.format(percentage);

        } catch (Exception e) {
            return "0.00"+suffix;
        }
    }

    public static String calculatePercentage(int valueStr, int totalStr, boolean needSuffix){
        return calculatePercentageWithTwoDecimal(valueStr+"", totalStr+"", needSuffix);
    }


    /**
     * 除法计算
     */
    /**
     * 执行除法运算并保留指定小数位数
     * @param dividend 被除数
     * @param divisor 除数
     * @param scale 保留小数位数
     * @return 计算结果字符串
     */
    public static String divideWithScale(String dividend, String divisor, int scale) {
        try {
            BigDecimal bd1 = new BigDecimal(dividend);
            BigDecimal bd2 = new BigDecimal(divisor);

            // 使用 BigDecimal 进行精确除法运算
            BigDecimal result = bd1.divide(bd2, scale, RoundingMode.HALF_UP);
            if(result.toString().equals("0.00")){
                return "0";
            }
            return result.toString();
        } catch (NumberFormatException | ArithmeticException e) {
            return "0";
        }
    }

}
