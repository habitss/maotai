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
 * @Description: Adapter数据抽取-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_sistar_rt_steps")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Adapter数据抽取-步骤")
public class PiSistarRtSteps implements Serializable {


	/**id*/
//	@TableId(type = IdType.ASSIGN_ID)
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

	/**siteNo*/
	@Excel(name = "siteNo", width = 15)
    @Schema(description = "siteNo")
    private Integer siteNo;

	/**areaNo*/
	@Excel(name = "areaNo", width = 15)
    @Schema(description = "areaNo")
    private Integer areaNo;

	/**年份*/
	@Excel(name = "yearNo", width = 15)
    @Schema(description = "yearNo")
    private Integer yearNo;

	/**设备*/
	@Excel(name = "reccatNo", width = 15)
    @Schema(description = "reccatNo")
    @Dict(dictTable ="mes3_pi_sistar_eng_reccats",dicText = "reccat_name",dicCode = "reccat_no")
    private Integer reccatNo;

	/**订单号*/
	@Excel(name = "orderNo", width = 15)
    @Schema(description = "orderNo")
    private String orderNo;

	/**批次号*/
	@Excel(name = "batchNo", width = 15)
    @Schema(description = "batchNo")
    private String batchNo;

	/**pcuNo*/
	@Excel(name = "pcuNo", width = 15)
    @Schema(description = "pcuNo")
    @Dict(dictTable ="mes3_pi_sistar_eng_pcus",dicText = "pcu_name",dicCode = "pcu_no")
    private Integer pcuNo;

	/**设备单元*/
	@Excel(name = "unitNo", width = 15)
    @Schema(description = "unitNo")
    @Dict(dictTable ="mes3_pi_sistar_eng_units",dicText = "unit_name",dicCode = "unit_no")
    private String unitNo;

	/**eopNo*/
	@Excel(name = "eopNo", width = 15)
    @Schema(description = "eopNo")
    @Dict(dictTable ="mes3_pi_sistar_eng_eops",dicText = "eop_name",dicCode = "eop_no")
    private Integer eopNo;

	/**开始时间*/
	@Excel(name = "startTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "startTime")
    private Date startTime;

	/**ropId*/
	@Excel(name = "ropId", width = 15)
    @Schema(description = "ropId")
    private Integer ropId;

	/**userProtId*/
	@Excel(name = "userProtId", width = 15)
    @Schema(description = "userProtId")
    private Integer userProtId;

	/**eventTime*/
	@Excel(name = "eventTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "eventTime")
    private Date eventTime;

	/**endTime*/
	@Excel(name = "endTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "endTime")
    private Date endTime;

	/**monitoringTime*/
	@Excel(name = "monitoringTime", width = 15)
    @Schema(description = "monitoringTime")
    private Integer monitoringTime;

	/**持续时间*/
	@Excel(name = "duration", width = 15)
    @Schema(description = "duration")
    private Integer duration;

	/**ordercatName*/
	@Excel(name = "ordercatName", width = 15)
    @Schema(description = "ordercatName")
    private String ordercatName;

	/**reccatName*/
	@Excel(name = "reccatName", width = 15)
    @Schema(description = "reccatName")
    private String reccatName;

	/**配方号*/
	@Excel(name = "recipeNo", width = 15)
    @Schema(description = "recipeNo")
    @Dict(dictTable ="mes3_pi_sistar_eng_recipes",dicText = "recipe_name",dicCode = "recipe_no")
    private Integer recipeNo;

	/**配方名称*/
	@Excel(name = "recipeName", width = 15)
    @Schema(description = "recipeName")
    private String recipeName;

	/**recprocNo*/
	@Excel(name = "recprocNo", width = 15)
    @Schema(description = "recprocNo")
    private Integer recprocNo;

	/**productId*/
	@Excel(name = "productId", width = 15)
    @Schema(description = "productId")
    private Integer productId;

	/**productName*/
	@Excel(name = "productName", width = 15)
    @Schema(description = "productName")
    private String productName;

	/**equipmentName*/
	@Excel(name = "equipmentName", width = 15)
    @Schema(description = "equipmentName")
    private String equipmentName;

	/**equipmentId*/
	@Excel(name = "equipmentId", width = 15)
    @Schema(description = "equipmentId")
    private String equipmentId;

	/**pcuName*/
	@Excel(name = "pcuName", width = 15)
    @Schema(description = "pcuName")
    private String pcuName;

	/**unitName*/
	@Excel(name = "unitName", width = 15)
    @Schema(description = "unitName")
    private String unitName;

	/**eopName*/
	@Excel(name = "eopName", width = 15)
    @Schema(description = "eopName")
    private String eopName;

	/**paramCount*/
	@Excel(name = "paramCount", width = 15)
    @Schema(description = "paramCount")
    private Integer paramCount;

	/**步骤状态*/
	@Excel(name = "stepStatus", width = 15)
    @Schema(description = "stepStatus")
    private String stepStatus;

	/**插入时间*/
	@Excel(name = "insertTimestamp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "insertTimestamp")
    private Date insertTimestamp;

	/**userData*/
	@Excel(name = "userData", width = 15)
    @Schema(description = "userData")
    private Integer userData;

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

    private static final long serialVersionUID = 1L;


    @TableField(exist = false)
    @Schema(description = "设备集合")
    private String unitNames;

    /**订单批次集合 订单-批次,订单-批次*/
    @TableField(exist = false)
    @Schema(description = "订单批次集合")
    private String orderAndBatch;

    /**点位集合 */
    @TableField(exist = false)
    @Schema(description = "点位集合")
    private String points;

    /**pcu集合 */
    @TableField(exist = false)
    @Schema(description = "pcu集合")
    private String pcuNos;

    /**是否发酵 */
    @TableField(exist = false)
    @Schema(description = "是否发酵")
    private Boolean isFermentation;
    /**步骤集合**/
    @TableField(exist = false)
    @Schema(description = "步骤集合")
    private List<PiSistarRtSteps> stepList;

    /**设备ID*/
    @TableField(exist = false)
    private String unitId;
    /**曲线数据源*/
    @TableField(exist = false)
    private String curveDataType;

    @TableField(exist = false)
    private String pcuNoStr;
}
