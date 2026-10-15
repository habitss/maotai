package com.keytech.modules.df.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * DF数据日志表
 * @author yepeng
 * @since 2025.12.11
 * */
@Data
@TableName("mes3_df_data_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF数据日志表")
public class DfDataLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键ID*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**事件ID*/
	@Excel(name = "事件ID", width = 15)
    @Schema(description = "事件ID")
    private String eventId;
	/**事件开始时间*/
	@Excel(name = "事件开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "事件开始时间")
    private LocalDateTime startTime;
	/**事件结束时间*/
	@Excel(name = "事件结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "事件结束时间")
    private LocalDateTime endTime;
	/**指标PI值*/
	@Excel(name = "指标PI值", width = 15)
    @Schema(description = "指标PI值")
    private String piValue;
	/**其他参数*/
	@Excel(name = "其他参数", width = 15)
    @Schema(description = "其他参数")
    private String dataJson;
	/**EOP编号*/
	@Excel(name = "EOP编号", width = 15)
    @Schema(description = "EOP编号")
    private Integer eopId;
	/**指标实例编码（来自MDM）*/
	@Excel(name = "指标实例编码（来自MDM）", width = 15)
    @Schema(description = "指标实例编码（来自MDM）")
    private String piCode;
	/**指标名称*/
	@Excel(name = "指标名称", width = 15)
    @Schema(description = "指标名称")
    private String piName;
	/**指标类型*/
	@Excel(name = "指标类型", width = 15)
    @Schema(description = "指标类型")
    private String piType;
	/**UDE规则编码*/
	@Excel(name = "UDE规则编码", width = 15)
    @Schema(description = "UDE规则编码")
    private String ruleId;
	/**运行模式状态（生产、清洗、维户等，瞬时值）*/
	@Excel(name = "运行模式状态（生产、清洗、维户等，瞬时值）", width = 15)
    @Schema(description = "运行模式状态（生产、清洗、维户等，瞬时值）")
    private Double status;
	/**步骤号*/
	@Excel(name = "步骤号", width = 15)
    @Schema(description = "步骤号")
    private Integer stepId;
	/**批次号*/
	@Excel(name = "批次号", width = 15)
    @Schema(description = "批次号")
    private Integer batchId;
	/**订单号*/
	@Excel(name = "订单号", width = 15)
    @Schema(description = "订单号")
    private Integer orderId;
	/**运行状态*/
	@Excel(name = "运行状态", width = 15)
    @Schema(description = "运行状态")
    private Double running;
	/**实例ID*/
	@Excel(name = "实例ID", width = 15)
    @Schema(description = "实例ID")
    private String thingId;
	/**配方号*/
	@Excel(name = "配方号", width = 15)
    @Schema(description = "配方号")
    private Integer recipeId;
	/**UDE规则描述*/
	@Excel(name = "UDE规则描述", width = 15)
    @Schema(description = "UDE规则描述")
    private String ruleName;
	/**配方类型ID*/
	@Excel(name = "配方类型ID", width = 15)
    @Schema(description = "配方类型ID")
    private Integer recipeTypeId;
	/**最后更新人*/
    @Schema(description = "最后更新人")
    private String updateBy;
	/**最后更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
