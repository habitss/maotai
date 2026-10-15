package com.keytech.modules.cop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * cop清洗项目配置
 * </p>
 *
 * @author lijun
 * @since 2025-02-19 02:03:56
 */
@Getter
@Setter
@ToString
@TableName("mes3_cop_config")
@Schema(description = "COP清洗项目配置")
public class CopConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "ID", width = 15, orderNum = "0")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description ="租户ID")
    @TableField("tenant_id")
    private Integer tenantId;

    @Schema(description ="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    @Excel(name = "部门Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description ="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    @Excel(name = "产线Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description ="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    @Schema(description ="工段")
    @TableField("section")
    private String section;

    @Excel(name = "部门Code", width = 15,  dictTable ="mes3_cop_point", dicText = "point_name", dicCode = "id")
    @Schema(description ="cop清洗点id")
    @TableField("cop_point_id")
    @Dict(dictTable ="mes3_cop_point",dicText = "point_name",dicCode = "id")
    private String copPointId;

    @Excel(name = "字段名称", width = 20)
    @Schema(description ="字段名称")
    @TableField("field_name")
    private String fieldName;

    @Excel(name = "部门Code", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FIELDTYPE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description ="字段类型：1时间、2日期、3输入框、4附件上传")
    @TableField("field_type")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FIELDTYPE'", dicText = "item_key", dicCode = "item_val")
    private String fieldType;

    @Excel(name = "排序", width = 15)
    @Schema(description ="排序")
    @TableField("sort")
    private Double sort;

    @Schema(description ="备注")
    @TableField("remark")
    private String remark;

    @Schema(description ="0不删除,1为删除")
    @TableField("del_flag")
    @TableLogic
    private Long delFlag;

    @Schema(description ="数据创建时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description ="数据更新时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description ="创建人")
    @TableField("create_by")
    private String createBy;

    @Schema(description ="更新人")
    @TableField("update_by")
    private String updateBy;

    //非实体字段
    @TableField(exist = false)
    private String copDeviceId;

    //非实体字段
    @TableField(exist = false)
    @Schema(description ="是否同步该设备下其他清洗点位")
    private Integer isSync;

}
