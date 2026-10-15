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
 * @Description: 会议评估模版
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_record_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="会议评估模版")
public class MeetingRecordTemplate implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

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

    /**会议类型*/
    @Excel(name = "会议类型", width = 15,  dictTable ="mes3_meeting_config", dicText = "meeting_type", dicCode = "id")
    @Schema(description = "会议类型")
//    @Dict(dictTable ="mes3_meeting_config",dicText = "meeting_type",dicCode = "id")
    private String meetingType;

	/**评估类型 1-会议内容 2-会议效果*/
	@Excel(name = "评估类型 1-会议内容 2-会议效果", width = 15)
    @Schema(description = "评估类型 1-会议内容 2-会议效果")
    private Integer recordType;

	/**内容*/
	@Excel(name = "内容", width = 15)
    @Schema(description = "内容")
    private String content;

	/**评分标准*/
	@Excel(name = "评分标准", width = 15)
    @Schema(description = "评分标准")
    private String standard;

	/**权重*/
	@Excel(name = "权重", width = 15)
    @Schema(description = "权重")
    private Integer weight;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
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

    private static final long serialVersionUID = 1L;
}
