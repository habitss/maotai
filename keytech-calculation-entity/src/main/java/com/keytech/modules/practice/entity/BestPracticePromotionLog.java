package com.keytech.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 最佳实践推广记录
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_promotion_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践推广记录")
public class BestPracticePromotionLog implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
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

	/**评估表ID(mes3_best_practice_promotion)*/
	@Excel(name = "评估表ID(mes3_best_practice_promotion)", width = 15)
    @Schema(description = "评估表ID(mes3_best_practice_promotion)")
    private String proId;

	/**是否展示 1: 展示  0:不展示*/
	@Excel(name = "是否展示 1: 展示  0:不展示", width = 15)
    @Schema(description = "是否展示 1: 展示  0:不展示")
    private Integer status;

	/**名称*/
	@Excel(name = "名称", width = 15)
    @Schema(description = "名称")
    private String title;

	/**编号*/
	@Excel(name = "编号", width = 15)
    @Schema(description = "编号")
    private String serialNumber;

	/**年*/
	@Excel(name = "年", width = 15)
    @Schema(description = "年")
    private String yearMon;

	/**提报工厂编码*/
	@Excel(name = "提报工厂编码", width = 15)
    @Schema(description = "提报工厂编码")
    private String reportFactory;

	/**提报部门编码*/
	@Excel(name = "提报部门编码", width = 15)
    @Schema(description = "提报部门编码")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String reportWorkshop;

	/**改善类型*/
	@Excel(name = "改善类型", width = 15)
    @Schema(description = "改善类型")
    @Dict(dictTable ="mes3_best_practice_type",dicText = "type_name",dicCode = "id")
    private String reportType;

    /**对应KPI年份*/
    @Excel(name = "对应KPI年份", width = 15)
    @Schema(description = "对应KPI年份")
    private String kpiYear;

	/**对应KPI*/
	@Excel(name = "对应KPI", width = 15)
    @Schema(description = "对应KPI")
    @Dict(dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "id")
    private String kpiCode;

	/**最佳实践评审得分*/
	@Excel(name = "最佳实践评审得分", width = 15)
    @Schema(description = "最佳实践评审得分")
    private String reviewScore;

	/**适用部门编码*/
	@Excel(name = "适用部门编码", width = 15)
    @Schema(description = "适用部门编码")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String applicableWorkshopId;

	/**对应KPI是否好于提报部门*/
	@Excel(name = "对应KPI是否好于提报部门", width = 15)
    @Schema(description = "对应KPI是否好于提报部门")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer deptType;

	/**对应设备,工艺是否一致*/
	@Excel(name = "对应设备,工艺是否一致", width = 15)
    @Schema(description = "对应设备,工艺是否一致")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer type;

	/**识别结果(是否推广)*/
	@Excel(name = "识别结果(是否推广)", width = 15)
    @Schema(description = "识别结果(是否推广)")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer identifyType;

    /**推广原因*/
    @Excel(name = "推广原因", width = 15)
    @Schema(description = "推广原因")
    private String promotedReason;

    /**未推广原因*/
    @Excel(name = "未推广原因", width = 15)
    @Schema(description = "未推广原因")
    private String unpromotedReason;

	/**推广行动计划*/
	@Excel(name = "推广行动计划", width = 15)
    @Schema(description = "推广行动计划")
    private String promotionActionPlan;

	/**行动计划id*/
	@Excel(name = "是否添加行动计划1:是，0：否", width = 15)
    @Schema(description = "是否添加行动计划1:是，0：否")
    private Integer ifTrack;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
