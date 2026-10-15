package com.keytech.modules.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @Description: 有效会议类型配置
 * @Author: qipengbo
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="有效会议类型配置")
public class MeetingConfig implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
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
    private String section;

    /**部门id*/
    @Excel(name = "部门id", width = 15)
    @Schema(description = "部门id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String deptId;

	/**会议类型*/
	@Excel(name = "会议类型", width = 15)
    @Schema(description = "会议类型")
    private String meetingType;

    /**会议周期*/
    @Excel(name = "会议频次", width = 15)
    @Schema(description = "会议频次")
    private Integer meetingCycle;

	/**会议名称*/
	@Excel(name = "会议名称", width = 15)
    @Schema(description = "会议名称")
    private String meetingName;

    /**主持人*/
    @Excel(name = "主持人", width = 15)
    @Schema(description = "主持人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String presenter;

	/**参会人*/
	@Excel(name = "参会人", width = 15)
    @Schema(description = "参会人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String participant;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

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

    private static final long serialVersionUID = 1L;
}
