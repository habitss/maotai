package com.keytech.modules.line.efficiency.entity;

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
 * @Description: 酒损记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_filerloss_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="酒损记录表")
public class LineEfficiencyFilerlossRecord implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**产线代码*/
	@Excel(name = "产线代码", width = 15)
    @Schema(description = "产线代码")
    private String lineCode;

	/**产线名称*/
	@Excel(name = "产线名称", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**运行状态 0未运行 1运行*/
	@Excel(name = "运行状态 0未运行 1运行", width = 15)
    @Schema(description = "运行状态 0未运行 1运行")
    private String stateCode;

	/**开始时间*/
	@Excel(name = "开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private Date startTime;

	/**结束时间*/
	@Excel(name = "结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private Date endTime;

	/**运行时长（秒）*/
	@Excel(name = "运行时长（秒）", width = 15)
    @Schema(description = "运行时长（秒）")
    private long runningTime;

	/**0十分钟酒机批次，1酒机批次，2总批次*/
	@Excel(name = "0十分钟酒机批次，1酒机批次，2总批次", width = 15)
    @Schema(description = "0十分钟酒机批次，1酒机批次，2总批次")
    private Integer type;

	/**酒机批次产量*/
	@Excel(name = "酒机批次产量", width = 15)
    @Schema(description = "酒机批次产量")
    private Double fillProduction;

	/**酒机批次接酒量*/
	@Excel(name = "酒机批次接酒量", width = 15)
    @Schema(description = "酒机批次接酒量")
    private Double fillReceivingCapacity;

	/**酒机批次酒损*/
	@Excel(name = "酒机批次酒损", width = 15)
    @Schema(description = "酒机批次酒损")
    private Double fillFilerloss;

	/**码垛机批次产量*/
	@Excel(name = "码垛机批次产量", width = 15)
    @Schema(description = "码垛机批次产量")
    private Double palProduction;

	/**码垛机批次酒损*/
	@Excel(name = "码垛机批次酒损", width = 15)
    @Schema(description = "码垛机批次酒损")
    private Double palFilerloss;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag = 0;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;
}
