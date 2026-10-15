package com.keytech.modules.plan.entity;

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
 * @Description: 工单记录配置表，用于存储工单生成时候的任务条目配置
 * @Author: jeecg-boot
 * @Date:   2025-06-19
 * @Version: V1.0
 */
@Data
@TableName("mes3_plan_order_result_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="工单记录配置表，用于存储工单生成时候的任务条目配置")
public class PlanOrderResultConfig implements Serializable {


	/**任务条目关联*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "任务条目关联")
    private String id;

	/**租户id*/
	@Excel(name = "租户id", width = 15)
    @Schema(description = "租户id")
    private Integer tenantId;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factoryCode;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String deptCode;

	/**产线code*/
	@Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

	/**任务id*/
	@Excel(name = "任务id", width = 15)
    @Schema(description = "任务id")
    private String taskId;

	/**条目id*/
	@Excel(name = "条目id", width = 15)
    @Schema(description = "条目id")
    private Integer itemId;

    /**条目检查项目*/
    @Excel(name = "条目检查项目", width = 15)
    @Schema(description = "条目检查项目")
    private String itemName;

    /**条目检查标准*/
    @Excel(name = "条目检查标准", width = 15)
    @Schema(description = "条目检查标准")
    private String itemStandard;

    /**条目显示内容*/
    @Excel(name = "条目检查内容", width = 15)
    @Schema(description = "条目检查内容")
    private String detail;

    /**条目结果类型*/
    @Excel(name = "条目结果类型", width = 15)
    @Schema(description = "条目结果类型")
    private String resultTypeCode;

    /**检查地点*/
    @Excel(name = "检查地点", width = 15)
    @Schema(description = "检查地点")
    private String itemLocation;

    /**该条目是否需要上传图片*/
    @Excel(name = "该条目是否需要上传图片", width = 15)
    @Schema(description = "该条目是否需要上传图片")
    private Integer isUploadImg;

    /**该条目是否需要检查时间*/
    @Excel(name = "该条目是否需要检查时间", width = 15)
    @Schema(description = "该条目是否需要检查时间")
    private Integer isCheckTime;

    /**该条目是否需要备注*/
    @Excel(name = "该条目是否需要备注", width = 15)
    @Schema(description = "该条目是否需要备注")
    private Integer isRemark;

    /**该条目是否需要确认人*/
    @Excel(name = "该条目是否需要确认人", width = 15)
    @Schema(description = "该条目是否需要确认人")
    private Integer isConfirmUser;

    /**该条目是否需要对结果进行合格判断*/
    @Excel(name = "该条目是否需要对结果进行合格判断", width = 15)
    @Schema(description = "该条目是否需要对结果进行合格判断")
    private Integer isQualified;

    /**是否必填*/
    @Excel(name = "是否必填", width = 15)
    @Schema(description = "是否必填")
    private Integer isRequire;

    /**该条目是否可以NA取消*/
    @Excel(name = "该条目是否可以NA取消", width = 15)
    @Schema(description = "该条目是否可以NA取消")
    private Integer isNa;

    /**结果历史配置id*/
    @Schema(description = "结果历史配置id")
    private String resultConfigId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**是否删除*/
	@Excel(name = "是否删除", width = 15)
    @Schema(description = "是否删除")
    @TableLogic
    private Integer delFlag;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    /**推送频率备注*/
    @Schema(description = "推送频率备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}
