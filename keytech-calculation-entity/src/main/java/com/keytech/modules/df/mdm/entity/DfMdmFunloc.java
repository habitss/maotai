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
 * @Description: DF主数据-功能位置
 * @Author: jeecg-boot
 * @Date:   2026-01-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_mdm_df_funloc")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF主数据-功能位置")
public class DfMdmFunloc implements Serializable {


	/**id*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private String id;

    /**租户ID*/
    @Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**功能位置编码*/
	@Excel(name = "功能位置编码", width = 15)
    @Schema(description = "功能位置编码")
    private String funlocCode;

	/**功能位置描述汇总*/
	@Excel(name = "功能位置描述汇总", width = 15)
    @Schema(description = "功能位置描述汇总")
    private String funlocDesc;

	/**部件编码*/
	@Excel(name = "部件编码", width = 15)
    @Schema(description = "部件编码")
    private String part;

	/**部件补充与描述*/
	@Excel(name = "部件补充与描述", width = 15)
    @Schema(description = "部件补充与描述")
    private String attr1;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String facCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deparCode;

	/**工序编码*/
	@Excel(name = "工序编码", width = 15)
    @Schema(description = "工序编码")
    private String processCode;

	/**产线编码*/
	@Excel(name = "产线编码", width = 15)
    @Schema(description = "产线编码")
    private String lineCode;

	/**主设备编码*/
	@Excel(name = "主设备编码", width = 15)
    @Schema(description = "主设备编码")
    private String equipCode;

	/**主设备序号*/
	@Excel(name = "主设备序号", width = 15)
    @Schema(description = "主设备序号")
    private String equipNum;

	/**附属设备*/
	@Excel(name = "附属设备", width = 15)
    @Schema(description = "附属设备")
    private String acceEquipCode;

	/**部件序号*/
	@Excel(name = "部件序号", width = 15)
    @Schema(description = "部件序号")
    private String partNum;

	/**部件补充*/
	@Excel(name = "部件补充", width = 15)
    @Schema(description = "部件补充")
    private String partRepl;

	/**部件类型*/
	@Excel(name = "部件类型", width = 15)
    @Schema(description = "部件类型")
    private String partType;

	/**部件英文缩写*/
	@Excel(name = "部件英文缩写", width = 15)
    @Schema(description = "部件英文缩写")
    private String partShortEn;

	/**功能位置编码汇总辅助列*/
	@Excel(name = "功能位置编码汇总辅助列", width = 15)
    @Schema(description = "功能位置编码汇总辅助列")
    private String funlocCodeAssi;

	/**结构标识*/
	@Excel(name = "结构标识", width = 15)
    @Schema(description = "结构标识")
    private String strId;

	/**大小/尺寸*/
	@Excel(name = "大小/尺寸", width = 15)
    @Schema(description = "大小/尺寸")
    private String sizeDime;

	/**制造商*/
	@Excel(name = "制造商", width = 15)
    @Schema(description = "制造商")
    private String manu;

	/**型号*/
	@Excel(name = "型号", width = 15)
    @Schema(description = "型号")
    private String model;

	/**制造商零件号*/
	@Excel(name = "制造商零件号", width = 15)
    @Schema(description = "制造商零件号")
    private String manuPartNum;

	/**系列号*/
	@Excel(name = "系列号", width = 15)
    @Schema(description = "系列号")
    private String seriNum;

	/**技术对象类型*/
	@Excel(name = "技术对象类型", width = 15)
    @Schema(description = "技术对象类型")
    private String techObjType;

	/**制造商国家*/
	@Excel(name = "制造商国家", width = 15)
    @Schema(description = "制造商国家")
    private String manuCountry;

	/**构建年*/
	@Excel(name = "构建年", width = 15)
    @Schema(description = "构建年")
    private String consYear;

	/**构建月*/
	@Excel(name = "构建月", width = 15)
    @Schema(description = "构建月")
    private String consMonth;

	/**维护工厂*/
	@Excel(name = "维护工厂", width = 15)
    @Schema(description = "维护工厂")
    private String mainFac;

	/**工作中心*/
	@Excel(name = "工作中心", width = 15)
    @Schema(description = "工作中心")
    private String workCenter;

	/**主工作中心*/
	@Excel(name = "主工作中心", width = 15)
    @Schema(description = "主工作中心")
    private String mainWorkCenter;

	/**ABC标识*/
	@Excel(name = "ABC标识", width = 15)
    @Schema(description = "ABC标识")
    private String abcMark;

	/**成本中心*/
	@Excel(name = "成本中心", width = 15)
    @Schema(description = "成本中心")
    private String costCenter;

	/**计划人员组*/
	@Excel(name = "计划人员组", width = 15)
    @Schema(description = "计划人员组")
    private String planGroup;

	/**开始日期*/
	@Excel(name = "开始日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "开始日期")
    private Date startDate;

	/**系统状态*/
	@Excel(name = "系统状态", width = 15)
    @Schema(description = "系统状态")
    private String sysStatus;

	/**工厂描述*/
	@Excel(name = "工厂描述", width = 15)
    @Schema(description = "工厂描述")
    private String factDesc;

	/**部门描述*/
	@Excel(name = "部门描述", width = 15)
    @Schema(description = "部门描述")
    private String deparDesc;

	/**工序描述*/
	@Excel(name = "工序描述", width = 15)
    @Schema(description = "工序描述")
    private String processDesc;

	/**主设备描述*/
	@Excel(name = "主设备描述", width = 15)
    @Schema(description = "主设备描述")
    private String equipDesc;

	/**附属设备描述*/
	@Excel(name = "附属设备描述", width = 15)
    @Schema(description = "附属设备描述")
    private String acceEquipDesc;

	/**部件描述*/
	@Excel(name = "部件描述", width = 15)
    @Schema(description = "部件描述")
    private String partDesc;

	/**弹性域2*/
	@Excel(name = "弹性域2", width = 15)
    @Schema(description = "弹性域2")
    private String attr2;

	/**弹性域3*/
	@Excel(name = "弹性域3", width = 15)
    @Schema(description = "弹性域3")
    private String attr3;

	/**业务状态*/
	@Excel(name = "业务状态", width = 15)
    @Schema(description = "业务状态")
    private String businessStatus;

	/**弹性域4*/
	@Excel(name = "弹性域4", width = 15)
    @Schema(description = "弹性域4")
    private String attr4;

	/**弹性域5*/
	@Excel(name = "弹性域5", width = 15)
    @Schema(description = "弹性域5")
    private String attr5;

	/**分发SAP标识*/
	@Excel(name = "分发SAP标识", width = 15)
    @Schema(description = "分发SAP标识")
    private String distributeSapFlag;

	/**弹性域6*/
	@Excel(name = "弹性域6", width = 15)
    @Schema(description = "弹性域6")
    private String attr6;

	/**弹性域7*/
	@Excel(name = "弹性域7", width = 15)
    @Schema(description = "弹性域7")
    private String attr7;

	/**弹性域8*/
	@Excel(name = "弹性域8", width = 15)
    @Schema(description = "弹性域8")
    private String attr8;

	/**弹性域9*/
	@Excel(name = "弹性域9", width = 15)
    @Schema(description = "弹性域9")
    private String attr9;

	/**弹性域10*/
	@Excel(name = "弹性域10", width = 15)
    @Schema(description = "弹性域10")
    private String attr10;

	/**产线描述*/
	@Excel(name = "产线描述", width = 15)
    @Schema(description = "产线描述")
    private String lineDesc;

	/**主设备序号描述*/
	@Excel(name = "主设备序号描述", width = 15)
    @Schema(description = "主设备序号描述")
    private String equipNumDesc;

	/**部件大类*/
	@Excel(name = "部件大类", width = 15)
    @Schema(description = "部件大类")
    private String partBigType;

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
