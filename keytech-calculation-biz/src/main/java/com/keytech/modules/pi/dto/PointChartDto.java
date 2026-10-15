package com.keytech.modules.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/3/10 15:20
 */
@Data
public class PointChartDto {
    @Schema(description="报表code")
    private String tableCode;

    @Schema(description="显示字段code")
    private String fieldCodes;

    @Schema(description="时间查询字段code")
    private String timeCode;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="查询时间-开始")
    private Date searchTimeStart;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="查询时间-结束")
    private Date searchTimeEnd;
}
