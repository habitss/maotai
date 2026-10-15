package com.keytech.modules.sop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.sop.dto.WriteTwoRrDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: SOP 清单
 * @Author: jeecg-boot
 * @Date:   2025-04-11
 * @Version: V1.0
 */
@Data
@TableName("mes3_sop_home_page")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="SOP 清单")
public class SopHomePage implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "主键（勿删）", width = 15)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "工厂Code")

    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "产线Code")
    private String lineCode;

	/**sop类型*/
	@Excel(name = "SOP类型", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_TYPE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "SOP类型")
    private String sopType;

	/**sop活动*/
	@Excel(name = "SOP活动", width = 15)
    @Schema(description = "SOP活动")
    private String sopActivity;

	/**sop岗位*/
	@Excel(name = "SOP岗位", width = 15)
    @Schema(description = "SOP岗位")
    private String sopPost;

	/**sop编号*/
	@Excel(name = "SOP编号", width = 15)
    @Schema(description = "SOP编号")
    private String sopNumber;

	/**sop唯一描述*/
	@Excel(name = "SOP名称", width = 15)
    @Schema(description = "SOP名称")
    private String sopDesc;

	/**sop上传记录*/
    @Schema(description = "SOP上传记录")
    private String sopDescStr;

	/**关键sop*/
    @Schema(description = "关键sop")
    private Integer sopState;

	/**owd频次*/
    @Schema(description = "owd频次")
    private Integer owdFrequency;

	/**编写职责*/
    @Schema(description = "编写职责")
    private String writeTwoRr;

	/**最新版版本号*/
    @Schema(description = "最新版版本号")
    private String versionNumber;

	/**实施时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实施时间")
    private Date putIntoTime;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
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

    /**是否需要诊断*/
    @Excel(name = "是否需要诊断", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否需要诊断")
    private Integer diagnosis;

    private static final long serialVersionUID = 1L;

    /**
     * 部门-查询条件
     */
    @TableField(exist = false)
    private String deptQuery;

    /**最新需求，SOP业务调整，SOP回顾计划，将最新的关键SOP、回顾频次、最新版版本号、实施时间需要从回顾计划中取 **/
    @TableField(exist = false)
    private SopReviewPlan sopReviewPlan;

    /**编写人*/
    @TableField(exist = false)
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String putIntoNames;

    /**编写职责List*/
    @TableField(exist = false)
    private List<WriteTwoRrDto> writeTwoRrList;

    /**诊断计划对象-最新一条*/
    @TableField(exist = false)
    private SopStatisticData statisticData;
}
