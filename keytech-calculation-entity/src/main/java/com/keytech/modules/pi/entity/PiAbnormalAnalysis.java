package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: PI异常分析
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_abnormal_analysis")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI异常分析")
public class PiAbnormalAnalysis implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**数据表英文名称*/
	@Excel(name = "数据表英文名称", width = 15)
    @Schema(description = "数据表英文名称")
    private String tableCode;

	/**数据表中文名称*/
	@Excel(name = "数据表中文名称", width = 15)
    @Schema(description = "数据表中文名称")
    private String tableName;

	/**pi英文名称*/
	@Excel(name = "pi英文名称", width = 15)
    @Schema(description = "pi英文名称")
    private String fieldCode;

	/**pi中文名称*/
	@Excel(name = "pi中文名称", width = 15)
    @Schema(description = "pi中文名称")
    private String fieldName;

	/**数据id*/
	@Excel(name = "数据id", width = 15)
    @Schema(description = "数据id")
    private String dataId;

	/**pi实际值*/
	@Excel(name = "pi实际值", width = 15)
    @Schema(description = "pi实际值")
    private String piData;

	/**问题类别*/
	@Excel(name = "问题类别", width = 15)
    @Schema(description = "问题类别")
    private String problemType;

	/**异常发生时间*/
	@Excel(name = "异常发生时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "异常发生时间")
    private Date abnormalTime;

	/**状态*/
	@Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private Integer state;

	/**异常描述*/
	@Excel(name = "异常描述", width = 15)
    @Schema(description = "异常描述")
    private String abnormalDescribe;

    /**异常分析*/
    @Excel(name = "异常分析", width = 15)
    @Schema(description = "异常分析")
    private String abnormalAnalysis;

    /**异常类型*/
    @Excel(name = "异常类型", width = 15)
    @Schema(description = "异常类型")
    private String abnormalType;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;

    /**
     * 查询-开始时间
     */
    @TableField(exist = false)
    private String opTime;

    /**
     * 查询-结束时间
     */
    @TableField(exist = false)
    private String edTime;
    /**
     * 查询-产线
     */
//    @TableField(exist = false)
//    private String lineId;


    // 班组-评价结果
//    @TableField(exist = false)
//    private String evaluationResult;
//    // 班组-评价类型
//    @TableField(exist = false)
//    private String evaluationType;
//    // 车间-评价结果
//    @TableField(exist = false)
//    private String workshopEvaluationResult;
//    // 车间-评价类型
//    @TableField(exist = false)
//    private String workshopEvaluationType;

    /**
     * 班组/车间-评价
     */
    @TableField(exist = false)
    private List<PiAbnormalAnalysisEvaluation> evaluations;


}
