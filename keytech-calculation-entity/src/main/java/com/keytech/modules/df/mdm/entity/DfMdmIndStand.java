package com.keytech.modules.df.mdm.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: DF主数据-指标标准
 * @Author: jeecg-boot
 * @Date:   2026-01-26
 * @Version: V1.0
 */
@Data
@TableName("mes3_mdm_df_ind_stand")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF主数据-指标标准")
public class DfMdmIndStand implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Integer id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**流程架构*/
	@Excel(name = "流程架构", width = 15)
    @Schema(description = "流程架构")
    private String bizCatalogPath;

	/**指标框架（T0）*/
	@Excel(name = "指标框架（T0）", width = 15)
    @Schema(description = "指标框架（T0）")
    private String indFraT0;

	/**指标框架（T1）*/
	@Excel(name = "指标框架（T1）", width = 15)
    @Schema(description = "指标框架（T1）")
    private String indFraT1;

	/**指标框架（T2）*/
	@Excel(name = "指标框架（T2）", width = 15)
    @Schema(description = "指标框架（T2）")
    private String indFraT2;

	/**指标框架（T3）*/
	@Excel(name = "指标框架（T3）", width = 15)
    @Schema(description = "指标框架（T3）")
    private String indFraT3;

	/**指标框架（T4）*/
	@Excel(name = "指标框架（T4）", width = 15)
    @Schema(description = "指标框架（T4）")
    private String indFraT4;

	/**指标框架（T5）【酿造/包装/动力-运营管理指标填写】*/
	@Excel(name = "指标框架（T5）【酿造/包装/动力-运营管理指标填写】", width = 15)
    @Schema(description = "指标框架（T5）【酿造/包装/动力-运营管理指标填写】")
    private String indFraT5;

	/**管理对象【酿造/包装/动力-运营管理指标填写】*/
	@Excel(name = "管理对象【酿造/包装/动力-运营管理指标填写】", width = 15)
    @Schema(description = "管理对象【酿造/包装/动力-运营管理指标填写】")
    private String manaObj;

	/**活动【酿造/包装/动力-运营管理指标填写】*/
	@Excel(name = "活动【酿造/包装/动力-运营管理指标填写】", width = 15)
    @Schema(description = "活动【酿造/包装/动力-运营管理指标填写】")
    private String activity;

	/**一级任务【酿造/包装/动力-生产监测指标填写】*/
	@Excel(name = "一级任务【酿造/包装/动力-生产监测指标填写】", width = 15)
    @Schema(description = "一级任务【酿造/包装/动力-生产监测指标填写】")
    private String firTask;

	/**二级任务【酿造/包装/动力-生产监测指标填写】*/
	@Excel(name = "二级任务【酿造/包装/动力-生产监测指标填写】", width = 15)
    @Schema(description = "二级任务【酿造/包装/动力-生产监测指标填写】")
    private String secTask;

	/**指标编码*/
	@Excel(name = "指标编码", width = 15)
    @Schema(description = "指标编码")
    private String indStandCode;

	/**对应ORS指标代码*/
	@Excel(name = "对应ORS指标代码", width = 15)
    @Schema(description = "对应ORS指标代码")
    private String corOrsCode;

	/**指标名称(原始名称)*/
	@Excel(name = "指标名称(原始名称)", width = 15)
    @Schema(description = "指标名称(原始名称)")
    private String indNameOld;

	/**指标名称（英文名)【质量-管理类指标填写】*/
	@Excel(name = "指标名称（英文名)【质量-管理类指标填写】", width = 15)
    @Schema(description = "指标名称（英文名)【质量-管理类指标填写】")
    private String indNameEn;

	/**指标名称*/
	@Excel(name = "指标名称", width = 15)
    @Schema(description = "指标名称")
    private String indName;

	/**指标定义*/
	@Excel(name = "指标定义", width = 15)
    @Schema(description = "指标定义")
    private String indDef;

	/**监测对象分类【酿造/动力/质量(生产)监测类指标填写】*/
	@Excel(name = "监测对象分类【酿造/动力/质量(生产)监测类指标填写】", width = 15)
    @Schema(description = "监测对象分类【酿造/动力/质量(生产)监测类指标填写】")
    private String monObjType;

	/**监测对象【酿造/动力/质量(生产)监测类指标填写】*/
	@Excel(name = "监测对象【酿造/动力/质量(生产)监测类指标填写】", width = 15)
    @Schema(description = "监测对象【酿造/动力/质量(生产)监测类指标填写】")
    private String monObj;

	/**检查项【包装-生产监测指标填写】*/
	@Excel(name = "检查项【包装-生产监测指标填写】", width = 15)
    @Schema(description = "检查项【包装-生产监测指标填写】")
    private String inspItem;

	/**检查对象分类【包装-生产监测指标填写】*/
	@Excel(name = "检查对象分类【包装-生产监测指标填写】", width = 15)
    @Schema(description = "检查对象分类【包装-生产监测指标填写】")
    private String inspObjType;

	/**检查对象【包装-生产监测指标填写】*/
	@Excel(name = "检查对象【包装-生产监测指标填写】", width = 15)
    @Schema(description = "检查对象【包装-生产监测指标填写】")
    private String inspObj;

	/**工序-所属部门【质量指标填写】*/
	@Excel(name = "工序-所属部门【质量指标填写】", width = 15)
    @Schema(description = "工序-所属部门【质量指标填写】")
    private String opStepType;

	/**工序-工艺一级分类【质量指标填写】*/
	@Excel(name = "工序-工艺一级分类【质量指标填写】", width = 15)
    @Schema(description = "工序-工艺一级分类【质量指标填写】")
    private String firProcName;

	/**工序-工艺二级分类【质量指标填写】*/
	@Excel(name = "工序-工艺二级分类【质量指标填写】", width = 15)
    @Schema(description = "工序-工艺二级分类【质量指标填写】")
    private String secProcName;

	/**工序-工序步骤1【质量指标填写】*/
	@Excel(name = "工序-工序步骤1【质量指标填写】", width = 15)
    @Schema(description = "工序-工序步骤1【质量指标填写】")
    private String op1StepName;

	/**度量对象*/
	@Excel(name = "度量对象", width = 15)
    @Schema(description = "度量对象")
    private String meaObj;

	/**计量单位*/
	@Excel(name = "计量单位", width = 15)
    @Schema(description = "计量单位")
    private String unit;

	/**对应SAP功能位置-工厂*/
	@Excel(name = "对应SAP功能位置-工厂", width = 15)
    @Schema(description = "对应SAP功能位置-工厂")
    private String funlocFac;

	/**对应SAP功能位置-部门*/
	@Excel(name = "对应SAP功能位置-部门", width = 15)
    @Schema(description = "对应SAP功能位置-部门")
    private String funlocDeparDesc;

	/**对应SAP功能位置-工序*/
	@Excel(name = "对应SAP功能位置-工序", width = 15)
    @Schema(description = "对应SAP功能位置-工序")
    private String funlocProcessDesc;

	/**对应SAP功能位置-主设备*/
	@Excel(name = "对应SAP功能位置-主设备", width = 15)
    @Schema(description = "对应SAP功能位置-主设备")
    private String funlocEquipDesc;

	/**对应SAP功能位置-附属设备*/
	@Excel(name = "对应SAP功能位置-附属设备", width = 15)
    @Schema(description = "对应SAP功能位置-附属设备")
    private String funlocAcceEquipDesc;

	/**对应SAP功能位置-部件*/
	@Excel(name = "对应SAP功能位置-部件", width = 15)
    @Schema(description = "对应SAP功能位置-部件")
    private String funlocPart;

	/**取样点*/
	@Excel(name = "取样点", width = 15)
    @Schema(description = "取样点")
    private String samPoint;

	/**监测方式*/
	@Excel(name = "监测方式", width = 15)
    @Schema(description = "监测方式")
    private String monMethod;

	/**监测设备*/
	@Excel(name = "监测设备", width = 15)
    @Schema(description = "监测设备")
    private String monEquip;

	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @Schema(description = "数据来源")
    private String dataSource;

	/**指标口径*/
	@Excel(name = "指标口径", width = 15)
    @Schema(description = "指标口径")
    private String indCal;

	/**指标设置目的*/
	@Excel(name = "指标设置目的", width = 15)
    @Schema(description = "指标设置目的")
    private String indSetPurp;

	/**计算方式*/
	@Excel(name = "计算方式", width = 15)
    @Schema(description = "计算方式")
    private String calMeth;

	/**计算公式*/
	@Excel(name = "计算公式", width = 15)
    @Schema(description = "计算公式")
    private String calFormula;

	/**指标统计周期*/
	@Excel(name = "指标统计周期", width = 15)
    @Schema(description = "指标统计周期")
    private String indStaCycle;

	/**指标统计维度*/
	@Excel(name = "指标统计维度", width = 15)
    @Schema(description = "指标统计维度")
    private String indStaDim;

	/**刷新频率*/
	@Excel(name = "刷新频率", width = 15)
    @Schema(description = "刷新频率")
    private String refFreq;

	/**指标归口管理部门*/
	@Excel(name = "指标归口管理部门", width = 15)
    @Schema(description = "指标归口管理部门")
    private String indCenManDept;

	/**指标责任人*/
	@Excel(name = "指标责任人", width = 15)
    @Schema(description = "指标责任人")
    private String indOwner;

	/**指标分类【动力-运营管理指标填写】*/
	@Excel(name = "指标分类【动力-运营管理指标填写】", width = 15)
    @Schema(description = "指标分类【动力-运营管理指标填写】")
    private String piType;

	/**原燃辅料类型【动力-运营管理指标填写】*/
	@Excel(name = "原燃辅料类型【动力-运营管理指标填写】", width = 15)
    @Schema(description = "原燃辅料类型【动力-运营管理指标填写】")
    private String rawcamType;

	/**业务类型【动力-运营管理指标填写】*/
	@Excel(name = "业务类型【动力-运营管理指标填写】", width = 15)
    @Schema(description = "业务类型【动力-运营管理指标填写】")
    private String busiType;

	/**采购来源【动力-运营管理指标填写】*/
	@Excel(name = "采购来源【动力-运营管理指标填写】", width = 15)
    @Schema(description = "采购来源【动力-运营管理指标填写】")
    private String procurementSource;

	/**能源介质类型【动力-能源消耗指标填写】*/
	@Excel(name = "能源介质类型【动力-能源消耗指标填写】", width = 15)
    @Schema(description = "能源介质类型【动力-能源消耗指标填写】")
    private String energyType;

	/**能源介质子类【动力-能源消耗指标填写】*/
	@Excel(name = "能源介质子类【动力-能源消耗指标填写】", width = 15)
    @Schema(description = "能源介质子类【动力-能源消耗指标填写】")
    private String energySubtype;

	/**指标别名*/
	@Excel(name = "指标别名", width = 15)
    @Schema(description = "指标别名")
    private String indNameAlias;

	/**指标应用场景*/
	@Excel(name = "指标应用场景", width = 15)
    @Schema(description = "指标应用场景")
    private String applyScenario;

	/**取数报表*/
	@Excel(name = "取数报表", width = 15)
    @Schema(description = "取数报表")
    private String accReport;

	/**取值字段*/
	@Excel(name = "取值字段", width = 15)
    @Schema(description = "取值字段")
    private String reportField;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remarks;

	/**同步状态*/
	@Excel(name = "同步状态", width = 15)
    @Schema(description = "同步状态")
    private String synchronousStatus;

	/**计量单位_中文*/
	@Excel(name = "计量单位_中文", width = 15)
    @Schema(description = "计量单位_中文")
    private String unitCn;

	/**工序-工序步骤2*/
	@Excel(name = "工序-工序步骤2", width = 15)
    @Schema(description = "工序-工序步骤2")
    private String op2StepName;

	/**系统ID*/
	@Excel(name = "系统ID", width = 15)
    @Schema(description = "系统ID")
    private String sysUniqueId;

	/**数据来源系统ID*/
	@Excel(name = "数据来源系统ID", width = 15)
    @Schema(description = "数据来源系统ID")
    private String dataSourceSysId;

	/**系统编码*/
	@Excel(name = "系统编码", width = 15)
    @Schema(description = "系统编码")
    private String sysUniqueCode;

	/**有效标识*/
	@Excel(name = "有效标识", width = 15)
    @Schema(description = "有效标识")
    private String enableFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**校验错误消息*/
	@Excel(name = "校验错误消息", width = 15)
    @Schema(description = "校验错误消息")
    private String checkErrMsg;

	/**数据版本号*/
	@Excel(name = "数据版本号", width = 15)
    @Schema(description = "数据版本号")
    private String rtMdmDataVersion;

    private static final long serialVersionUID = 1L;
}
