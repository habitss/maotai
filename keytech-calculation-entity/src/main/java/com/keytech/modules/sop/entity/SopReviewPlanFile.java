package com.keytech.modules.sop.entity;

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
 * @Description: SOP管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Data
@TableName("mes3_sop_review_plan_file")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="SOP管理回顾-文件")
public class SopReviewPlanFile implements Serializable {


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

	/**回顾计划id*/
	@Excel(name = "回顾计划id", width = 15)
    @Schema(description = "回顾计划id")
    private String reviewPlanId;

	/**回顾人*/
	@Excel(name = "回顾人", width = 15)
    @Schema(description = "回顾人")
    private String review;

	/**版本号*/
	@Excel(name = "版本号", width = 15)
    @Schema(description = "版本号")
    private String version;

	/**实施时间*/
	@Excel(name = "实施时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实施时间")
    private Date operateTime;

	/**变更内容*/
	@Excel(name = "变更内容", width = 15)
    @Schema(description = "变更内容")
    private String changes;

	/**文件路径*/
	@Excel(name = "文件路径", width = 15)
    @Schema(description = "文件路径")
    private String url;

	/**周*/
	@Excel(name = "周", width = 15)
    @Schema(description = "周")
    private String week;

	/**文件名称(文件名不能重复)*/
	@Excel(name = "文件名称(文件名不能重复)", width = 15)
    @Schema(description = "文件名称(文件名不能重复)")
    private String fileName;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

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
}
