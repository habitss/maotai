package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * pi-计算信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 21:12:22
 */
@JsonIgnoreProperties(value = {"tenantId","delFlag","source"})
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_calculate_signal")
@Schema(description="pi-计算信号表")
public class PiCalculateSignal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Excel(name = "主键", width = 15)
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

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

    @Schema(description="pi存储表")
    @TableField("pi_table")
    @Excel(name = "pi存储表", width = 15)
    private String piTable;

    @Schema(description="pi编码")
    @TableField("pi_code")
    @Excel(name = "pi编码", width = 15)
    private String piCode;

    @Schema(description="pi名称")
    @TableField("pi_name")
    @Excel(name = "pi名称", width = 15)
    private String piName;

    @Schema(description="基础点位代码")
    @TableField("point_code")
    @Excel(name = "基础点位代码", width = 15)
    @Dict(dictTable ="mes3_pi_base_point",dicText = "point_name",dicCode = "point_code")
    private String pointCode;

    @Schema(description="基础点位名称")
    @TableField("point_name")
    @Excel(name = "基础点位名称", width = 15)
    private String pointName;

    @Schema(description="计算类型")
    @TableField("calculate_type")
    @Excel(name = "计算类型", width = 15)
    private String calculateType;

    @Schema(description="计算值")
    @TableField("calculate_value")
    @Excel(name = "计算值", width = 15)
    private String calculateValue;

    @Schema(description="说明")
    @TableField("explanation")
    @Excel(name = "说明", width = 15)
    private String explanation;

    @Schema(description="唯一约束")
    @TableField("unique_json")
    @Excel(name = "唯一约束", width = 15)
    private String uniqueJson;

    @Schema(description="唯一约束取值-提前或延后，0提前 1延后")
    @TableField(value = "advance_delay", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "唯一约束取值-提前或延后，0提前 1延后", width = 15)
    private String advanceDelay;

    @Schema(description="唯一约束取值-提前或延后时间，单位秒")
    @TableField("advance_time")
    @Excel(name = "唯一约束取值-提前或延后时间，单位秒", width = 15)
    private String advanceTime;

    @Schema(description="步骤类型，0瞬时取数 1范围取数 2监控取数")
    @TableField("step_type")
    @Excel(name = "步骤类型，0瞬时取数 1范围取数 2监控取数", width = 15)
    private String stepType;

    //
    @Schema(description="是否启用: 0-禁用, 1-启用")
    @Excel(name = "是否启用: 0-禁用, 1-启用", width = 15)
    private Integer isEnabled;

    @Schema(description="是否删除 0-正常 1-删除")
    @TableField("del_flag")
//    @TableLogic(value = "0", delval = "id")
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

    @Schema(description="点位类型,0代表自动化点位,1代表时间")
    @TableField("point_type")
    @Excel(name = "点位类型,0代表自动化点位,1代表时间", width = 15)
    private Integer pointType;

    @Schema(description="时间类型，0整点小时，1偶数小时，2指定时间")
    @TableField("time_type")
    @Excel(name = "时间类型，0整点小时，1偶数小时，2指定时间", width = 15)
    private Integer timeType;

    @Schema(description="time_type为2的时候，显示时分，例如：09:30")
    @TableField("time_comment")
    @Excel(name = "time_type为2的时候，显示时分，例如：09:30", width = 15)
    private String timeComment;

    @Schema(description="结果模型转换id")
    @TableField("result_model_id")
    @Excel(name = "结果模型转换id", width = 15)
    private String resultModelId;

    @Schema(description="结果模型转换名称")
    @TableField("result_model_name")
    @Excel(name = "结果模型转换名称", width = 15)
    private String resultModelName;

    @Schema(description="唯一约束字符串解析:map格式：colName唯一约束字段中文名、colCode唯一约束字段名、tagCode字段对应的点位code、tagValue字段对应的点位value")
    @TableField(exist = false)
    private List<Map<String, Object>> listUniqueMap;

    @Schema(description="触发时间")
    @TableField(exist = false)
    private LocalDateTime calculateTime;

    @Schema(description="唯一约束对应自动化的tagCode")
    @TableField(exist = false)
    private List<String> listUniqueTagCode;

    @Schema(description="重算标记 1自动计算，2手动重算，3自动重算")
    @TableField(exist = false)
    private int reCalculateBj;

    @Schema(description="PI配置信息集合")
    @TableField(exist = false)
    private List<PiCustomizeTableField> customizeTableFieldList;

    @Schema(description="取值信号集合")
    @TableField(exist = false)
    private List<PiRetrievalSignal> piRetrievalSignalList;

    @Schema(description="取值步骤集合")
    @TableField(exist = false)
    private List<PiRetrievalStep> piRetrievalStepList;

    @Schema(description="计算模型集合")
    @TableField(exist = false)
    private List<PiComputationModel> piComputationModelList;

    @Schema(description="监控型算法集合")
    @TableField(exist = false)
    private List<PiComputationMonitorModel> piComputationMonitorModelList;

    @Schema(description="取数信号-描述(说明)")
    @TableField(exist = false)
    private String retrievalExplanation;

    @Schema(description="计算模型-描述")
    @TableField(exist = false)
    private String formulaDescribe;

}
