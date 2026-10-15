package com.keytech.modules.period.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 期间核查排期
 * @Author: qipengbo
 * @Date:   2025-03-11
 * @Version: V1.0
 */
@Data
@TableName("mes3_period_verification_schedule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="期间核查排期")
public class PeriodVerificationSchedule implements Serializable {


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
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**仪器id*/
	@Excel(name = "仪器id", width = 15)
    @Schema(description = "仪器id")
    private String apparatusId;

	/**仪器名称*/
	@Excel(name = "仪器名称", width = 15)
    @Schema(description = "仪器名称")
    private String apparatusName;

	/**仪器型号*/
	@Excel(name = "仪器型号", width = 15)
    @Schema(description = "仪器型号")
    private String apparatusType;

	/**频次*/
	@Excel(name = "频次", width = 15)
    @Schema(description = "频次")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    private String frequency;

	/**允许误差*/
	@Excel(name = "允许误差", width = 15)
    @Schema(description = "允许误差")
    private String permissibleError;

	/**检查人*/
	@Excel(name = "检查人", width = 15)
    @Schema(description = "检查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkUser;

	/**确认人*/
	@Excel(name = "确认人", width = 15)
    @Schema(description = "确认人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String verifyUser;

	/**核查方式*/
	@Excel(name = "核查方式", width = 15)
    @Schema(description = "核查方式")
    private String verificationMethod;

	/**说明*/
	@Excel(name = "说明", width = 15)
    @Schema(description = "说明")
    private String explainType;

	/**年度*/
	@Excel(name = "年度", width = 15)
    @Schema(description = "年度")
    private Integer planYear;

	/**周json字符串*/
	@Excel(name = "周json字符串", width = 15)
    @Schema(description = "周json字符串")
    private String weekJson;

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

    //非实体字段
    @TableField(exist = false)
    private String months;

    @Serial
    private static final long serialVersionUID = 1L;
}
