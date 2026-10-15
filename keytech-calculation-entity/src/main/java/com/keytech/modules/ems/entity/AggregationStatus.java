package com.keytech.modules.ems.entity;

import java.io.Serializable;
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
 * 聚合计算状态表
 * @author yepeng
 * @since 2025.05.12
 * */
@Data
@TableName("ems_aggregation_status")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="聚合计算状态表")
public class AggregationStatus implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**工厂Code*/
	@Excel(name = "工厂", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "工厂Code")
    private String factoryCode;
	/**部门Code*/
	@Excel(name = "部门", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;
	/**产线Code*/
	@Excel(name = "产线", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
	@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    private String lineCode;
	/**报表ID*/
	@Dict(dictTable = "ems_custom_table", dicText = "rep_name", dicCode = "id")
    @Schema(description = "报表ID")
    private Integer tableId;
	/**字段Code*/
	@Dict(dictTable = "ems_custom_field", dicText = "field_name", dicCode = "id")
    @Schema(description = "字段Code")
    private String fieldCode;
    /**点位编码*/
    @Excel(name = "点位编码", width = 15)
    @Schema(description = "点位编码")
    private String tagId;
	/**粒度类型(小时/班/天/周/月)*/
	@Excel(name = "粒度类型", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "粒度类型(小时/班/天/周/月)")
    private Integer grainType;
	/**点位类型(collect/thirdparty/ors)*/
	@Excel(name = "点位类型", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_TagKind'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_TagKind'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "点位类型(collect/thirdparty/ors)")
    private Integer tagKind;
	/**时间点*/
	@Excel(name = "时间点", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "时间点")
    private LocalDateTime timePoint;
	/**是否完成(0-未完成 1-完成)*/
	@Excel(name = "是否完成", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否完成(0-未完成 1-完成)")
    private Integer finishFlag;
	/**是否异常(0-正常 1-异常)*/
	@Excel(name = "是否异常", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否异常(0-正常 1-异常)")
    private Integer errorFlag;
    /**详细错误信息*/
    @Excel(name = "详细错误信息", width = 15)
    @Schema(description = "详细错误信息")
    private String errorMsg;
	/**重试次数*/
	@Excel(name = "重试次数", width = 15)
    @Schema(description = "重试次数")
    private Integer retryCount;
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

    @Schema(description = "点位名称")
    @TableField(exist = false)
    private String tagName;
}
