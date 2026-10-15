package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @Description: Adapter-步骤
 * @Author: HQ
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Data
@TableName("sistar_rt_steps")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Adapter数据抽取-步骤")
public class SourceSistarRtSteps implements Serializable {

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
    private Integer reccatNo;

	/**订单号*/
	@Excel(name = "订单号", width = 15)
    @Schema(description = "orderNo")
    private Integer orderNo;

	/**批量号*/
	@Excel(name = "批量号", width = 15)
    @Schema(description = "batchNo")
    private Integer batchNo;

	/**pcuNo*/
	@Excel(name = "pcuNo", width = 15)
    @Schema(description = "pcuNo")
    private Integer pcuNo;

	/**设备单元*/
	@Excel(name = "unitNo", width = 15)
    @Schema(description = "unitNo")
    private Integer unitNo;

	/**eopNo*/
	@Excel(name = "eopNo", width = 15)
    @Schema(description = "eopNo")
    private Integer eopNo;

	/**startTime*/
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

	/**duration*/
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

	/**配方*/
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

	/**stepStatus*/
	@Excel(name = "stepStatus", width = 15)
    @Schema(description = "stepStatus")
    private String stepStatus;

	/**insertTimestamp*/
	@Excel(name = "insertTimestamp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "insertTimestamp")
    private Date insertTimestamp;

	/**userData*/
	@Excel(name = "userData", width = 15)
    @Schema(description = "userData")
    private Integer userData;

    private static final long serialVersionUID = 1L;
}
