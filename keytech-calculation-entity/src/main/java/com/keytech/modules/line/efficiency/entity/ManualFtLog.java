package com.keytech.modules.line.efficiency.entity;

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
 * @Description: 上位机系统手动操作记录
 * @Author: jeecg-boot
 * @Date:   2025-11-13
 * @Version: V1.0
 */
@Data
@TableName("mes3_manual_ft_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="上位机系统手动操作记录")
public class ManualFtLog implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
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

	/**recordId*/
	@Excel(name = "recordId", width = 15)
    @Schema(description = "recordId")
    private String recordId;

	/**parentId*/
	@Excel(name = "parentId", width = 15)
    @Schema(description = "parentId")
    private String parentId;

	/**sequenceNumber*/
	@Excel(name = "sequenceNumber", width = 15)
    @Schema(description = "sequenceNumber")
    private Integer sequenceNumber;

	/**chainTime*/
	@Excel(name = "chainTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "chainTime")
    private Date chainTime;

	/**TimeStmp*/
	@Excel(name = "TimeStmp", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "TimeStmp")
    @TableField("TimeStmp")
    private Date timeStmp;

	/**messageText*/
	@Excel(name = "messageText", width = 15)
    @Schema(description = "messageText")
    private String messageText;

	/**audience*/
	@Excel(name = "audience", width = 15)
    @Schema(description = "audience")
    private Integer audience;

	/**severity*/
	@Excel(name = "severity", width = 15)
    @Schema(description = "severity")
    private Integer severity;

	/**verbosity*/
	@Excel(name = "verbosity", width = 15)
    @Schema(description = "verbosity")
    private Integer verbosity;

	/**location*/
	@Excel(name = "location", width = 15)
    @Schema(description = "location")
    private String location;

	/**provider*/
	@Excel(name = "provider", width = 15)
    @Schema(description = "provider")
    private String provider;

	/**userId*/
	@Excel(name = "userId", width = 15)
    @Schema(description = "userId")
    private String userId;

	/**userFullName*/
	@Excel(name = "userFullName", width = 15)
    @Schema(description = "userFullName")
    private String userFullName;

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
