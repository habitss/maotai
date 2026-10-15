package com.keytech.modules.alarm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 报警接入日志
 * @author liu
 * @since 2026.01.12
 * */
@Data
@TableName("alarm_ingest_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="报警接入日志")
public class AlarmIngestLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
    /**工厂Code*/
    @Excel(name = "工厂", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;
    /**部门Code*/
    @Excel(name = "部门", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;
    /**产线Code*/
    @Excel(name = "产线", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;
	/**来源系统*/
	@Excel(name = "来源系统", width = 15)
    @Schema(description = "来源系统")
    private String sourceSystem;
	/**请求ID*/
	@Excel(name = "请求ID", width = 15)
    @Schema(description = "请求ID")
    private String requestId;
	/**报警PI*/
	@Excel(name = "报警PI", width = 15, dictTable = "alarm_point", dicText = "alarm_name", dicCode = "alarm_pi_code")
	@Dict(dictTable = "alarm_point", dicText = "alarm_name", dicCode = "alarm_pi_code")
    @Schema(description = "报警PI")
    private String alarmPiCode;
	/**原始报文*/
	@Excel(name = "原始报文", width = 15)
    @Schema(description = "原始报文")
    private String rawPayload;
	/**接入状态*/
	@Excel(name = "接入状态", width = 15)
    @Schema(description = "接入状态")
    private Integer ingestStatus;
	/**错误信息*/
	@Excel(name = "错误信息", width = 15)
    @Schema(description = "错误信息")
    private String errorMsg;
	/**接收时间*/
	@Excel(name = "接收时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "接收时间")
    private LocalDateTime receiveTime;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @Schema(description = "排序号")
    private Integer sort;
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**删除标记*/
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;
}
