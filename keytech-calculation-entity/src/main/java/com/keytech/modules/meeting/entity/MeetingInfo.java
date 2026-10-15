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
import java.util.List;

/**
 * @Description: 会议信息
 * @Author: qipengbo
 * @Date:   2025-03-18
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="会议信息")
public class MeetingInfo implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    /**会议编码*/
    @Excel(name = "会议编码", width = 15)
    @Schema(description = "会议编码")
    private String meetingId;

	/**模版编码*/
	@Excel(name = "模版编码", width = 15)
    @Schema(description = "模版编码")
    private String templateId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
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
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**会议名称*/
	@Excel(name = "会议名称", width = 15)
    @Schema(description = "会议名称")
    private String meetingName;

	/**会议类型*/
	@Excel(name = "会议类型", width = 15)
    @Schema(description = "会议类型")
    private String meetingType;

	/**会议主题*/
	@Excel(name = "会议主题", width = 15)
    @Schema(description = "会议主题")
    private String contentTitle;

	/**会议内容*/
	@Excel(name = "会议内容", width = 15)
    @Schema(description = "会议内容")
    private String content;

	/**链接*/
	@Excel(name = "链接", width = 15)
    @Schema(description = "链接")
    private String link;

	/**链接地址*/
	@Excel(name = "链接地址", width = 15)
    @Schema(description = "链接地址")
    private String linkAddress;

	/**汇报人*/
	@Excel(name = "汇报人", width = 15)
    @Schema(description = "汇报人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String reporter;

	/**代理人*/
	@Excel(name = "代理人", width = 15)
    @Schema(description = "代理人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String agent;

	/**时间*/
	@Excel(name = "计划时长", width = 15)
    @Schema(description = "计划时长")
    private Integer time;

    /**时间*/
    @Excel(name = "实际时长", width = 15)
    @Schema(description = "实际时长")
    private Integer realTime;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String notes;

	/**备注*/
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

    @Schema(description = "上传文件")
    private String uploadFiles;

    @TableField(exist = false)
    private List<MeetingLinks> links;

    private static final long serialVersionUID = 1L;
}
