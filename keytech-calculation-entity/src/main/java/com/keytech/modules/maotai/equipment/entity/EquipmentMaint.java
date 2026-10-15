package com.keytech.modules.maotai.equipment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
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
 * 设备保养记录(保养工单)
 * @author maotai
 * @since 2026-06-15
 */
@Data
@TableName("mes3_maotai_equipment_maint")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备保养记录")
public class EquipmentMaint extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**工单号(后端生成:WO+yyyyMMdd+4位序号)*/
    @Excel(name = "工单号", width = 22)
    @Schema(description = "工单号")
    private String orderNo;

    /**所属设备ID(关联 mes3_maotai_equipment_ledger.id)*/
    @Schema(description = "所属设备ID")
    private String equipmentId;

    /**设备名称(冗余,后端根据 equipment_id 回填)*/
    @Excel(name = "设备名称", width = 20)
    @Schema(description = "设备名称")
    private String equipmentName;

    /**保养类型(字典 MES3_MAINT_TYPE)*/
    @Excel(name = "保养类型", width = 15, dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_TYPE' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_TYPE' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "保养类型")
    private String maintType;

    /**优先级(字典 MES3_MAINT_PRIORITY)*/
    @Excel(name = "优先级", width = 12, dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_PRIORITY' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_PRIORITY' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "优先级")
    private String priority;

    /**计划日期*/
    @Excel(name = "计划日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划日期")
    private Date planDate;

    /**完成日期*/
    @Excel(name = "完成日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "完成日期")
    private Date completeDate;

    /**执行人*/
    @Excel(name = "执行人", width = 15)
    @Schema(description = "执行人")
    private String executor;

    /**预估工时(如 2.5 小时)*/
    @Excel(name = "预估工时", width = 15)
    @Schema(description = "预估工时")
    private String estWorkhour;

    /**实际工时(如 3.5 小时)*/
    @Excel(name = "实际工时", width = 15)
    @Schema(description = "实际工时")
    private String actualWorkhour;

    /**费用(如 ¥2,380)*/
    @Excel(name = "费用", width = 15)
    @Schema(description = "费用")
    private String cost;

    /**保养内容*/
    @Excel(name = "保养内容", width = 40)
    @Schema(description = "保养内容")
    private String maintContent;

    /**保养内容清单(| 分隔文本,如 检查油位|更换滤芯|记录参数)*/
    @Excel(name = "保养内容清单", width = 50)
    @Schema(description = "保养内容清单(| 分隔文本)")
    private String checklist;

    /**所需备件(如 空气滤芯x1,润滑油x5L)*/
    @Excel(name = "所需备件", width = 30)
    @Schema(description = "所需备件")
    private String spareParts;

    /**状态(字典 MES3_MAINT_STATUS:pending待执行/progress进行中/completed已完成/overdue逾期)*/
    @Excel(name = "状态", width = 12, dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_STATUS' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_STATUS' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "状态")
    private String status;

    /**备注*/
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;
}
