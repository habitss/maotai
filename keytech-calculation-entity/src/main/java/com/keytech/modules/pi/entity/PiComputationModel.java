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
 * pi-计算模型
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 21:12:22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_computation_model")
@Schema(description="pi-计算模型")
public class PiComputationModel implements Serializable {

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

    @Schema(description="取值来源，0-influxDB,1-pi日报'")
    @TableField("value_source")
    @Excel(name = "取值来源，0-influxDB,1-pi日报'", width = 15)
    private Integer valueSource;

    @Schema(description="是否覆盖 0-否，1-是")
    @TableField("if_cover")
    @Excel(name = "是否覆盖 0-否，1-是", width = 15)
    private String ifCover;

    @Schema(description="是否汉化 0-否，1-是'")
    @TableField(value = "if_sinicized", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "是否汉化 0-否，1-是'", width = 15)
    private String ifSinicized;

    @Schema(description="汉化code")
    @TableField(value = "sinicized_dic", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "汉化code", width = 15)
    private String sinicizedDic;

    @Schema(description="公式编码")
    @TableField("formula_code")
    @Excel(name = "公式编码", width = 15)
    private String formulaCode;

    @Schema(description="公式描述")
    @TableField("formula_describe")
    @Excel(name = "公式描述", width = 15)
    private String formulaDescribe;

    @Schema(description="结果类型")
    @TableField(value = "result_type", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "结果类型", width = 15)
    private String resultType;

    @Schema(description="提前或延后，0提前 1延后")
    @TableField(value = "advance_delay", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "提前或延后，0提前 1延后", width = 15)
    private String advanceDelay;

    @Schema(description="唯一约束取值-提前或延后时间，单位秒")
    @TableField("advance_time")
    @Excel(name = "唯一约束取值-提前或延后时间，单位秒", width = 15)
    private String advanceTime;

    @Schema(description="排序字段")
    @TableField(value = "sort_field")
    @Excel(name = "排序字段", width = 15)
    private String sortField;

    @Schema(description="排序方式（DESC、ASC）")
    @TableField(value = "sort_mode")
    @Excel(name = "排序方式（DESC、ASC）", width = 15)
    private String sortMode;

    @Schema(description="过滤字段")
    @TableField(value = "filter_field")
    @Excel(name = "过滤字段", width = 15)
    private String filterField;

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

    @Schema(description="结果模型转换id")
    @TableField("result_model_id")
    @Excel(name = "结果模型转换id", width = 15)
    private String resultModelId;

    @Schema(description="结果模型转换名称")
    @TableField("result_model_name")
    @Excel(name = "结果模型转换名称", width = 15)
    private String resultModelName;

    @Schema(description="结果转换模型对象")
    @TableField(exist = false)
    private PiResultTransformationModel resultTransformationModel;

}
