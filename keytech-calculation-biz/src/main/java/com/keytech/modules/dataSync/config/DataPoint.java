package com.keytech.modules.dataSync.config;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据点实体（实时/历史视图通用）
 */
@Data
public class DataPoint {
    private String dataPointId;
    private String dataPointName;
    private String equipmentId;
    private String equipmentName;
    private String uomName;
    private BigDecimal value;
    private LocalDateTime timestampFrom;
    /**
     * 历史视图专属字段
     * */
    private LocalDateTime timestampTo;
}