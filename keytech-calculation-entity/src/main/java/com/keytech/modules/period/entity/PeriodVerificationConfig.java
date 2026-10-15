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
 * @Description: 期间核查填报项配置
 * @Author: qipengbo
 * @Date:   2025-03-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_period_verification_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="期间核查填报项配置")
public class PeriodVerificationConfig implements Serializable {


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
    @Excel(name = "部门Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
    @Excel(name = "产线Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

	/**仪器id*/
    @Excel(name = "仪器", width = 15,  dictTable ="mes3_period_verification_device  where del_flag=0 " , dicText = "apparatus_name", dicCode = "id")
    @Schema(description = "仪器id")
    @Dict(dictTable ="mes3_period_verification_device",dicText = "apparatus_name",dicCode = "id")
    private String apparatusId;

	/**字段名称*/
	@Excel(name = "字段名称", width = 15)
    @Schema(description = "字段名称")
    private String fieldName;

	/**字段类型：1时间、2日期、3输入框、4附件上传*/
    @Excel(name = "字段类型", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FIELDTYPE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "字段类型：1时间、2日期、3输入框、4附件上传")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FIELDTYPE'", dicText = "item_key", dicCode = "item_val")
    private String fieldType;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
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
