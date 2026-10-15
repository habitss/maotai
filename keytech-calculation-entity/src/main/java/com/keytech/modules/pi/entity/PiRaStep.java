package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: RA-步骤
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_ra_step")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="RA-步骤")
public class PiRaStep implements Serializable {


	/**id*/
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**数据插入时间*/
	@Excel(name = "数据插入时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据插入时间")
    private Date insertDt;

	/**事件发生时间(开始时间)*/
	@Excel(name = "事件发生时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "事件发生时间")
    private Date eventTime;

	/**区域ID*/
	@Excel(name = "区域ID", width = 15)
    @Schema(description = "区域ID")
    private Integer areaId;

	/**区域名称*/
	@Excel(name = "区域名称", width = 15)
    @Schema(description = "区域名称")
    private String areaName;

	/**设备ID*/
	@Excel(name = "设备ID", width = 15)
    @Schema(description = "设备ID")
    private String unitId;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String unitName;

	/**订单ID*/
	@Excel(name = "订单ID", width = 15)
    @Schema(description = "订单ID")
    private Integer orderId;

	/**批次号*/
	@Excel(name = "批次号", width = 15)
    @Schema(description = "批次号")
    private Integer batchNr;

	/**配方ID*/
	@Excel(name = "配方ID", width = 15)
    @Schema(description = "配方ID")
    private Integer recipeId;

	/**配方名称*/
	@Excel(name = "配方名称", width = 15)
    @Schema(description = "配方名称")
    private String recipeName;

	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @Schema(description = "批次ID")
    private Integer batchId;

	/**批次类型名称*/
	@Excel(name = "批次类型名称", width = 15)
    @Schema(description = "批次类型名称")
    private String batchTypeName;

	/**状态*/
	@Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private String status;

	/**BOP ID*/
	@Excel(name = "BOP ID", width = 15)
    @Schema(description = "BOP ID")
    private Integer bopId;

	/**BOP名称*/
	@Excel(name = "BOP名称", width = 15)
    @Schema(description = "BOP名称")
    private String bopName;

	/**步骤号*/
	@Excel(name = "步骤号", width = 15)
    @Schema(description = "步骤号")
    private Integer stepNr;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String bk;

	/**单元时间戳*/
	@Excel(name = "单元时间戳", width = 15)
    @Schema(description = "单元时间戳")
    private String unitStamp;

	/**BOP步骤时间戳*/
	@Excel(name = "BOP步骤时间戳", width = 15)
    @Schema(description = "BOP步骤时间戳")
    private String bopStepStamp;

    /**结束时间*/
    @Excel(name = "结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private Date endTime;

    /**持续时间*/
    @Excel(name = "持续时间", width = 20)
    @Schema(description = "持续时间")
    private Long duration;

    /**产线id*/
    @Excel(name = "产线id", width = 15)
    @Schema(description = "产线id")
    private Integer lineId;
    /**产线名称*/
    @Excel(name = "产线名称", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
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

    /**点位集合 */
    @TableField(exist = false)
    @Schema(description = "点位集合")
    private String points;

    /**订单批次集合 订单-批次,订单-批次*/
    @TableField(exist = false)
    @Schema(description = "订单批次集合")
    private String orderAndBatch;

    @TableField(exist = false)
    @Schema(description = "设备集合")
    private String unitNames;

    /**开始时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    @TableField(exist = false)
    private Date startTime;

    /**下一批0/上一批1*/
    @TableField(exist = false)
    @Schema(description = "下一批/上一批")
    private String pageType;

    /**步骤名称*/
    @TableField(exist = false)
    @Schema(description = "步骤名称")
    private String eopName;

    /**步骤id*/
    @TableField(exist = false)
    @Schema(description = "步骤id")
    private Integer ropId;

    /**分组名称**/
    @TableField(exist = false)
    @Schema(description = "分组名称")
    private String groupName;

    /**步骤集合**/
    @TableField(exist = false)
    @Schema(description = "步骤集合")
    private List<PiRaStep> stepList;

    private static final long serialVersionUID = 1L;

}
