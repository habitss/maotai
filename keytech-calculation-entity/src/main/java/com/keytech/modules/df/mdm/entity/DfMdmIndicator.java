package com.keytech.modules.df.mdm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * MDM-指标主数据表
 * @author yepeng
 * @since 2025.12.08
 * */
@Data
@TableName("mes3_mdm_df_indicator")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="MDM-指标主数据表")
public class DfMdmIndicator implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
    /**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**系统唯一ID sys_unique_id*/
	@Excel(name = "系统唯一ID", width = 15)
    @Schema(description = "系统唯一ID sys_unique_id")
    private String sysUniqueId;
	/**来源系统ID data_source_sys_id*/
	@Excel(name = "来源系统ID", width = 15)
    @Schema(description = "来源系统ID data_source_sys_id")
    private String dataSourceSysId;
	/**唯一编码 sys_unique_code*/
	@Excel(name = "唯一编码", width = 15)
    @Schema(description = "唯一编码 sys_unique_code")
    private String sysUniqueCode;
	/**功能位置编码 FUNLOC_CODE*/
	@Excel(name = "功能位置编码 FUNLOC_CODE", width = 15)
    @Schema(description = "功能位置编码 FUNLOC_CODE")
    private String funlocCode;
	/**功能位置描述 FUNLOC_DESC*/
	@Excel(name = "功能位置描述 FUNLOC_DESC", width = 15)
    @Schema(description = "功能位置描述 FUNLOC_DESC")
    private String funlocDesc;
	/**业务区域 IND_BUSI_AREA*/
	@Excel(name = "业务区域 IND_BUSI_AREA", width = 15)
    @Schema(description = "业务区域 IND_BUSI_AREA")
    private String indBusiArea;
	/**中心管理部门 IND_CEN_MAN_DEPT*/
	@Excel(name = "中心管理部门 IND_CEN_MAN_DEPT", width = 15)
    @Schema(description = "中心管理部门 IND_CEN_MAN_DEPT")
    private String indCenManDept;
	/**指标名称 IND_NAME*/
	@Excel(name = "指标名称 IND_NAME", width = 15)
    @Schema(description = "指标名称 IND_NAME")
    private String indName;
	/**指标分类 T0 IND_FRA_T0*/
	@Excel(name = "指标分类 T0 IND_FRA_T0", width = 15)
    @Schema(description = "指标分类 T0 IND_FRA_T0")
    private String indFraT0;
	/**指标分类 T1 IND_FRA_T1*/
	@Excel(name = "指标分类 T1 IND_FRA_T1", width = 15)
    @Schema(description = "指标分类 T1 IND_FRA_T1")
    private String indFraT1;
	/**指标分类 T2 IND_FRA_T2*/
	@Excel(name = "指标分类 T2 IND_FRA_T2", width = 15)
    @Schema(description = "指标分类 T2 IND_FRA_T2")
    private String indFraT2;
	/**指标分类 T3 IND_FRA_T3*/
	@Excel(name = "指标分类 T3 IND_FRA_T3", width = 15)
    @Schema(description = "指标分类 T3 IND_FRA_T3")
    private String indFraT3;
	/**指标分类 T4 IND_FRA_T4*/
	@Excel(name = "指标分类 T4 IND_FRA_T4", width = 15)
    @Schema(description = "指标分类 T4 IND_FRA_T4")
    private String indFraT4;
	/**指标分类 T5 IND_FRA_T5*/
	@Excel(name = "指标分类 T5 IND_FRA_T5", width = 15)
    @Schema(description = "指标分类 T5 IND_FRA_T5")
    private String indFraT5;
	/**指标标准编码 IND_STAND_CODE*/
	@Excel(name = "指标标准编码 IND_STAND_CODE", width = 15)
    @Schema(description = "指标标准编码 IND_STAND_CODE")
    private String indStandCode;
	/**指标标准描述 IND_STAND_DESC*/
	@Excel(name = "指标标准描述 IND_STAND_DESC", width = 15)
    @Schema(description = "指标标准描述 IND_STAND_DESC")
    private String indStandDesc;
	/**单位 UNIT*/
	@Excel(name = "单位 UNIT", width = 15)
    @Schema(description = "单位 UNIT")
    private String unit;
	/**单位中文 UNIT_CN*/
	@Excel(name = "单位中文 UNIT_CN", width = 15)
    @Schema(description = "单位中文 UNIT_CN")
    private String unitCn;
	/**工厂编码 FAC_CODE*/
	@Excel(name = "工厂编码 FAC_CODE", width = 15)
    @Schema(description = "工厂编码 FAC_CODE")
    private String facCode;
	/**工厂名称 FAC_DESC*/
	@Excel(name = "工厂名称 FAC_DESC", width = 15)
    @Schema(description = "工厂名称 FAC_DESC")
    private String facDesc;
	/**部门编码 DEPAR_CODE*/
	@Excel(name = "部门编码 DEPAR_CODE", width = 15)
    @Schema(description = "部门编码 DEPAR_CODE")
    private String deparCode;
	/**部门名称 DEPAR_DESC*/
	@Excel(name = "部门名称 DEPAR_DESC", width = 15)
    @Schema(description = "部门名称 DEPAR_DESC")
    private String deparDesc;
	/**工序编码 PROCESS_CODE*/
	@Excel(name = "工序编码 PROCESS_CODE", width = 15)
    @Schema(description = "工序编码 PROCESS_CODE")
    private String processCode;
	/**工序描述 PROCESS_DESC*/
	@Excel(name = "工序描述 PROCESS_DESC", width = 15)
    @Schema(description = "工序描述 PROCESS_DESC")
    private String processDesc;
	/**产线编码 LINE_CODE*/
	@Excel(name = "产线编码 LINE_CODE", width = 15)
    @Schema(description = "产线编码 LINE_CODE")
    private String lineCode;
	/**产线描述 LINE_DESC*/
	@Excel(name = "产线描述 LINE_DESC", width = 15)
    @Schema(description = "产线描述 LINE_DESC")
    private String lineDesc;
	/**设备编码 EQUIP_CODE*/
	@Excel(name = "设备编码 EQUIP_CODE", width = 15)
    @Schema(description = "设备编码 EQUIP_CODE")
    private String equipCode;
	/**设备描述 EQUIP_DESC*/
	@Excel(name = "设备描述 EQUIP_DESC", width = 15)
    @Schema(description = "设备描述 EQUIP_DESC")
    private String equipDesc;
	/**设备数量 EQUIP_NUM*/
	@Excel(name = "设备数量 EQUIP_NUM", width = 15)
    @Schema(description = "设备数量 EQUIP_NUM")
    private String equipNum;
	/**设备数量描述 EQUIP_NUM_DESC*/
	@Excel(name = "设备数量描述 EQUIP_NUM_DESC", width = 15)
    @Schema(description = "设备数量描述 EQUIP_NUM_DESC")
    private String equipNumDesc;
	/**辅件设备编码 ACCE_EQUIP_CODE*/
	@Excel(name = "辅件设备编码 ACCE_EQUIP_CODE", width = 15)
    @Schema(description = "辅件设备编码 ACCE_EQUIP_CODE")
    private String acceEquipCode;
	/**辅件设备描述 ACCE_EQUIP_DESC*/
	@Excel(name = "辅件设备描述 ACCE_EQUIP_DESC", width = 15)
    @Schema(description = "辅件设备描述 ACCE_EQUIP_DESC")
    private String acceEquipDesc;
	/**部件编码 PART_NUM*/
	@Excel(name = "部件编码 PART_NUM", width = 15)
    @Schema(description = "部件编码 PART_NUM")
    private String partNum;
	/**部件描述 PART_NUM_DESC*/
	@Excel(name = "部件描述 PART_NUM_DESC", width = 15)
    @Schema(description = "部件描述 PART_NUM_DESC")
    private String partNumDesc;
	/**零件 PART*/
	@Excel(name = "零件 PART", width = 15)
    @Schema(description = "零件 PART")
    private String part;
	/**零件描述 PART_DESC*/
	@Excel(name = "零件描述 PART_DESC", width = 15)
    @Schema(description = "零件描述 PART_DESC")
    private String partDesc;
	/**数据来源 DATA_SOURCE*/
	@Excel(name = "数据来源 DATA_SOURCE", width = 15)
    @Schema(description = "数据来源 DATA_SOURCE")
    private String dataSource;
	/**计算方法 CAL_METH*/
	@Excel(name = "计算方法 CAL_METH", width = 15)
    @Schema(description = "计算方法 CAL_METH")
    private String calMeth;
	/**计算公式 CAL_FORMULA*/
	@Excel(name = "计算公式 CAL_FORMULA", width = 15)
    @Schema(description = "计算公式 CAL_FORMULA")
    private String calFormula;
	/**备注 REMARKS*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注 REMARKS")
    private String remarks;
	/**备注1 REMARKS1*/
	@Excel(name = "备注1", width = 15)
    @Schema(description = "备注1 REMARKS1")
    private String remarks1;
	/**备注2 REMARKS2*/
	@Excel(name = "备注2", width = 15)
    @Schema(description = "备注2 REMARKS2")
    private String remarks2;
	/**备注3 REMARKS3*/
	@Excel(name = "备注3", width = 15)
    @Schema(description = "备注3 REMARKS3")
    private String remarks3;
	/**PI类型 PI_TYPE*/
	@Excel(name = "PI类型 PI_TYPE", width = 15)
    @Schema(description = "PI类型 PI_TYPE")
    private String piType;
	/**原始CAM类型 RAWCAM_TYPE*/
	@Excel(name = "原始CAM类型 RAWCAM_TYPE", width = 15)
    @Schema(description = "原始CAM类型 RAWCAM_TYPE")
    private String rawcamType;
	/**业务类型 BUSI_TYPE*/
	@Excel(name = "业务类型 BUSI_TYPE", width = 15)
    @Schema(description = "业务类型 BUSI_TYPE")
    private String busiType;
	/**采购来源 PROCUREMENT_SOURCE*/
	@Excel(name = "采购来源 PROCUREMENT_SOURCE", width = 15)
    @Schema(description = "采购来源 PROCUREMENT_SOURCE")
    private String procurementSource;
	/**能源类型 ENERGY_TYPE*/
	@Excel(name = "能源类型 ENERGY_TYPE", width = 15)
    @Schema(description = "能源类型 ENERGY_TYPE")
    private String energyType;
	/**能源子类型 ENERGY_SUBTYPE*/
	@Excel(name = "能源子类型 ENERGY_SUBTYPE", width = 15)
    @Schema(description = "能源子类型 ENERGY_SUBTYPE")
    private String energySubtype;
	/**同步状态 SYNCHRONOUS_STATUS*/
	@Excel(name = "同步状态 SYNCHRONOUS_STATUS", width = 15)
    @Schema(description = "同步状态 SYNCHRONOUS_STATUS")
    private String synchronousStatus;
	/**指标实例编码 IND_INST_CODE*/
	@Excel(name = "指标实例编码 IND_INST_CODE", width = 15)
    @Schema(description = "指标实例编码 IND_INST_CODE")
    private String indInstCode;
	/**实例使用编码 INST_USE_CODE*/
	@Excel(name = "实例使用编码 INST_USE_CODE", width = 15)
    @Schema(description = "实例使用编码 INST_USE_CODE")
    private String instUseCode;
	/**业务状态 BUSINESS_STATUS*/
	@Excel(name = "业务状态 BUSINESS_STATUS", width = 15)
    @Schema(description = "业务状态 BUSINESS_STATUS")
    private String businessStatus;
	/**实例业务状态 INST_BUSINESS_STATUS*/
	@Excel(name = "实例业务状态 INST_BUSINESS_STATUS", width = 15)
    @Schema(description = "实例业务状态 INST_BUSINESS_STATUS")
    private String instBusinessStatus;
	/**数据来源 rt_data_origin*/
	@Excel(name = "数据来源", width = 15)
    @Schema(description = "数据来源")
    private String rtDataOrigin;
	/**申请号 rt_apply_no*/
	@Excel(name = "申请号", width = 15)
    @Schema(description = "申请号")
    private String rtApplyNo;
	/**工单号 rt_ticket_no*/
	@Excel(name = "工单号", width = 15)
    @Schema(description = "工单号")
    private String rtTicketNo;
	/**锁产线状态 LOCK_LINE_STATUS*/
	@Excel(name = "锁产线状态", width = 15)
    @Schema(description = "锁产线状态 LOCK_LINE_STATUS")
    private String lockLineStatus;
	/**是否启用 enable_flag*/
	@Excel(name = "是否启用", width = 15)
    @Schema(description = "是否启用 enable_flag")
    private String enableFlag;
	/**创建人 create_by*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间 create_time*/
    @Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人 update_by*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间 update_time*/
    @Excel(name = "更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**校验错误信息 check_err_msg*/
	@Excel(name = "校验错误信息", width = 15)
    @Schema(description = "校验错误信息 ")
    private String checkErrMsg;
	/**主数据版本 rt_mdm_data_version*/
	@Excel(name = "主数据版本", width = 15)
    @Schema(description = "主数据版本")
    private String rtMdmDataVersion;
    /**同步时间 sync_time*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    /**工厂 factory_code*/
    @TableField(exist = false)
    private String factoryCode;
}
