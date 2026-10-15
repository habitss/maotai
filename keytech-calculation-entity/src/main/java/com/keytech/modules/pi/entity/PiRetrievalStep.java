package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * pi-取值步骤
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-15 09:12:22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_retrieval_step")
@Schema(description="pi-取值步骤")
public class PiRetrievalStep implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Excel(name = "主键", width = 15)
    private String id;

    @Schema(description="工厂")
    @TableField("factory")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factory;

    @Schema(description="部门")
    @TableField("dept")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String dept;


    @Schema(description="计算信号id")
    @TableField("calculate_id")
    @Excel(name = "计算信号id", width = 15)
    private String calculateId;

    @Schema(description="步骤类型 0开始 1结束")
    @TableField(value = "step_type", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "步骤类型 0开始 1结束", width = 15)
    private Integer stepType;

    @Schema(description="基础点位代码")
    @TableField(value = "point_code", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "基础点位代码", width = 15)
    private String pointCode;

    @Schema(description="基础点位名称")
    @TableField(value = "point_name", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "基础点位名称", width = 15)
    private String pointName;

    @Schema(description="计算类型")
    @TableField(value = "calculate_type", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "计算类型", width = 15)
    private String calculateType;

    @Schema(description="计算值")
    @TableField(value = "calculate_value", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "计算值", width = 15)
    private String calculateValue;

    @Schema(description="提前或延后，0提前 1延后")
    @TableField(value = "advance_delay", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "提前或延后，0提前 1延后", width = 15)
    private String advanceDelay;

    @Schema(description="提前或延后时间，单位秒")
    @TableField(value = "advance_time", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "提前或延后时间，单位秒", width = 15)
    private String advanceTime;

    @Schema(description="说明")
    @TableField("explanation")
    @Excel(name = "说明", width = 15)
    private String explanation;

    @Schema(description="频率（秒）")
    @TableField(value = "frequency", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "频率（秒）", width = 15)
    private String frequency;

    @Schema(description="步骤值类型(1当前时间/2步骤号)")
    @TableField(value = "step_value_type", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "步骤值类型(1当前时间/2步骤号)", width = 15)
    private String stepValueType;

    @Schema(description="是否切割(1是/0否)")
    @TableField(value = "is_slicing", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "是否切割", width = 15)
    private String isSlicing;

    @Schema(description="是否删除 0-正常 1-删除")
    @TableField("del_flag")
    @TableLogic(value = "0", delval = "id")
    private Integer delFlag;

    @Schema(description="创建人")
    @TableField("create_by")
    @Excel(name = "创建人", width = 15)
    private String createBy;

    @Schema(description="创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Excel(name = "创建时间", width = 20 ,format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description="更新人")
    @TableField("update_by")
    @Excel(name = "更新人", width = 15)
    private String updateBy;

    @Schema(description="更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Excel(name = "更新时间", width = 20 ,format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
