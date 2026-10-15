package com.keytech.modules.alarm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 报警事实
 * @author liu
 * @since 2026.01.12
 * */
@Data
@TableName("alarm_event")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="报警事实")
public class AlarmEvent implements Serializable {
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
	/**报警PI*/
	@Excel(name = "报警PI", width = 15)
    @Schema(description = "报警PI")
    private String alarmPiCode;
	/**报警点位ID*/
	@Excel(name = "报警点位ID", width = 15)
    @Schema(description = "报警点位ID")
    private Integer alarmPointId;
	/**来源系统*/
	@Excel(name = "来源系统", width = 15)
    @Schema(description = "来源系统")
    private String sourceSystem;
	/**功能位置编码*/
	@Excel(name = "功能位置编码", width = 15)
    @Schema(description = "功能位置编码")
    private String funcLocCode;
	/**设备编码*/
    @Excel(name = "设备", width = 15)
    @Schema(description = "设备编码")
    private String deviceCode;
	/**报警等级*/
	@Excel(name = "报警等级", width = 15)
    @Schema(description = "报警等级")
    private Integer alarmLevel;
	/**报警时间*/
	@Excel(name = "报警时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报警时间")
    private LocalDateTime alarmTime;
	/**报警描述*/
	@Excel(name = "报警描述", width = 15)
    @Schema(description = "报警描述")
    private String alarmDesc;
	/**在岗人员*/
	@Excel(name = "在岗人员", width = 15)
    @Schema(description = "在岗人员")
    private String onDutyPerson;
	/**当前状态(0-未响应,1-已响应,2-处理中,3-已处理)*/
	@Excel(name = "当前状态", width = 15)
    @Schema(description = "当前状态(0-未响应,1-已响应,2-处理中,3-已处理)")
    private Integer status;
    /**处理方式(现场处理/远程复位/转派工单/标记误报)*/
	@Excel(name = "处理方式", width = 15)
    @Schema(description = "处理方式(现场处理/远程复位/转派工单/标记误报)")
    private String handleWay;
    /**处理说明(值班人手填)*/
	@Excel(name = "处理说明", width = 20)
    @Schema(description = "处理说明")
    private String handleDesc;
    /**当前状态(0、未处理 1、处理中 2、已解决 3、未解决 6、无法解决 7、待班长审核)*/
    @Excel(name = "TSM状态", width = 15)
    @Schema(description = "TSM状态(0、未处理 1、处理中 2、已解决 3、未解决 6、无法解决 7、待班长审核)")
    private Integer tsmState;
    /**接口请求ID*/
    @Schema(description = "接口请求ID")
    private String requestId;
	/**排序号*/
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

    //数量
    @TableField(exist = false)
    private Integer cnt;
    //报警类型
    @TableField(exist = false)
    private String alarmCategory;
    //状态描述
    @TableField(exist = false)
    private String statusName;
    //报警点位名称
    @TableField(exist = false)
    private String alarmName;
    //工厂名称(join sys_depart 翻译 factoryCode;非持久化,手动填充,前端 factoryCode_dictText 用)
    @TableField(exist = false)
    private String factoryCodeDictText;
    //部门/车间名称(join sys_depart 翻译 deptCode;非持久化,手动填充,前端 deptCode_dictText 用)
    @TableField(exist = false)
    private String deptCodeDictText;
    //产线名称(join sys_depart 翻译 lineCode;非持久化,手动填充,前端 lineCode_dictText 用)
    @TableField(exist = false)
    private String lineCodeDictText;
}
