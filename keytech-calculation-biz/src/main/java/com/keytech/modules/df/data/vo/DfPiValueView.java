package com.keytech.modules.df.data.vo;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DF视图数据
 * @author yepeng
 * @since 2025.12.12
 */
@Data
@TableName("df_pi_value_view")
@DS("source")
public class DfPiValueView {
    //事件ID
    private String EventID;
    //开始时间
    private LocalDateTime StartTime;
    //结束时间
    private LocalDateTime EndTime;
    //PI值
    private String PI;
    //其他参数
    private String Data;
    //EOP号
    private Integer EopID;
    //指标实例编码
    private String PICode;
    //指标名称
    private String PIName;
    //指标类型
    private String PIType;
    //UDE规则编码
    private String RuleID;
    //运行模式状态（生产、清洗、维户等，瞬时值）
    private Double Status;
    //步骤号
    private Integer StepID;
    //批次号
    private Integer BatchID;
    //订单号
    private Integer OrderID;
    //运行状态
    private Double Running;
    //实例ID
    private String ThingID;
    //配方号
    private Integer RecipeID;
    //UDE规则描述
    private String RuleName;
    //配方类型ID
    private Integer RecipeTypeID;
    //最后更新者
    private String UpdateBy;
    //最后更新时间
    private String UpdateTime;
}