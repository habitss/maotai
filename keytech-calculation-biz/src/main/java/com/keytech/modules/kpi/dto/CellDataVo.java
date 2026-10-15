package com.keytech.modules.kpi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
@Builder
public class CellDataVo {
    private String id;
    private String value;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String colour;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Builder.Default
    private Boolean fiveWhyMark =null;
    @Builder.Default
    private FiveWhyAnalyse fiveWhyData =null;

    public CellDataVo(String id, String value, String colour) {
        this.id = id;
        this.value = value;
        this.colour = colour;
    }

    public CellDataVo(String id, String value, String colour, Boolean fiveWhyMark, FiveWhyAnalyse fiveWhyData) {
        this.id = id;
        this.value = value;
        this.colour = colour;
        this.fiveWhyMark = fiveWhyMark;
        this.fiveWhyData = fiveWhyData;
    }
}
