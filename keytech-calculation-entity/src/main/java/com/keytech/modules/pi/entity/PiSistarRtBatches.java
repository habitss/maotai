package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: BM-实时订单记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-23
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_sistar_rt_batches")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="BM-实时订单记录表")
public class PiSistarRtBatches implements Serializable {


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
    private Integer reccatNo;

	/**orderNo*/
	@Excel(name = "orderNo", width = 15)
    @Schema(description = "orderNo")
    private Integer orderNo;

	/**batchNo*/
	@Excel(name = "batchNo", width = 15)
    @Schema(description = "batchNo")
    private Integer batchNo;

	/**actualStatus*/
	@Excel(name = "actualStatus", width = 15)
    @Schema(description = "actualStatus")
    private String actualStatus;

	/**batchSize*/
	@Excel(name = "batchSize", width = 15)
    @Schema(description = "batchSize")
    private Double batchSize;

	/**batchName*/
	@Excel(name = "batchName", width = 15)
    @Schema(description = "batchName")
    private String batchName;

	/**ordercatName*/
	@Excel(name = "ordercatName", width = 15)
    @Schema(description = "ordercatName")
    private String ordercatName;

	/**reccatName*/
	@Excel(name = "reccatName", width = 15)
    @Schema(description = "reccatName")
    private String reccatName;

	/**recipeNo*/
	@Excel(name = "recipeNo", width = 15)
    @Schema(description = "recipeNo")
    private Integer recipeNo;

	/**recipeName*/
	@Excel(name = "recipeName", width = 15)
    @Schema(description = "recipeName")
    private String recipeName;

	/**recprocNo*/
	@Excel(name = "recprocNo", width = 15)
    @Schema(description = "recprocNo")
    private Integer recprocNo;

	/**lineNo*/
	@Excel(name = "lineNo", width = 15)
    @Schema(description = "lineNo")
    private Integer lineNo;

	/**productId*/
	@Excel(name = "productId", width = 15)
    @Schema(description = "productId")
    private Integer productId;

	/**productName*/
	@Excel(name = "productName", width = 15)
    @Schema(description = "productName")
    private String productName;

	/**startPcuNo*/
	@Excel(name = "startPcuNo", width = 15)
    @Schema(description = "startPcuNo")
    private Integer startPcuNo;

	/**startUnitNo*/
	@Excel(name = "startUnitNo", width = 15)
    @Schema(description = "startUnitNo")
    private Integer startUnitNo;

	/**startMode*/
	@Excel(name = "startMode", width = 15)
    @Schema(description = "startMode")
    private String startMode;

	/**plannedStartTime*/
	@Excel(name = "plannedStartTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "plannedStartTime")
    private Date plannedStartTime;

	/**actualStartTime*/
	@Excel(name = "actualStartTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "actualStartTime")
    private Date actualStartTime;

	/**actualEndTime*/
	@Excel(name = "actualEndTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "actualEndTime")
    private Date actualEndTime;

	/**insertTimestamp*/
	@Excel(name = "insertTimestamp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "insertTimestamp")
    private Date insertTimestamp;

	/**updateTimestamp*/
	@Excel(name = "updateTimestamp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "updateTimestamp")
    private Date updateTimestamp;

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
}
