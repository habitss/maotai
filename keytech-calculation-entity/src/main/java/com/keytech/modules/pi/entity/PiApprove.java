package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: pi报表-报表审批
 * @Author: jeecg-boot
 * @Date:   2025-01-09
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_approve")
@Schema(description="pi报表-报表审批")
public class PiApprove implements Serializable {

    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String factoryCode;

    @Schema(description="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String deptCode;

    @Schema(description="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    private String lineCode;

	/**基础报表id*/
	@Excel(name = "基础报表id", width = 15)
    @Schema(description = "基础报表id")
    @Dict(dictTable ="mes3_pi_customize_table",dicText = "name",dicCode = "id")
    private String tableId;

	/**pi报表id*/
	@Excel(name = "pi报表id", width = 15)
    @Schema(description = "pi报表id")
    @Dict(dictTable ="mes3_pi_allocation_table",dicText = "report_name",dicCode = "id")
    private String allocationId;

	/**pi数据id*/
	@Excel(name = "pi数据id", width = 15)
    @Schema(description = "pi数据id")
    private String dataId;

	/**报表名称(表名)*/
	@Excel(name = "报表名称(表名)", width = 15)
    @Schema(description = "报表名称(表名)")
    private String tableName;

	/**原始值*/
	@Excel(name = "原始值", width = 15)
    @Schema(description = "原始值")
    private String originalValue;

	/**修改值*/
	@Excel(name = "修改值", width = 15)
    @Schema(description = "修改值")
    private String modifyValue;

	/**修改/剔除原因*/
	@Excel(name = "修改/剔除原因", width = 15)
    @Schema(description = "修改/剔除原因")
    private String reason;

	/**修改字段/剔除字段(多个)*/
	@Excel(name = "修改字段/剔除字段(多个)", width = 15)
    @Schema(description = "修改字段/剔除字段(多个)")
    private String fields;

	/**修改/剔除时间*/
	@Excel(name = "修改/剔除时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改/剔除时间")
    private Date modifyTime;

	/**修改/剔除修改人*/
	@Excel(name = "修改/剔除修改人", width = 15)
    @Schema(description = "修改/剔除修改人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String modifyBy;

	/**剔除分类(0.试验批/1.停产/2.停机/3.设备故障)*/
	@Excel(name = "剔除分类(0.试验批/1.停产/2.停机/3.设备故障)", width = 15)
    @Schema(description = "剔除分类(0.试验批/1.停产/2.停机/3.设备故障)")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_REMOVE_CATEGORY'", dicText = "item_key", dicCode = "item_val")
    private String removeCategory;

	/**审批状态 (0.待审批/1.通过/2.不通过)*/
	@Excel(name = "审批状态", width = 15)
    @Schema(description = "审批状态")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_APPROVE_STATE'", dicText = "item_key", dicCode = "item_val")
    private String state;

	/**审批人*/
	@Excel(name = "审批人", width = 15)
    @Schema(description = "审批人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String approveBy;

	/**审批时间*/
	@Excel(name = "审批时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审批时间")
    private Date approveTime;

	/**类型(0.修改/1.剔除)*/
	@Excel(name = "类型(0.修改/1.剔除)", width = 15)
    @Schema(description = "类型(0.修改/1.剔除)")
    private String type;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

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

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**删除状态(0未删,1已删)*/
	@Excel(name = "删除状态(0未删,1已删)", width = 15)
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;

    /**剔除范围*/
    @Excel(name = "剔除范围", width = 15)
    @Schema(description = "剔除范围(0整行,1当前PI)")
    private String removeRange;

    /**修改字段/剔除字段(多个)[中文名称]*/
    @TableField(exist = false)
    @Schema(description = "修改字段/剔除字段(多个)")
    private String fieldName;


    /**唯一约束字符串*/
    @TableField(exist = false)
    @Schema(description = "唯一约束字符串")
    private String uniqueStr;
}
