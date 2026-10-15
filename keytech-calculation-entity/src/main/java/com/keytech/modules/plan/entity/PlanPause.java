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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * &#064;Description:  计划暂停或恢复记录
 * &#064;Author: jeecg-boot
 * &#064;Date:   2025-07-02
 * &#064;Version: V1.0
 */
@Data
@TableName("mes3_plan_pause")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="计划暂停或恢复记录")
public class PlanPause implements Serializable {


	/**主计划*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主计划")
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

	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    private String planId;

	/**记录类型2：恢复，5：暂停*/
	@Excel(name = "记录类型2：恢复，5：暂停", width = 15)
    @Schema(description = "记录类型2：恢复，5：暂停")
    private Integer type;

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
    @Dict(dictTable ="sys_user", dicText = "realname", dicCode = "username")
    private String createBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
