package com.keytech.modules.period.entity;

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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 期间排查排查内容提交
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_period_verification_submit")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="期间排查排查内容提交")
public class PeriodVerificationSubmit implements Serializable {


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

	/**排期计划id*/
	@Excel(name = "排期计划id", width = 15)
    @Schema(description = "排期计划id")
    private String periodScheduleId;

	/**年*/
	@Excel(name = "年", width = 15)
    @Schema(description = "年")
    private Integer planYear;

	/**月*/
	@Excel(name = "月", width = 15)
    @Schema(description = "月")
    private Integer planMonth;

	/**周*/
	@Excel(name = "周", width = 15)
    @Schema(description = "周")
    private Integer planWeek;

	/**检查日期*/
	@Excel(name = "检查日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检查日期")
    private Date checkDate;

	/**数据json字符串*/
	@Excel(name = "数据json字符串", width = 15)
    @Schema(description = "数据json字符串")
    private String dataJson;

	/**检查人*/
	@Excel(name = "检查人", width = 15)
    @Schema(description = "检查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkUser;

    /**检查结果*/
    @Excel(name = "检查结果", width = 15)
    @Schema(description = "检查结果")
    private Integer checkResult;

	/**核查人*/
	@Excel(name = "核查人", width = 15)
    @Schema(description = "核查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String verifyUser;

    /**核查结果*/
    @Excel(name = "核查结果", width = 15)
    @Schema(description = "核查结果")
    private Integer verifyResult;

    /**核查结果备注*/
    @Excel(name = "核查结果备注", width = 15)
    @Schema(description = "核查结果备注")
    private String verifyRemark;

    /**核查时间*/
    @Excel(name = "核查时间", width = 15)
    @Schema(description = "核查时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date verifyDate;

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

    @Serial
    private static final long serialVersionUID = 1L;
}
