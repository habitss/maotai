package com.keytech.modules.kpi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @FileName DataItem
 * @Description
 * @Author HQ
 * @date 2026-03-26
 **/
@Data
public class DataItemDto {
    /**
     * 表 (mes3_basedata_00x)
     */
    @JsonProperty("tableId")
    private String tableId;
    /**
     * 字段(field_x)
     */
    @JsonProperty("fieldId")
    private String fieldId;
    /**
     * 时间字段id
     */
    @JsonProperty("timeFieldId")
    private String timeFieldId;
}
