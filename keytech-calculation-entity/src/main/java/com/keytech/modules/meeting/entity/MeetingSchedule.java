package com.keytech.modules.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 会议排期
 * @Author: jeecg-boot
 * @Date:   2025-04-16
 * @Version: V1.0
 */
@Data
@TableName("mes3_meeting_schedule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="会议排期")
public class MeetingSchedule implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**会议类型id*/
	@Excel(name = "会议类型id", width = 15)
    @Schema(description = "会议类型id")
    private String configId;

	/**会议类型名称*/
	@Excel(name = "会议类型名称", width = 15)
    @Schema(description = "会议类型名称")
    private String typeName;

	/**排期状态 1.正在使用 2.取消排期*/
	@Excel(name = "排期状态 1.正在使用 2.取消排期", width = 15)
    @Schema(description = "排期状态 1.正在使用 2.取消排期")
    private Integer status;

	/**排期类型*/
	@Excel(name = "排期类型", width = 15)
    @Schema(description = "排期类型")
    private String meetingType;

	/**日期*/
	@Excel(name = "日期", width = 15)
    @Schema(description = "日期")
    private String meetingDate;

	/**星期几*/
	@Excel(name = "星期几", width = 15)
    @Schema(description = "星期几")
    private String meetingWeek;

	/**周数*/
	@Excel(name = "周数", width = 15)
    @Schema(description = "周数")
    private Integer meetingNum;

	/**时间*/
	@Excel(name = "时间", width = 15)
    @Schema(description = "时间")
    private String meetingTime;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    @TableField(exist = false)
    private String weeks;;


    private static final long serialVersionUID = 1L;
}
