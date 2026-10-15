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
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 最佳实践推荐表明细
 * @Author: jeecg-boot
 * @Date:   2025-03-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_recommend_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践推荐表明细")
public class BestPracticeRecommendDetail implements Serializable {


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

	/**推荐表ID*/
	@Excel(name = "推荐表ID", width = 15)
    @Schema(description = "推荐表ID")
    private String recommendId;

    /**改善效果id*/
    @Excel(name = "改善效果id", width = 15)
    @Schema(description = "改善效果id")
    private String improvementTypeId;

    /**改善效果名称*/
    @Excel(name = "改善效果名称", width = 15)
    @Schema(description = "改善效果名称")
    private String improvementTypeName;

	/**改善前（量化数据）*/
	@Excel(name = "改善前（量化数据）", width = 15)
    @Schema(description = "改善前（量化数据）")
    private String beforeImprovement;

	/**改善后（量化数据）*/
	@Excel(name = "改善后（量化数据）", width = 15)
    @Schema(description = "改善后（量化数据）")
    private String afterImprovement;

	/**实际成本（量化数据）*/
	@Excel(name = "实际成本（量化数据）", width = 15)
    @Schema(description = "实际成本（量化数据）")
    private String actualCost;

	/**推广范围*/
	@Excel(name = "推广范围", width = 15)
    @Schema(description = "推广范围")
    private String promotionScope;

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
