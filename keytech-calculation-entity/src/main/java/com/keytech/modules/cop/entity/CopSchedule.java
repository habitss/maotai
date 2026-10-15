package com.keytech.modules.cop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.cop.dto.CopScheduleJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * cop计划排期
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Getter
@Setter
@ToString
@TableName("mes3_cop_schedule")
@Schema(description = "cop计划排期")
public class CopSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description ="id")
    private String id;

    @Schema(description ="租户ID")
    @TableField("tenant_id")
    private Integer tenantId;

    @Schema(description ="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    @Schema(description ="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    @Schema(description ="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    @Schema(description ="工段")
    @TableField("section")
    private String section;

    @Schema(description ="cop设备id")
    @TableField("cop_device_id")
    @Dict(dictTable ="mes3_cop_device",dicText = "cop_device_name",dicCode = "id")
    private String copDeviceId;

    @Schema(description ="cop设备名称")
    @TableField("cop_device_name")
    private String copDeviceName;

    @Schema(description ="cop清洗点id")
    @TableField("cop_point_id")
    @Dict(dictTable ="mes3_cop_point",dicText = "point_name",dicCode = "id")
    private String copPointId;

    @Schema(description ="cop清洗点名称")
    @TableField("cop_point_name")
    private String copPointName;

    @Schema(description ="温度")
    @TableField("temperature")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_CONFIG_TEMPERATURE'", dicText = "item_key", dicCode = "item_val")
    private String temperature;

    @Schema(description ="推荐配方")
    @TableField("formula")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_COP_RECOMMENDED_FORMULA'", dicText = "item_key", dicCode = "item_val")
    private String formula;

    @Schema(description ="时间")
    @TableField("time_value")
    private String timeValue;

    @Schema(description ="频次")
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

    @Schema(description ="清理人id")
    @TableField("clear_by")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String clearBy;

    @Schema(description ="检查人id")
    @TableField("check_by")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkBy;

    @Schema(description ="清洗标准")
    @TableField("cleaning_standard")
    private String cleaningStandard;

    @Schema(description ="清洗步骤")
    @TableField("cleaning_step")
    private String cleaningStep;

    @Schema(description ="年度")
    @TableField("plan_year")
    private Integer planYear;

    @Schema(description ="周json字符串")
    @TableField("week_json")
    //        '1': 'green', //已完成
    //        '2': 'red', //延迟
    //        '3': 'red', //延迟完成
    //        '4': 'bc', //待审核
    //        '5': 'red', //未执行
    //        '0': 'red', //不合格
    //        '-1': 'yc', //计划
    //        '6': 'grey' //未执行
    private String weekJson;

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
    List<CopScheduleJson> scheduleJsonList = new ArrayList<>();

    //非实体字段
    @TableField(exist = false)
    private String rowKey;

    //非实体字段
    @TableField(exist = false)
    private String months;;

}
