package com.keytech.modules.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 会议信息
 * @Author: qipengbo
 * @Date:   2025-03-18
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="会议信息")
public class Meeting implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;

    /**车间Code*/
    @Excel(name = "车间Code", width = 15)
    @Schema(description = "车间Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String workshop;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String section;

	/**会议名称*/
	@Excel(name = "会议名称", width = 15)
    @Schema(description = "会议名称")
    private String meetingName;

	/**会议类型*/
	@Excel(name = "会议类型", width = 15)
    @Schema(description = "会议类型")
    @Dict(dictTable ="mes3_meeting_config",dicText = "meeting_type",dicCode = "id")
    private String meetingType;

    /**会议时间*/
    @Excel(name = "会议日期", width = 20, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "会议日期")
    private Date meetingDate;

	/**会议时间*/
	@Excel(name = "会议时间", width = 20)
    @Schema(description = "会议时间")
    private String meetingTime;

	/**参会人*/
	@Excel(name = "参会人", width = 15)
    @Schema(description = "参会人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String participant;

    /**排期标识*/
    @Excel(name = "排期标识", width = 15)
    @Schema(description = "排期标识")
    private String scheduleId;

    /**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**状态*/
	@Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private Integer active;

    /**取消备注*/
    @Excel(name = "取消备注", width = 15)
    @Schema(description = "取消备注")
    private String notes;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer sort;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    @TableField(exist = false)
    private Integer day;

    @TableField(exist = false)
    private Integer month;

    @TableField(exist = false)
    private String currentDate;

    @TableField(exist = false)
    private Double score;

    @TableField(exist = false)
    private Integer color;

    private static final long serialVersionUID = 1L;
}
