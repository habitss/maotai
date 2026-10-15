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
 * @Description: 期间核查仪器信息
 * @Author: qipengbo
 * @Date:   2025-03-09
 * @Version: V1.0
 */
@Data
@TableName("mes3_period_verification_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="期间核查设备信息")
public class PeriodVerificationDevice implements Serializable {


	/**id*/
    @Excel(name = "ID", width = 15, orderNum = "0")
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Schema(description = "工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
    @Excel(name = "*部门", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
    @Excel(name = "*产线", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

    @Schema( description ="计划年份")
    @TableField("plan_year")
    private Integer planYear;

	/**仪器名称*/
	@Excel(name = "*仪器名称", width = 15)
    @Schema(description = "仪器名称")
    private String apparatusName;

	/**仪器型号*/
	@Excel(name = "仪器型号", width = 15)
    @Schema(description = "仪器型号")
    private String apparatusType;

	/**检查频次*/
    @Excel(name = "*检查频次", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "检查频次")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    private String frequencyOfCheck;

	/**核查方式*/
	@Excel(name = "核查方式", width = 15)
    @Schema(description = "核查方式")
    private String verificationMethod;

	/**说明*/
	@Excel(name = "说明", width = 15)
    @Schema(description = "说明")
    private String explainType;

	/**允许误差*/
	@Excel(name = "允许误差", width = 15)
    @Schema(description = "允许误差")
    private String permissibleError;

    @Schema(description ="是否激活，1：激活、0：不激活")
    @TableField("active")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_DEVICE_ACTIVE'", dicText = "item_key", dicCode = "item_val")
    private Integer active;

    /**检查人*/
    @Excel(name = "检查人", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "检查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String checkUser;

    /**核查人*/
    @Excel(name = "核查人", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "核查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String verifyUser;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
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
