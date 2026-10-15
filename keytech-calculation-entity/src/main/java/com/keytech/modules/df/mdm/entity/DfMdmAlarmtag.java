package com.keytech.modules.df.mdm.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * MDM-报警点位主数据表
 * @author yepeng
 * @since 2025.12.24
 * */
@Data
@TableName("mes3_mdm_df_alarmtag")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="MDM-报警点位主数据表")
public class DfMdmAlarmtag implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;
	/**MDM系统唯一ID*/
	@Excel(name = "系统唯一ID", width = 15)
    @Schema(description = "系统唯一ID")
    private String sysUniqueId;
	/**MDM系统唯一编码*/
	@Excel(name = "系统唯一编码", width = 15)
    @Schema(description = "MDM系统唯一编码")
    private String sysUniqueCode;
	/**业务唯一标识 UNIQ_ID*/
	@Excel(name = "业务唯一标识 UNIQ_ID", width = 15)
    @Schema(description = "业务唯一标识 UNIQ_ID")
    private String uniqId;
	/**TAG功能位置编码 TAG_FUNLOC_CODE*/
	@Excel(name = "TAG功能位置编码 TAG_FUNLOC_CODE", width = 15)
    @Schema(description = "TAG功能位置编码 TAG_FUNLOC_CODE")
    private String tagFunlocCode;
	/**TAG功能位置层级描述 TAG_FUNLOC_CODE2*/
	@Excel(name = "TAG功能位置层级描述 TAG_FUNLOC_CODE2", width = 15)
    @Schema(description = "TAG功能位置层级描述 TAG_FUNLOC_CODE2")
    private String tagFunlocCode2;
	/**功能位置描述 V_FUNLOC_DESC*/
	@Excel(name = "功能位置描述 V_FUNLOC_DESC", width = 15)
    @Schema(description = "功能位置描述 V_FUNLOC_DESC")
    private String vfunlocDesc;
	/**功能位置描述2 V_FUNLOC_DESC2*/
	@Excel(name = "功能位置描述2 V_FUNLOC_DESC2", width = 15)
    @Schema(description = "功能位置描述2 V_FUNLOC_DESC2")
    private String vfunlocDesc2;
	/**IOT属性编码 IOT_ATTR_CODE*/
	@Excel(name = "IOT属性编码 IOT_ATTR_CODE", width = 15)
    @Schema(description = "IOT属性编码 IOT_ATTR_CODE")
    private String iotAttrCode;
	/**IOT属性显示名称 IOT_ATTR_DISP_NAME*/
	@Excel(name = "IOT属性显示名称 IOT_ATTR_DISP_NAME", width = 15)
    @Schema(description = "IOT属性显示名称 IOT_ATTR_DISP_NAME")
    private String iotAttrDispName;
	/**报警位 TAG_BIT*/
	@Excel(name = "报警位 TAG_BIT", width = 15)
    @Schema(description = "报警位 TAG_BIT")
    private String tagBit;
	/**报警位描述 TAG_BIT_DESC*/
	@Excel(name = "报警位描述 TAG_BIT_DESC", width = 15)
    @Schema(description = "报警位描述 TAG_BIT_DESC")
    private String tagBitDesc;
	/**报警位描述2 TAG_BIT_DESC2*/
	@Excel(name = "报警位描述2 TAG_BIT_DESC2", width = 15)
    @Schema(description = "报警位描述2 TAG_BIT_DESC2")
    private String tagBitDesc2;
	/**来源系统ID*/
	@Excel(name = "来源系统ID", width = 15)
    @Schema(description = "来源系统ID")
    private String dataSourceSysId;
	/**是否启用 Y/N*/
	@Excel(name = "是否启用", width = 15)
    @Schema(description = "是否启用")
    private String enableFlag;
	/**校验错误信息*/
	@Excel(name = "校验错误信息", width = 15)
    @Schema(description = "校验错误信息")
    private String checkErrMsg;
	/**主数据版本*/
	@Excel(name = "主数据版本", width = 15)
    @Schema(description = "主数据版本")
    private String rtMdmDataVersion;
	/**来源 ORIGIN*/
	@Excel(name = "来源 ORIGIN", width = 15)
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
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**创建时间*/
    @Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**更新时间*/
    @Excel(name = "更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
	/**同步时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;
}
