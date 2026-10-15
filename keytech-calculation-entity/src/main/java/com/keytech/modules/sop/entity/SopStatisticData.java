package com.keytech.modules.sop.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: SOP-诊断计划
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Data
@TableName("mes3_sop_statistic_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="SOP-诊断计划")
public class SopStatisticData implements Serializable {

    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "主键(如果是新增被诊断人请删除主键)", width = 15)
    @Schema(description = "id")
    private String id;

    /**sop首页关联ID*/
    @Schema(description = "SOP首页关联ID")
    @Excel(name = "SOP主键（勿删）", width = 15,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    private String sopId;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    /**SOP编号*/
//    @TableField(exist = false)
    @Excel(name = "SOP编号", width = 15)
    @Schema(description = "SOP编号")
    private String sopNumber;

    /**SOP名称*/
//    @TableField(exist = false)
    @Excel(name = "SOP名称", width = 15)
    @Schema(description = "SOP名称")
    private String sopDesc;

	/**诊断负责人*/
	@Excel(name = "诊断负责人", width = 15,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "诊断负责人")
    private String checkUserId;

	/**上一次诊断年份*/
	@Excel(name = "上一次诊断年份", width = 15)
    @Schema(description = "上一次诊断年份")
    private String lastCheckYear;

	/**诊断频率*/
    @Excel(name = "诊断频率", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "诊断频率")
    private String checkInterval;

    /**被诊断人*/
    @Excel(name = "被诊断人", width = 15,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "被诊断人")
    private String byCheckUserId;

    /**SOP类型*/
    @Schema(description = "SOP类型")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_TYPE'", dicText = "item_key", dicCode = "item_val")
    private String sopType;


	/**设备工艺变更 0:无 1:有*/
    @Schema(description = "设备工艺变更 0:无 1:有")
    private Integer deviceCraftUpdate;

	/**员工状态 1熟练员工、2转岗员工、3新员工*/
    @Schema(description = "员工状态 1熟练员工、2转岗员工、3新员工")
    private String employeeState;

	/**办公室诊断 1:是  0:否*/
    @Schema(description = "办公室诊断 1:是  0:否")
    private String officeFileState;

	/**现场诊断 1:是  0:否*/
    @Schema(description = "现场诊断 1:是  0:否")
    private String sceneCheck;

	/**year*/
    @Schema(description = "year")
    @Excel(name = "年份(勿删)", width = 15)
    private Integer year;

	/**结果数据*/
    @Schema(description = "结果数据")
    private String resultData;

	/**排序*/
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


    /**当前最新状态 完成/计划/延迟*/
    @TableField(exist = false)
    @Schema(description = "当前最新状态")
    private String newState;

    /**部门-查询*/
    @TableField(exist = false)
    @Schema(description = "部门-查询条件")
    private String deptQuery;
    /**sop翻译*/
    @TableField(exist = false)
    @Schema(description = "SOP翻译")
    private String sopName;
    /**sop活动*/
    @TableField(exist = false)
    @Schema(description = "SOP活动")
    private String sopActivity;

    /**回顾责任人*/
    @TableField(exist = false)
    @Schema(description = "回顾责任人")
    private String responsibleUserName;

    /**回顾咨询人*/
    @TableField(exist = false)
    @Schema(description = "回顾咨询人")
    private String seekUserName;

    /**回顾通知人*/
    @TableField(exist = false)
    @Schema(description = "回顾通知人")
    private String noticeUserName;

    /**诊断间隔名称*/
    @TableField(exist = false)
    @Schema(description = "诊断间隔名称")
    private String checkIntervalName;

    /**已完成*/
    @TableField(exist = false)
    @Schema(description = "已完成")
    private String completed;

    /**计划*/
    @TableField(exist = false)
    @Schema(description = "计划")
    private String underWay;

    /**延迟*/
    @TableField(exist = false)
    @Schema(description = "延迟")
    private String delay;

}
