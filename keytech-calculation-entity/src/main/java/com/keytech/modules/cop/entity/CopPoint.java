package com.keytech.modules.cop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.mdm3.entity.MdmWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * cop清洗点
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 04:42:21
 */
@Getter
@Setter
@ToString
@TableName("mes3_cop_point")
@Schema( description = "cop清洗点")
public class CopPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "ID", width = 15, orderNum = "0")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    @Schema( description ="租户ID")
    @TableField("tenant_id")
    private Integer tenantId;

    @Schema( description ="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    @Excel(name = "*部门", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema( description ="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    @Excel(name = "*产线", width = 15,  dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema( description ="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    @Schema( description ="工段")
    @TableField("section")
    private String section;

//    @Schema( description ="计划年份")
//    @TableField("plan_year")
//    private Integer planYear;

    @Excel(name = "*cop设备", width = 15,  dictTable ="mes3_cop_device  where del_flag=0 ", dicText = "cop_device_name", dicCode = "id")
    @Schema( description ="cop设备id")
    @TableField("cop_device_id")
    @Dict(dictTable ="mes3_cop_device",dicText = "cop_device_name",dicCode = "id")
    private String copDeviceId;

    @Schema(description ="cop设备名称")
    @TableField("cop_device_name")
    private String copDeviceName;

    @Excel(name = "*清洗点名称", width = 15)
    @Schema( description ="清洗点名称")
    @TableField("point_name")
    private String pointName;

    @Excel(name = "*清洗标准", width = 15)
    @Schema( description ="清洗标准")
    @TableField("cleaning_standard")
    private String cleaningStandard;

    @Excel(name = "*清洗步骤", width = 15)
    @Schema( description ="清洗步骤")
    @TableField("cleaning_step")
    private String cleaningStep;

    @Excel(name = "温度", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_TEMPERATURE'", dicText = "item_key", dicCode = "item_val")
    @Schema( description ="温度")
    @TableField("temperature")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_TEMPERATURE'", dicText = "item_key", dicCode = "item_val")
    private String temperature;

    @Excel(name = "推荐配方", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_RECOMMENDED_FORMULA'", dicText = "item_key", dicCode = "item_val")
    @Schema( description ="推荐配方")
    @TableField("formula")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_RECOMMENDED_FORMULA'", dicText = "item_key", dicCode = "item_val")
    private String formula;

    @Excel(name = "时间", width = 15)
    @Schema( description ="时间")
    @TableField("time_value")
    private String timeValue;

    @Excel(name = "*频次", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    @Schema( description ="频次")
    @TableField("frequency")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    //1：周/次
    //2：月/次
    //3：季度/次
    //4：半年/次
    //5：一年/次
    //6：两年/次
    //7：五年/次
    private String frequency;

    @Excel(name = "清理人", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema( description ="清理人id")
    @TableField("clear_by")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String clearBy;

    @Excel(name = "检查人", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema( description ="检查人id")
    @TableField("check_by")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkBy;



    @Excel(name = "排序", width = 15)
    @Schema( description ="排序")
    @TableField("sort")
    private Double sort;

    @Schema(description ="是否激活，1：激活、0：不激活")
    @TableField("active")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_DEVICE_ACTIVE'", dicText = "item_key", dicCode = "item_val")
    private Integer active;

    @Schema( description ="备注")
    @TableField("remark")
    private String remark;

    @Schema( description ="0不删除,1为删除")
    @TableField("del_flag")
    @TableLogic
    private Long delFlag;

    @Schema( description ="数据创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema( description ="数据更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema( description ="创建人")
    @TableField("create_by")
    private String createBy;

    @Schema( description ="更新人")
    @TableField("update_by")
    private String updateBy;

    //非实体字段
    @TableField(exist = false)
    private List<CopConfig> copConfigList = new ArrayList<>();

    //非实体字段
    @TableField(exist = false)
    private List<String> deivceIdList = new ArrayList<>();

    //非实体字段
    @TableField(exist = false)
    private List<MdmWeek> weekList = new ArrayList<>();
}
