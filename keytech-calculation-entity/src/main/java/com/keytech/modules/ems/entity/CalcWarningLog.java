package com.keytech.modules.ems.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 采集计算预警日志表
 * @author yepeng
 * @since 2025.05.22
 * */
@Data
@TableName("ems_calc_warning_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="采集计算预警日志表")
public class CalcWarningLog implements Serializable {
    @Serial
	private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**工厂Code*/
    //@Excel(name = "工厂", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "工厂Code")
    private String factoryCode;
	/**部门Code*/
	@Excel(name = "部门", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;
	/**产线Code*/
	//@Excel(name = "产线", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    private String lineCode;
	/**关联点位*/
	@Excel(name = "关联点位", width = 15)
    @Schema(description = "关联点位ID")
    private String tagId;
	/**粒度类型(小时/班/天/周/月)*/
	@Excel(name = "粒度类型", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "粒度类型(小时/班/天/周/月)")
    private Integer grainType;
	/**点位大类(collect/thirdparty/ors)*/
	@Excel(name = "点位大类", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_TagKind'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_TagKind'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "点位大类(collect/thirdparty/ors)")
    private Integer tagKind;
	/**时间点*/
	@Excel(name = "时间点", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "时间点")
    private LocalDateTime timePoint;
	/**预警详情*/
	@Excel(name = "预警详情", width = 100)
    @Schema(description = "预警详情")
    private String warnMsg;
	/**预警等级(3高2中1低)*/
	@Excel(name = "预警等级", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_WarnLevel'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_WarnLevel'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "预警等级(3高2中1低)")
    private Integer warnLevel;
    /**是否推送消息*/
    @Excel(name = "是否推送消息", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_YN'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_YN'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否推送消息")
    private Integer pushFlag;
	/**排序号*/
    @Schema(description = "排序号")
    private Integer sort;
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**删除标记*/
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;

    @Schema(description = "开始时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(exist = false)
    private LocalDate startTime;
    @Schema(description = "结束时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(exist = false)
    private LocalDate endTime;
    @Schema(description = "点位名称")
    @TableField(exist = false)
    private String tagName;
}
