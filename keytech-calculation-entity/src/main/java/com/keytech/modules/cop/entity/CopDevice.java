package com.keytech.modules.cop.entity;

import com.baomidou.mybatisplus.annotation.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * cop设备管理
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 02:08:08
 */
@Getter
@Setter
@ToString
@TableName("mes3_cop_device")
@Schema(description="cop设备管理")
public class CopDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "ID", width = 15, orderNum = "0")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    @Schema(description ="租户ID")
    @TableField("tenant_id")
    private Integer tenantId;

    @Schema(description ="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    @Excel(name = "*部门Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description ="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    @Excel(name = "*产线Code", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description ="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    @Schema(description ="工段")
    @TableField("section")
    private String section;

    @Excel(name = "*设备名称", width = 15)
    @Schema(description ="设备名称")
    @TableField("cop_device_name")
    private String copDeviceName;

    @Schema(description ="是否激活，1：激活、0：不激活")
    @TableField("active")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_DEVICE_ACTIVE'", dicText = "item_key", dicCode = "item_val")
    private Integer active;

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
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description ="数据更新时间")
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
    private List<CopPoint> copPointList = new ArrayList<>();
}
