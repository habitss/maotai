package com.keytech.modules.post.check.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
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
 * @Description: 岗位记录填报主表
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Data
@TableName("mes3_post_check_record_datainfo_main")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="岗位记录填报主表")
public class PostCheckRecordDatainfoMain implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

    @Schema(description="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

    /**工段*/
    @Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**配置主表id*/
	@Excel(name = "配置主表id", width = 15)
    @Schema(description = "配置主表id")
    @Dict(dictTable ="mes3_post_check_record_config_main",dicText = "report_name",dicCode = "id")
    private String configMainId;

	/**填报日期*/
	@Excel(name = "填报日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "填报日期")
    private Date fillingDate;

	/**班次*/
	@Excel(name = "班次", width = 15)
    @Schema(description = "班次")
    @Dict(dictTable ="mes3_mdm_shift_manage",dicText = "shift_name",dicCode = "id")
    private String classes;

	/**班组*/
	@Excel(name = "班组", width = 15)
    @Schema(description = "班组")
    @Dict(dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    private String teamsGroups;

	/**当班操作工*/
	@Excel(name = "当班操作工", width = 15)
    @Schema(description = "当班操作工")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String operator;

	/**确认人*/
	@Excel(name = "确认人", width = 15)
    @Schema(description = "确认人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String confirm;

	/**配置-主表(历史配置)*/
	@Excel(name = "配置-主表(历史配置)", width = 15)
    @Schema(description = "配置-主表(历史配置)")
    private String configMain;

	/**配置-项目(历史配置)*/
	@Excel(name = "配置-项目(历史配置)", width = 15)
    @Schema(description = "配置-项目(历史配置)")
    private String configProject;

	/**配置-标准(历史配置)*/
	@Excel(name = "配置-标准(历史配置)", width = 15)
    @Schema(description = "配置-标准(历史配置)")
    private String configStandard;

	/**备注内容*/
	@Excel(name = "备注内容", width = 15)
    @Schema(description = "备注内容")
    private String notesContent;

	/**订单*/
	@Excel(name = "订单", width = 15)
    @Schema(description = "订单")
    private String orderNo;

	/**批次*/
	@Excel(name = "批次", width = 15)
    @Schema(description = "批次")
    private String batch;

	/**品种*/
	@Excel(name = "品种", width = 15)
    @Schema(description = "品种")
    private String variety;

	/**设备*/
	@Excel(name = "设备", width = 15)
    @Schema(description = "设备")
    private String device;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**是否删除 0-正常 1-删除*/
	@Excel(name = "是否删除 0-正常 1-删除", width = 15)
    @Schema(description = "是否删除 0-正常 1-删除")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableField(exist = false)
    private List<PostCheckRecordDatainfoDetail> detailList;

    private static final long serialVersionUID = 1L;
}
