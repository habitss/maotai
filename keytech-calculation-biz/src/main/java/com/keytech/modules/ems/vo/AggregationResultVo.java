package com.keytech.modules.ems.vo;

import com.keytech.modules.ems.entity.AggregationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 聚合结果 VO
 * @author yepeng
 * @since 2025.05.13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResultVo {
    private AggregationStatus aggregationStatus;
    /** 用量值 */
    private BigDecimal usage;
    /** 是否成功 */
    private boolean success;
    /** 错误消息 */
    private String errorMsg;
    /** 错误类型 */
    private Integer errorType;
}
