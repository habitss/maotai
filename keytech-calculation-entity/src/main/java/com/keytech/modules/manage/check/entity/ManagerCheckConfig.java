package com.keytech.modules.manage.check.entity;

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
 * @Description: 分层检查-检查识别矩阵
 * @Author: jeecg-boot
 * @Date:   2025-04-08
 * @Version: V1.0
 */
@Data
@TableName("mes3_manager_check_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="分层检查-检查识别矩阵")
public class ManagerCheckConfig implements Serializable {


	/**id*/
    @Excel(name = "ID", width = 20)
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户id*/
    @Schema(description = "租户id")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线编码*/
    @Excel(name = "产线", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "产线")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**检查项编号*/
	@Excel(name = "检查项编号", width = 15)
    @Schema(description = "检查项编号")
    private String checkBh;

	/**常规活动*/
	@Excel(name = "常规活动", width = 15)
    @Schema(description = "常规活动")
    private String cghd;

	/**检查项来源*/
	@Excel(name = "检查项来源", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_SOURCE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "检查项来源")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_SOURCE'", dicText = "item_key", dicCode = "item_val")
    private String jcxlyDm;

	/**检查体系*/
	@Excel(name = "检查体系", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_TX'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "检查体系")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_TX'", dicText = "item_key", dicCode = "item_val")
    private String jctxDm;

	/**检查内容*/
	@Excel(name = "检查内容", width = 15)
    @Schema(description = "检查内容")
    private String jcnr;

	/**岗位配置-json*/
	@Excel(name = "岗位配置-json", width = 15)
    @Schema(description = "岗位配置-json")
    private String gwpzJson;

	/**是否必须在生产下执行0否，1是*/
	@Excel(name = "是否必须在生产下执行", width = 15, replace = { "是_1", "否_2" })
    @Schema(description = "是否必须在生产下执行")
    private Integer ifZx;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

    @TableField(exist = false)
    @Schema(description = "是否配置频率,1:是，0否")
    private Integer isConfig;

    @TableField(exist = false)
    @Schema(description = "岗位id")
    private String postId;

    private static final long serialVersionUID = 1L;
}
