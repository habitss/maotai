package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * pi-监控模型
 * </p>
 *
 * @author 贺强
 * @since 2024-11-5 14:00:00
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_computation_monitor_model")
@Schema(description="pi-计算模型")
public class PiComputationMonitorModel implements Serializable {

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



    @Schema(description="实际值")
    @TableField(value="actual_value", updateStrategy = FieldStrategy.IGNORED)
    @Excel(name = "实际值", width = 15)
    private String actualValue;

    @Schema(description="设定值-基础点位")
    @TableField(value="presume_point", updateStrategy = FieldStrategy.IGNORED)
    @Excel(name = "设定值-基础点位", width = 15)
    private String presumePoint;

    @Schema(description="设定值-值")
    @TableField(value="presume_value", updateStrategy = FieldStrategy.IGNORED)
    @Excel(name = "设定值-值'", width = 15)
    private String presumeValue;

    @Schema(description="取数规则-左偏差值")
    @TableField("left_value")
    @Excel(name = "取数规则-左偏差值", width = 15)
    private String leftValue;

    @Schema(description="取数规则-左偏差条件")
    @TableField("left_condition")
    @Excel(name = "取数规则-左偏差条件", width = 15)
    private String leftCondition;

    @Schema(description="取数规则-计算值类型")
    @TableField("value_type")
    @Excel(name = "取数规则-计算值类型", width = 15)
    private String valueType;

    @Schema(description="取数规则-右偏差条件")
    @TableField("right_condition")
    @Excel(name = "取数规则-右偏差条件", width = 15)
    private String rightCondition;

    @Schema(description="取数规则-右偏差值")
    @TableField("right_value")
    @Excel(name = "取数规则-右偏差值", width = 15)
    private String rightValue;

    @Schema(description="取数规则-计算方式")
    @TableField("calculation_method")
    @Excel(name = "取数规则-计算方式", width = 15)
    private String calculationMethod;

    @Schema(description="取数规则-计算条件")
    @TableField("calculation_condition")
    @Excel(name = "取数规则-计算条件", width = 15)
    private String calculationCondition;

    @Schema(description="取数规则-计算次数")
    @TableField("calculation_number")
    @Excel(name = "取数规则-计算次数", width = 15)
    private String calculationNumber;

    @Schema(description="取数规则-取结果值类型")
    @TableField("result_type")
    @Excel(name = "取数规则-取结果值类型", width = 15)
    private String resultType;

    @Schema(description="取数规则-结果值模型")
    @TableField("result_model")
    @Excel(name = "取数规则-结果值模型", width = 15)
    private String resultModel;

    @Schema(description="公式编码")
    @TableField("formula_code")
    @Excel(name = "公式编码", width = 15)
    private String formulaCode;

    @Schema(description="公式描述")
    @TableField("formula_describe")
    @Excel(name = "公式描述", width = 15)
    private String formulaDescribe;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description="排序")
    private Integer sort;

    @Schema(description="是否覆盖 0-否，1-是")
    @TableField("if_cover")
    @Excel(name = "是否覆盖 0-否，1-是", width = 15)
    private String ifCover;

    @Schema(description="是否删除 0-正常 1-删除")
    @TableField("del_flag")
    @TableLogic
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
