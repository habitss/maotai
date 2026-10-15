package com.keytech.modules.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 点图
 * @Author: sjw
 * @Date: 2025/3/10 15:20
 */
@Data
public class PointDiagramDto {
    @Schema(description="id")
    private String id;

    @Schema(description="表名")
    private String tableName;

    @Schema(description="数据字段")
    private String fieldName;

    @Schema(description="横向坐标字段")
    private String xFieldName;

    @Schema(description="时间查询字段")
    private String timeField;

    @Schema(description="唯一标识字段")
    private String uniqueField;

    @Schema(description="标准id")
    private String standardId;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="查询时间-开始")
    private Date searchTimeStart;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="查询时间-结束")
    private Date searchTimeEnd;

    @Schema(description="查询sql")
    private String strSql;

    @Schema(description="红上")
    private String redUp;

    @Schema(description="黄上")
    private String yellowUp;

    @Schema(description="标准值")
    private String standardValue;

    @Schema(description="黄下")
    private String yellowDown;

    @Schema(description="红下")
    private String redDown;

    @Schema(description="标准名称")
    private String manageName;
}
