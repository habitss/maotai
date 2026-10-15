package com.keytech.modules.ems.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 计算结果包装类
 * @author yepeng
 * @since 2025.05.10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalcUsageResult {
    /**
     * 用量
     */
    private BigDecimal usage;
    /**
     * 最新底数(存储到状态日志表)
     */
    private BigDecimal baseVal;
    /**
     * 异常类型
     */
    private Integer errorType;

}