package com.keytech.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 工单管理
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_plan")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="工单管理")
public class Plan implements Serializable {
	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "主键(新增数据请删除该列的值)", width = 20)
    @Schema(description = "主键")
    private String id;

	/**租户id*/
    @Schema(description = "租户id")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

    @Schema(description="产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

	/**设备id*/
	@Excel(name = "设备(多个设备用,隔开)", width = 20, dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    @Schema(description = "设备id")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    private String deviceIds;

	/**订单号*/
	@Excel(name = "订单号", width = 15)
    @Schema(description = "订单号")
    private String orderName;

	/**计划名*/
	@Excel(name = "计划名", width = 15)
    @Schema(description = "计划名")
    private String planName;

    /**计划年月日*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划年月日")
    private Date ymd;

    /**预计开始时间*/
    @Excel(name = "预计开始时间", width = 20, format = "yyyy-MM-dd HH:mm")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    @Schema(description = "预计开始时间")
    private Date planStartTime;

    /**预计结束时间*/
    @Excel(name = "预计结束时间", width = 20, format = "yyyy-MM-dd HH:mm")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    @Schema(description = "预计结束时间")
    private Date planEndTime;

    @Schema(description = "恢复时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    private Date recoveryTime;

    /**计划大类*/
    @Excel(name = "计划大类", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE'",dicText = "item_key",dicCode = "item_val")
    @Schema(description = "计划大类")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE'",dicText = "item_key",dicCode = "item_val")
    private Integer planType;

    /**计划细类*/
    @Excel(name = "计划细类", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB'",dicText = "item_key",dicCode = "item_val")
    @Schema(description = "计划细类")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB'",dicText = "item_key",dicCode = "item_val")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String planTypeSub;

    /**计划状态*/
    @Schema(description = "计划状态")
    private Integer planStatus;

	/**生产状态*/
    @Schema(description = "生产状态")
    private Integer productionStatus;

	/**换线类型*/
    @Schema(description = "换线类型")
    private Integer changeLineType;

	/**前一个skuid*/
    @Schema(description = "前一个skuid")
    private Integer preSkuId;

	/**当前skuid*/
    @Schema(description = "当前skuid")
    private Integer skuId;

	/**单位*/
    @Schema(description = "单位")
    private String unit;

	/**计划产量*/
    @Schema(description = "计划产量")
    private Double yield;

	/**吨数*/
    @Schema(description = "吨数")
    private Double ton;

	/**计划瓶数*/
    @Schema(description = "计划瓶数")
    private Double bottleNum;

	/**罐号*/
    @Schema(description = "罐号")
    private String potNum;

	/**订单箱数*/
    @Schema(description = "订单箱数")
    private Double orderNameBoxNum;

	/**订单箱数小计*/
    @Schema(description = "订单箱数小计")
    private Double orderNameBoxNumSubtotal;

	/**预计上箱数*/
    @Schema(description = "预计上箱数")
    private Double planBoxesLoaded;

	/**预计上瓶数*/
    @Schema(description = "预计上瓶数")
    private Double planBottleLoaded;

	/**当前实际产量*/
    @Schema(description = "当前实际产量")
    private Double currentYield;

	/**实际开始时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际开始时间")
    private Date realStartTime;

	/**实际结束时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际结束时间")
    private Date realEndTime;

	/**计划时长 分钟*/
    @Schema(description = "计划时长 分钟")
    private Integer timeConsume;

	/**周*/
    @Schema(description = "周")
    private Integer week;

    /**年*/
    @Schema(description = "年")
    private Integer year;

	/**是否删除*/
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

	/**备注*/
    @Schema(description = "备注")
    private String remark;

    //计划操作岗位
    @Excel(name = "计划操作岗位", width = 15, dictTable ="mes3_mdm_post",dicText = "post_name",dicCode = "id")
    @Schema(description = "计划操作岗位")
    @Dict(dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    private String triggerPostId;

    @Schema(description = "延迟关闭时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date delayCloseTime;

    //岗位id
    @TableField(exist = false)
    private String postIds;

    //检查项名称
    @TableField(exist = false)
    private String taskItemName;

    private static final long serialVersionUID = 1L;
}
