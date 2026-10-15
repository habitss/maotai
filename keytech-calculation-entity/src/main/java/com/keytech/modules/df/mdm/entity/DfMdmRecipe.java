package com.keytech.modules.df.mdm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * MDM-配方主数据表
 * @author yepeng
 * @since 2025.12.08
 * */
@Data
@TableName("mes3_mdm_df_recipe")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="MDM-配方主数据表")
public class DfMdmRecipe implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
    /**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
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
	/**工序编码 PROCESS_COD*/
	@Excel(name = "工序编码 PROCESS_COD", width = 15)
    @Schema(description = "工序编码 PROCESS_COD")
    private String processCod;
	/**工序描述 PROCESS_DESC*/
	@Excel(name = "工序描述 PROCESS_DESC", width = 15)
    @Schema(description = "工序描述 PROCESS_DESC")
    private String processDesc;
    /**产线编码 LINE_CODE*/
    @Excel(name = "产线编码 LINE_CODE", width = 15)
    @Schema(description = "产线编码 LINE_CODE")
    private String lineCode;
	/**产线子编码 LINE_S_CODE*/
	@Excel(name = "产线子编码 LINE_S_CODE", width = 15)
    @Schema(description = "产线子编码 LINE_S_CODE")
    private String lineSCode;
	/**配方号 RECIPE_NUM*/
	@Excel(name = "配方号 RECIPE_NUM", width = 15)
    @Schema(description = "配方号 RECIPE_NUM")
    private String recipeNum;
	/**配方类型代码 RECIPE_TYPE*/
	@Excel(name = "配方类型代码 RECIPE_TYPE", width = 15)
    @Schema(description = "配方类型代码 RECIPE_TYPE")
    private String recipeType;
	/**配方类型描述 RECIPE_TYPE_DESC*/
	@Excel(name = "配方类型描述 RECIPE_TYPE_DESC", width = 15)
    @Schema(description = "配方类型描述 RECIPE_TYPE_DESC")
    private String recipeTypeDesc;
	/**工厂配方名称 FAC_RECIPE_NAME*/
	@Excel(name = "工厂配方名称 FAC_RECIPE_NAME", width = 15)
    @Schema(description = "工厂配方名称 FAC_RECIPE_NAME")
    private String facRecipeName;
	/**工厂配方编码 FAC_RECIPE_CODE*/
	@Excel(name = "工厂配方编码 FAC_RECIPE_CODE", width = 15)
    @Schema(description = "工厂配方编码 FAC_RECIPE_CODE")
    private String facRecipeCode;
	/**区域配方编码 ZONE_RECIPE_CODE*/
	@Excel(name = "区域配方编码 ZONE_RECIPE_CODE", width = 15)
    @Schema(description = "区域配方编码 ZONE_RECIPE_CODE")
    private String zoneRecipeCode;
	/**区域配方中文名 ZONE_RECIPE_CN_NAME*/
	@Excel(name = "区域配方中文名 ZONE_RECIPE_CN_NAME", width = 15)
    @Schema(description = "区域配方中文名 ZONE_RECIPE_CN_NAME")
    private String zoneRecipeCnName;
	/**子品牌原始编码 SUB_BRAND_OG_CODE*/
	@Excel(name = "子品牌原始编码 SUB_BRAND_OG_CODE", width = 15)
    @Schema(description = "子品牌原始编码 SUB_BRAND_OG_CODE")
    private String subBrandOgCode;
	/**子品牌中文名称 SUB_BRAND_OG_CN_NAME*/
	@Excel(name = "子品牌中文名称 SUB_BRAND_OG_CN_NAME", width = 15)
    @Schema(description = "子品牌中文名称 SUB_BRAND_OG_CN_NAME")
    private String subBrandOgCnName;
	/**标准体积 STAND_VOL*/
	@Excel(name = "标准体积 STAND_VOL", width = 15)
    @Schema(description = "标准体积 STAND_VOL")
    private String standVol;
	/**连续生产标准周期时间*/
	@Excel(name = "连续生产标准周期时间", width = 15)
    @Schema(description = "连续生产标准周期时间")
    private String contProdStandCycleTime;
	/**首道工序时间*/
	@Excel(name = "首道工序时间", width = 15)
    @Schema(description = "首道工序时间")
    private String firstProdProcessTime;
	/**版本号 VERSION_NUMBER*/
	@Excel(name = "版本号 VERSION_NUMBER", width = 15)
    @Schema(description = "版本号 VERSION_NUMBER")
    private String versionNumber;
	/**设备代码 EQUIP_CODE*/
	@Excel(name = "设备代码 EQUIP_CODE", width = 15)
    @Schema(description = "设备代码 EQUIP_CODE")
    private String equipCode;
	/**设备描述 EQUIP_DESC*/
	@Excel(name = "设备描述 EQUIP_DESC", width = 15)
    @Schema(description = "设备描述 EQUIP_DESC")
    private String equipDesc;
	/**生效时间 EFFE_DATE*/
	@Excel(name = "生效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效时间 EFFE_DATE")
    private LocalDateTime effeDate;
	/**失效时间 EXPI_DATE*/
	@Excel(name = "失效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "失效时间 EXPI_DATE")
    private LocalDateTime expiDate;
	/**业务状态 BUSINE_STATUS*/
	@Excel(name = "业务状态 BUSINE_STATUS", width = 15)
    @Schema(description = "业务状态 BUSINE_STATUS")
    private String busineStatus;
	/**扩展字段 ATTR1*/
	@Excel(name = "扩展字段1", width = 15)
    @Schema(description = "扩展字段1")
    private String attr1;
	/**扩展字段 ATTR2*/
	@Excel(name = "扩展字段2", width = 15)
    @Schema(description = "扩展字段2")
    private String attr2;
	/**系统唯一ID sys_unique_id*/
	@Excel(name = "系统唯一ID", width = 15)
    @Schema(description = "系统唯一ID sys_unique_id")
    private String sysUniqueId;
	/**唯一编码 sys_unique_code*/
	@Excel(name = "唯一编码", width = 15)
    @Schema(description = "唯一编码 sys_unique_code")
    private String sysUniqueCode;
	/**来源系统ID*/
	@Excel(name = "来源系统ID", width = 15)
    @Schema(description = "来源系统ID")
    private String dataSourceSysId;
	/**主数据版本*/
	@Excel(name = "主数据版本", width = 15)
    @Schema(description = "主数据版本")
    private String rtMdmDataVersion;
	/**来源 ORIGIN*/
	@Excel(name = "来源", width = 15)
    @Schema(description = "来源 ORIGIN")
    private String rtDataOrigin;
	/**申请单号*/
	@Excel(name = "申请单号", width = 15)
    @Schema(description = "申请单号")
    private String rtApplyNo;
	/**工单号*/
	@Excel(name = "工单号", width = 15)
    @Schema(description = "工单号")
    private String rtTicketNo;
	/**校验错误信息*/
	@Excel(name = "校验错误信息", width = 15)
    @Schema(description = "校验错误信息")
    private String checkErrMsg;
	/**是否启用*/
	@Excel(name = "是否启用", width = 15)
    @Schema(description = "是否启用")
    private String enableFlag;
	/**createBy*/
    @Schema(description = "创建人")
    private String createBy;
	/**createTime*/
    @Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**updateBy*/
    @Schema(description = "更新人")
    private String updateBy;
	/**updateTime*/
    @Excel(name = "更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    /**同步时间 sync_time*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;
}
