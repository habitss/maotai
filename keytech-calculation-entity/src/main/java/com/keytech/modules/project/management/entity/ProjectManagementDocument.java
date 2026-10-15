package com.keytech.modules.project.management.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: 项目清单
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_project_management_document")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="项目清单")
public class ProjectManagementDocument implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**项目id*/
	@Excel(name = "项目id", width = 15)
    @Schema(description = "项目id")
    private String projectId;

	/**年*/
	@Excel(name = "年", width = 15)
    @Schema(description = "年")
    private String documentYear;

	/**月*/
	@Excel(name = "月", width = 15)
    @Schema(description = "月")
    private String documentMonth;

	/**本月计划节点 id*/
	@Excel(name = "本月计划节点 id", width = 15)
    @Schema(description = "本月计划节点 id")
    private String planNodeIds;

    /**本月实际完成节点 id*/
    @Excel(name = "本月实际完成节点 id", width = 15)
    @Schema(description = "本月实际完成节点 id")
    private String realNodeIds;

    /**效果*/
    @Excel(name = "效果", width = 15)
    @Schema(description = "效果")
    private String effect;

	/**延期说明以及改进*/
	@Excel(name = "延期说明以及改进", width = 15)
    @Schema(description = "延期说明以及改进")
    private String improveRemark;

	/**立项计划分*/
	@Excel(name = "立项计划分", width = 15)
    @Schema(description = "立项计划分")
    private Double planApprovalScore;

	/**主目标计划分*/
	@Excel(name = "主目标计划分", width = 15)
    @Schema(description = "主目标计划分")
    private Double planTargetScore;

	/**举措目标计划分*/
	@Excel(name = "举措目标计划分", width = 15)
    @Schema(description = "举措目标计划分")
    private Double planActScore;

    /**结项计划分*/
    @Excel(name = "结项计划分", width = 15)
    @Schema(description = "结项计划分")
    private Double planEndScore;

    /**合计计划分*/
    @Excel(name = "合计计划分", width = 15)
    @Schema(description = "合计计划分")
    private Double planTotalScore;

	/**立项加分*/
	@Excel(name = "立项加分", width = 15)
    @Schema(description = "立项加分")
    private Double addApprovalScore;

	/**主目标加分*/
	@Excel(name = "主目标加分", width = 15)
    @Schema(description = "主目标加分")
    private Double addTargetScore;

	/**举措目标加分*/
	@Excel(name = "举措目标加分", width = 15)
    @Schema(description = "举措目标加分")
    private Double addActScore;

    /**结项加分*/
    @Excel(name = "结项加分", width = 15)
    @Schema(description = "结项加分")
    private Double addEndScore;

    /**合计加分*/
    @Excel(name = "合计加分", width = 15)
    @Schema(description = "合计加分")
    private Double addTotalScore;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

	/**是否删除（1是0否）*/
	@Excel(name = "是否删除（1是0否）", width = 15)
    @Schema(description = "是否删除（1是0否）")
    @TableLogic
    private Integer delFlag;

    private static final long serialVersionUID = 1L;
}
