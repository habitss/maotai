package com.keytech.modules.ems.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 曲线结果实体类
 * @author yepeng
 * @since 2025.05.30
 */
@Data
@Schema(description = "曲线结果实体类")
public class HistoryCurveVo {
    @Schema(description = "点位ID")
    private String tagId;
    @Schema(description = "时间类型(Day,Hour,Second,Other)")
    private String dataType;
    @Schema(description = "曲线数据")
    private List<FluxZone8Data> lineDatas;
}