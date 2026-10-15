package com.keytech.modules.ems.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * 点位采集状态表
 * @author yepeng
 * @since 2025.04.29
 * */
@Data
@TableName("ems_collect_point_status")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="点位采集状态表")
public class CollectPointStatus implements Serializable {
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
	@Excel(name = "报表", width = 15, dictTable = "ems_custom_table", dicText = "rep_name", dicCode = "id")
	@Dict(dictTable = "ems_custom_table", dicText = "rep_name", dicCode = "id")
    @Schema(description = "报表ID")
    private Integer tableId;
	/**字段Code*/
	@Excel(name = "字段", width = 15, dictTable = "ems_custom_field", dicText = "field_name", dicCode = "id")
	@Dict(dictTable = "ems_custom_field", dicText = "field_name", dicCode = "id")
    @Schema(description = "字段Code")
    private String fieldCode;
    /**点位编码*/
    @Excel(name = "点位编码", width = 15, dictTable = "mes3_pi_base_point", dicText = "point_name", dicCode = "tag_id")
    @Dict(dictTable = "mes3_pi_base_point", dicText = "point_name", dicCode = "tag_id")
    @Schema(description = "点位编码")
    private String tagId;
    /**粒度类型(半小时/小时)*/
    @Excel(name = "粒度类型(半小时/小时)", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_GrainType'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "粒度类型(半小时/小时)")
    private Integer grainType;
	/**采集时段起点*/
	@Excel(name = "采集时段起点", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "采集时段起点")
    private LocalDateTime collectTime;
	/**是否完成(0-未完成 1-完成)*/
	@Excel(name = "是否完成", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否完成(0-未完成 1-完成)")
    private Integer finishFlag;
	/**是否异常标记(0-正常 1-异常)*/
	@Excel(name = "是否异常", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'", dicText = "item_key", dicCode = "item_val")
	@Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "是否异常标记(0-正常 1-异常)")
    private Integer errorFlag;
	/**重试次数*/
	@Excel(name = "重试次数", width = 15)
    @Schema(description = "重试次数")
    private Integer retryCount;
    /**采集时段起点底数*/
    @Excel(name = "采集时段起点底数", width = 15)
    @Schema(description = "采集时段起点底数")
    private BigDecimal lastBaseVal;
    /**最新用量*/
    @Excel(name = "最新用量", width = 15)
    @Schema(description = "最新用量")
    private BigDecimal lastUsage;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
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

    @Schema(description = "Influx查询编码")
    @TableField(exist = false)
    private String fluxCode;
}
