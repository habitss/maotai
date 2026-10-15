package com.keytech.modules.kpi.dto;

import lombok.Data;

import java.util.List;

/**
 * @FileName BatchConfig
 * @Description
 * @Author HQ
 * @date 2026-03-26
 **/
@Data
public class BatchConfigDto {
    /**
     * 类型 1KPI,2PI
     */
    private Integer type;
    /**
     * 数据项
     */
    private List<DataItemDto> datas;
}
