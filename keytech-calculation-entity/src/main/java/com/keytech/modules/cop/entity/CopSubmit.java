package com.keytech.modules.cop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * cop填报
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Getter
@Setter
@ToString
@TableName("mes3_cop_submit")
@Schema(description = "cop填报")
public class CopSubmit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description ="id")
    private String id;

    @Schema(description ="租户ID")
    @TableField("tenant_id")
    private Integer tenantId;

    @Schema(description ="工厂Code")
    @TableField("factory_code")
    private String factoryCode;

    @Schema(description ="部门Code")
    @TableField("dept_code")
    private String deptCode;

    @Schema(description ="产线Code")
    @TableField("line_code")
    private String lineCode;

    @Schema(description ="工段")
    @TableField("section")
    private String section;

    @Schema(description ="排期计划id")
    @TableField("cop_schedule_id")
    private String copScheduleId;

    @Schema(description ="年")
    @TableField("plan_year")
    private Integer planYear;

    @Schema(description ="月")
    @TableField("plan_month")
    private Integer planMonth;

    @Schema(description ="周")
    @TableField("plan_week")
    private String planWeek;

    @Schema(description ="清理日期")
    @TableField("clear_date")
    private Date clearDate;

    @Schema(description ="清理人")
    @TableField("clear_by")
    private String clearBy;

    @Schema(description ="清理备注")
    @TableField("clear_remark")
    private String clearRemark;

    @Schema(description ="检查日期")
    @TableField("check_date")
    private Date checkDate;

    @Schema(description ="检查人")
    @TableField("check_by")
    private String checkBy;

    @Schema(description ="检查备注")
    @TableField("check_remark")
    private String checkRemark;

    @Schema(description ="检查结果")
    @TableField("check_result")
    private String checkResult;

    @Schema(description ="数据json字符串")
    @TableField("data_json")
    private String dataJson;

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
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description ="数据更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Schema(description ="创建人")
    @TableField("create_by")
    private String createBy;

    @Schema(description ="更新人")
    @TableField("update_by")
    private String updateBy;


}
