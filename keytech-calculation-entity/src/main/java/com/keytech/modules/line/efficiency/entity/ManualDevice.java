package com.keytech.modules.line.efficiency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @Description: 上位机系统手动操作设备对照
 * @Author: jeecg-boot
 * @Date:   2025-12-08
 * @Version: V1.0
 */
@Data
@TableName("mes3_manual_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="上位机系统手动操作设备对照")
public class ManualDevice implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
    @Schema(description = "产线Code")
    private String lineCode;

	/**区域*/
    @Schema(description = "区域")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION'", dicText = "item_key", dicCode = "item_val")
    @Excel(name = "区域", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION'", dicText = "item_key", dicCode = "item_val")
    private String region;

	/**设备编号*/
	@Excel(name = "设备编号", width = 15)
    @Schema(description = "设备编号")
    private String deviceCode;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String deviceName;

	/**是否启用：0-禁用，1-启用*/
	@Excel(name = "是否启用", width = 15, replace = { "启用_1", "禁用_0"})
    @Schema(description = "是否启用：0-禁用，1-启用")
    private Integer isEnabled;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;
}
