package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

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
 * @Description: Adapter数据-步骤参数
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_sistar_rt_step_params")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Adapter数据-步骤明细")
public class PiSistarRtStepParams implements Serializable {


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

	/**yearNo*/
	@Excel(name = "yearNo", width = 15)
    @Schema(description = "yearNo")
    private Integer yearNo;

	/**reccatNo*/
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

	/**EOP号*/
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

	/**paramPos*/
	@Excel(name = "paramPos", width = 15)
    @Schema(description = "paramPos")
    private Integer paramPos;

	/**eparNo*/
	@Excel(name = "eparNo", width = 15)
    @Schema(description = "eparNo")
    private String eparNo;

	/**参数名*/
	@Excel(name = "paramName", width = 15)
    @Schema(description = "paramName")
    private String paramName;

	/**实际值*/
	@Excel(name = "actualValue", width = 15)
    @Schema(description = "actualValue")
    private String actualValue;

	/**设定值*/
	@Excel(name = "setpointValue", width = 15)
    @Schema(description = "setpointValue")
    private String setpointValue;

	/**单位*/
	@Excel(name = "uom", width = 15)
    @Schema(description = "uom")
    private String uom;

	/**插入时间*/
	@Excel(name = "insertTimestamp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "insertTimestamp")
    private Date insertTimestamp;

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

    //写入时间起
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date insertTimestampBegin;

    //写入时间止
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date insertTimestampEnd;
}
