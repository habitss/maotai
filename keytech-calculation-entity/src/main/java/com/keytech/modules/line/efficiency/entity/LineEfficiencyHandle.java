package com.keytech.modules.line.efficiency.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: 线效率-产量/线效状态点位配置
 * @Author: jeecg-boot
 * @Date:   2025-05-26
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_handle")
@Schema(description="线效率-产量/线效状态点位配置")
public class LineEfficiencyHandle implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**产线code*/
    @Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

    /**产线名称*/
    @Excel(name = "产线名称", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    private String sbId;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String sbName;

	/**原点位通道号*/
	@Excel(name = "原点位通道号", width = 15)
    @Schema(description = "原点位通道号")
    private String pointCode;

	/**原点位名称*/
	@Excel(name = "原点位名称", width = 15)
    @Schema(description = "原点位名称")
    private String pointName;

	/**新虚拟点位通道号*/
	@Excel(name = "新虚拟点位通道号", width = 15)
    @Schema(description = "新虚拟点位通道号")
    private String pointCodeNew;

	/**新虚拟点位名称*/
	@Excel(name = "新虚拟点位名称", width = 15)
    @Schema(description = "新虚拟点位名称")
    private String pointNameNew;

	/**虚拟点位处理方式（0防清零/1线效状态开机/2线效状态关机/3开关机信号）*/
	@Excel(name = "虚拟点位处理方式（0防清零/1线效状态开机/2线效状态关机/3开关机信号/4报警状态）", width = 15)
    @Schema(description = "虚拟点位处理方式（0防清零/1线效状态开机/2线效状态关机/3开关机信号/4报警状态）")
    private Integer handleType;

	/**初始时间*/
	@Excel(name = "初始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "初始时间")
    private Date initialTime;

	/**初始产量*/
	@Excel(name = "初始产量", width = 15)
    @Schema(description = "初始产量")
    private Double initialNum;

	/**滤波上限*/
	@Excel(name = "滤波上限", width = 15)
    @Schema(description = "滤波上限")
    private Double filterUp;

	/**滤波下限*/
	@Excel(name = "滤波下限", width = 15)
    @Schema(description = "滤波下限")
    private Double filterDown;

    /**额定产量*/
    @Excel(name = "额定产量", width = 15)
    @Schema(description = "额定产量")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double ratedProduction;

    /**是否推送*/
    @Excel(name = "是否推送", width = 15)
    @Schema(description = "是否推送")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer ifSend;

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

    /**线效点位编码*/
    @Excel(name = "线效点位编码", width = 15)
    @Schema(description = "线效点位编码")
    private String lineEfficiencyPointCode;

    /**防清零产量点位编码*/
    @Excel(name = "防清零产量点位编码", width = 15)
    @Schema(description = "防清零产量点位编码")
    private String lineProductionPointCode;

    /**计算类型1（1> 2>= 3= 4!= 5< 6<=）*/
    @Excel(name = "计算类型1（1> 2>= 3= 4!= 5< 6<=）", width = 15)
    @Schema(description = "计算类型1（1> 2>= 3= 4!= 5< 6<=）")
    private String leftConditionType;

    /**计算值1*/
    @Excel(name = "计算值1", width = 15)
    @Schema(description = "计算值1")
    private Double leftConditionValue;

    /**计算类型2（1> 2>= 3= 4!= 5< 6<=）*/
    @Excel(name = "计算类型2（1> 2>= 3= 4!= 5< 6<=）", width = 15)
    @Schema(description = "计算类型2（1> 2>= 3= 4!= 5< 6<=）")
    private String rightConditionType;

    /**计算值2*/
    @Excel(name = "计算值2", width = 15)
    @Schema(description = "计算值2")
    private Double rightConditionValue;

    /**0代表或的关系，1代表且的关系*/
    @Excel(name = "0代表或的关系，1代表且的关系", width = 15)
    @Schema(description = "0代表或的关系，1代表且的关系")
    private String conditionSymbol;


    /**点位最新时间*/
    @Schema(description = "点位最新时间")
    @TableField(exist = false)
    private Date latestTime;

    /**点位最新值*/
    @Schema(description = "点位最新值")
    @TableField(exist = false)
    private Double latestValue;


    /**cip运行信号-通道号*/
    @Schema(description = "cip运行信号-通道号")
    @TableField(exist = false)
    private String cipCode;

    /**cip运行信号-点位名称*/
    @Schema(description = "cip运行信号-点位名称")
    @TableField(exist = false)
    private String cipName;

    /**进酒温度-通道号*/
    @Schema(description = "进酒温度-通道号")
    @TableField(exist = false)
    private String jjwdCode;

    /**进酒温度-点位名称*/
    @Schema(description = "进酒温度-点位名称")
    @TableField(exist = false)
    private String jjwdName;

    /**SKU配置集合*/
    @Schema(description = "SKU配置集合")
    @TableField(exist = false)
    private List<LineEfficiencySku> lineEfficiencySkuList;

    private static final long serialVersionUID = 1L;
}
