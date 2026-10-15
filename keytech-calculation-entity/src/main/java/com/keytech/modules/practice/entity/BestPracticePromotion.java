package com.keytech.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @Description: 最佳实践评估
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_promotion")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践评估")
public class BestPracticePromotion implements Serializable {


	/**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
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

	/**名称*/
	@Excel(name = "名称", width = 15)
    @Schema(description = "名称")
    private String title;

	/**编号*/
	@Excel(name = "编号", width = 15)
    @Schema(description = "编号")
    private String serialNumber;

	/**年月*/
	@Excel(name = "年月", width = 15)
    @Schema(description = "年月")
    private String yearMon;

	/**提报工厂*/
	@Excel(name = "提报工厂", width = 15)
    @Schema(description = "提报工厂")
    private String reportFactory;

	/**提报部门*/
	@Excel(name = "提报部门", width = 15)
    @Schema(description = "提报部门")
    @Dict(dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
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

	/**评审会意见*/
	@Excel(name = "评审会意见", width = 15)
    @Schema(description = "评审会意见")
    private String reviewOpinion;

	/**评审结果(强制推广≥23分；建议推广≥18分；不推广＜18分)*/
	@Excel(name = "评审结果(强制推广≥23分；建议推广≥18分；不推广＜18分)", width = 15)
    @Schema(description = "评审结果(强制推广≥23分；建议推广≥18分；不推广＜18分)")
    private Integer reviewResult;

	/**发布时间*/
	@Excel(name = "发布时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "发布时间")
    private Date reviewTime;

	/**识别部门*/
	@Excel(name = "识别部门", width = 15)
    @Schema(description = "识别部门")
    private String identificationShop;

    /**关联数据id*/
    @Schema(description = "关联数据id")
    private String dataId;

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
