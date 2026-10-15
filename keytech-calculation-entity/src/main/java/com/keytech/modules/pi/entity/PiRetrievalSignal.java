package com.keytech.modules.pi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * pi-取值信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 21:12:22
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_retrieval_signal")
@Schema(description="pi-计算信号表")
public class PiRetrievalSignal implements Serializable {

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

    @Schema(description="标识符")
    @TableField("identifier")
    @Excel(name = "标识符", width = 15)
    private String identifier;

    @Schema(description="基础点位代码")
    @TableField("point_code")
    @Excel(name = "基础点位代码", width = 15)
    private String pointCode;

    @Schema(description="基础点位名称")
    @TableField("point_name")
    @Excel(name = "基础点位名称", width = 15)
    private String pointName;

    @Schema(description="计算类型，1> 2>= 3= 4!= 5< 6<= 7进入 8结束")
    @TableField("calculate_type")
    @Excel(name = "计算类型", width = 15)
    private String calculateType;

    @Schema(description="计算值")
    @TableField("calculate_value")
    @Excel(name = "计算值", width = 15)
    private String calculateValue;

    @Schema(description="提前延迟(0提前/1延后)")
    @TableField("advance_delay")
    @Excel(name = "提前延迟", width = 15)
    private String advanceDelay;

    @Schema(description="提前延迟多少秒")
    @TableField("advance_time")
    @Excel(name = "提前延迟多少秒", width = 15)
    private String advanceTime;

    @Schema(description="取值类型(0值/1点位/2字典)")
    @TableField("value_type")
    @Excel(name = "取值类型", width = 15)
    private String valueType;

    @Schema(description="说明")
    @TableField("explanation")
    @Excel(name = "说明", width = 15)
    private String explanation;

    @Schema(description="排序")
    @TableField("sort")
    @Excel(name = "排序", width = 15)
    private Double sort;

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

    @Schema(description="适用于(0计算信号/1取数步骤)")
    @TableField("apply_to")
    @Excel(name = "适用于", width = 15)
    private String applyTo;

    /**是否显示(1是/0否)*/
    @Schema(description="是否显示")
    @Excel(name = "是否显示", width = 15)
    private String display;

    @Schema(description="条件是否成立")
    @TableField(exist = false)
    private Boolean conditionIsTrue =  false;

}
