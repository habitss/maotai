package com.keytech.modules.pi.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * &#064;Description: PI岗位绑定
 * &#064;Author: jeecg-boot
 * &#064;Date:   2025-07-14
 * &#064;Version: V1.0
 */
@Data
@TableName("mes3_pi_post")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI岗位绑定")
public class PiPost implements Serializable {
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

	/**岗位id*/
	@Excel(name = "岗位id", width = 15)
    @Schema(description = "岗位id")
    @Dict(dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    private String postId;

	/**关联表*/
	@Excel(name = "关联表", width = 15)
    @Schema(description = "关联表")
    @Dict(dictTable ="mes3_pi_customize_table", dicText = "name", dicCode = "table_name")
    private String relatedTable;

	/**关联字段*/
	@Excel(name = "关联字段", width = 15)
    @Schema(description = "关联字段")
    private String relatedField;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
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

    /**是否自动报表 1自动报表 0手动报表*/
    @Schema(description="是否自动报表")
    @TableField(exist = false)
    private String automatic;

    @Serial
    private static final long serialVersionUID = 1L;
}
