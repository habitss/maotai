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
 * 设备台账
 * @author maotai
 * @since 2026-06-15
 */
@Data
@TableName("mes3_maotai_equipment_ledger")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备台账")
public class EquipmentLedger extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**功能位置编码(用户手动输入,租户内唯一)*/
    @Excel(name = "功能位置编码", width = 20)
    @Schema(description = "功能位置编码")
    private String funlocCode;

    /** 罐体配置关联键（tank_config.id，稳定不变；funloc_code/equip_name 为台账可变业务字段） */
    @Schema(description = "罐体配置关联键")
    private String tankConfigId;

    /**设备名称*/
    @Excel(name = "设备名称", width = 20)
    @Schema(description = "设备名称")
    private String equipName;

    /**所属车间(字典 MES3_WORKSHOP)*/
    @Excel(name = "所属车间", width = 20, dictTable = "mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "所属车间")
    private String workshopCode;

    /**设备型号*/
    @Excel(name = "设备型号", width = 15)
    @Schema(description = "设备型号")
    private String equipModel;

    /**制造商*/
    @Excel(name = "制造商", width = 20)
    @Schema(description = "制造商")
    private String manufacturer;

    /**安装日期*/
    @Excel(name = "安装日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "安装日期")
    private Date installDate;

    /**投运日期*/
    @Excel(name = "投运日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "投运日期")
    private Date commissionDate;

    /**额定功率(如 450 kW)*/
    @Excel(name = "额定功率", width = 15)
    @Schema(description = "额定功率")
    private String ratedPower;

    /**安装位置*/
    @Excel(name = "安装位置", width = 20)
    @Schema(description = "安装位置")
    private String installLocation;

    @Schema(description = "罐体容量(m³,驾驶舱/预览容量显示来源;非罐设备为空)")
    private Double tankCapacity;

    /**保养周期(天):30/60/90/180/365(字典 MES3_MAINT_CYCLE)*/
    @Excel(name = "保养周期(天)", width = 15, dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_CYCLE' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_MAINT_CYCLE' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "保养周期(天)")
    private Integer maintCycle;

    /**设计寿命(如 15年)*/
    @Excel(name = "设计寿命", width = 15)
    @Schema(description = "设计寿命")
    private String designLife;

    /**运行状态(字典 MES3_EQUIP_STATUS:1在线 2停机 3维修 4报警)*/
    @Excel(name = "运行状态", width = 12, dictTable = "mes3_mdm_dict where group_code='MES3_EQUIP_STATUS' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Dict(dictTable = "mes3_mdm_dict where group_code='MES3_EQUIP_STATUS' and del_flag=0", dicText = "item_val", dicCode = "item_key")
    @Schema(description = "运行状态(1在线 2停机 3维修 4报警)")
    private Integer runStatus;

    /**联系电话*/
    @Excel(name = "联系电话", width = 15)
    @Schema(description = "联系电话")
    private String contactPhone;

    /**最近保养日期*/
    @Excel(name = "最近保养", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "最近保养日期")
    private Date lastMaintDate;

    /**备注*/
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;
}
