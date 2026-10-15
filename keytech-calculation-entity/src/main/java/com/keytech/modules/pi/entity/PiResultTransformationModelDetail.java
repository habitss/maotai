package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: PI-结果转换模型配置
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_result_transformation_model_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI-结果转换模型配置")
public class PiResultTransformationModelDetail implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factory;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String dept;

	/**计算信号id*/
	@Excel(name = "计算信号id", width = 15)
    @Schema(description = "计算信号id")
    private String modelId;

	/**取数规则-左偏差值*/
	@Excel(name = "取数规则-左偏差值", width = 15)
    @Schema(description = "取数规则-左偏差值")
    private String leftValue;

	/**取数规则-左偏差条件(大于/小于/大于等于)*/
	@Excel(name = "取数规则-左偏差条件(大于/小于/大于等于)", width = 15)
    @Schema(description = "取数规则-左偏差条件(大于/小于/大于等于)")
    private String leftCondition;

	/**取数规则-右偏差条件(大于/小于/大于等于)*/
	@Excel(name = "取数规则-右偏差条件(大于/小于/大于等于)", width = 15)
    @Schema(description = "取数规则-右偏差条件(大于/小于/大于等于)")
    private String rightCondition;

	/**取数规则-右偏差值*/
	@Excel(name = "取数规则-右偏差值", width = 15)
    @Schema(description = "取数规则-右偏差值")
    private String rightValue;

	/**输出方式0自定义输出 1函数输出*/
	@Excel(name = "输出方式0自定义输出 1函数输出", width = 15)
    @Schema(description = "输出方式0自定义输出 1函数输出")
    private Integer outputType;

	/**输出值*/
	@Excel(name = "输出值", width = 15)
    @Schema(description = "输出值")
    private String outputValue;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

    private static final long serialVersionUID = 1L;

    //规则字符
    @TableField(exist = false)
    private String conditionJson;
}
