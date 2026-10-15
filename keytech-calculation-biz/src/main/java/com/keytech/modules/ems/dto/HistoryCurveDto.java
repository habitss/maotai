package com.keytech.modules.ems.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

/**
 * 曲线查询参数
 * @author yepeng
 * @since 2025.05.30
 */
@Data
@Schema(description = "曲线查询参数")
public class HistoryCurveDto {
    @Schema(description = "曲线容器宽度")
    private Integer frequency;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime stime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime etime;
    @Schema(description = "点位ID串")
    private String tagIds;
    @Schema(description = "工厂编码")
    private String factoryCode;
}
