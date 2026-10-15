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
 * @Description: 会议模版
 * @Author: qipengbo
 * @Date:   2025-03-18
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="会议模版")
public class MeetingTemplate implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "ID", width = 15, orderNum = "0")
    @Schema(description = "id")
    private String id;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer sort;

	/**工厂Code*/
    @Excel(name = "工厂(非工厂会议请勿填写此列)", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
    @Excel(name = "部门(非部门会议请勿填写此列)", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    /**车间Code*/
    @Excel(name = "车间(非车间会议请勿填写此列)", width = 15)
    @Schema(description = "车间Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String workshop;

	/**产线Code*/
    @Excel(name = "产线", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

	/**会议类型*/
    @Excel(name = "会议类型(*)", width = 15,  dictTable ="mes3_meeting_config where del_flag=0 ", dicText = "meeting_type", dicCode = "id")
    @Schema(description = "会议类型")
    @Dict(dictTable ="mes3_meeting_config",dicText = "meeting_type",dicCode = "id")
    private String meetingType;

	/**会议名称*/
	@Excel(name = "会议名称(*)", width = 15)
    @Schema(description = "会议名称")
    private String meetingName;

	/**参会人*/
    @Excel(name = "参会人", width = 50,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "参会人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String participant;

	/**会议日程*/
	@Excel(name = "会议日程", width = 15)
    @Schema(description = "会议日程")
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
    @Excel(name = "汇报人", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "汇报人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String reporter;

	/**代理人*/
    @Excel(name = "代理人", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "代理人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String agent;

	/**时间*/
	@Excel(name = "时间", width = 15)
    @Schema(description = "时间")
    private Integer time;

	/**状态*/
    @Schema(description = "状态")
    private Integer active;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

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
    private List<MeetingLinks> links;

    private static final long serialVersionUID = 1L;
}
