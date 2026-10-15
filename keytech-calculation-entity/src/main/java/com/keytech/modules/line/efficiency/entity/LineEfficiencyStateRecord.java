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
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 线效率-线效状态记录表
 * @Author: jeecg-boot
 * @Date:   2025-06-04
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_state_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="线效率-线效状态记录表")
public class LineEfficiencyStateRecord implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**产线code*/
    //@Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

    /**产线名称*/
    @Excel(name = "产线", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**设备id*/
	//@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    private String sbId;

	/**设备名称*/
	//@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String sbName;

	/**运行状态 0未运行 1运行*/
	//@Excel(name = "运行状态 0未运行 1运行", width = 15)
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
    private Long runningTime;

	/**分类0线效状态、1设备运行状态*/
	//@Excel(name = "分类0线效状态、1设备运行状态", width = 15)
    @Schema(description = "分类0线效状态、1设备运行状态、2报警状态，3实时线效，4非生产能耗，5生产能耗")
    private Integer type;

    /**线效实际结束时间*/
    //@Excel(name = "线效实际结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "线效实际结束时间")
    private Date endTimeActual;

    /**线效值*/
    //@Excel(name = "线效值", width = 15)
    @Schema(description = "线效值")
    private String lineEfficiencyValue;

    /**线效值*/
    @Excel(name = "产量", width = 15)
    @Schema(description = "产量")
    private String production;

    /**实际线效值*/
    @Excel(name = "OPI", width = 15)
    @Schema(description = "实际线效值")
    private String actualEfficiencyValue;

    /**额定产量*/
    //@Excel(name = "额定产量", width = 15)
    @Schema(description = "额定产量")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double ratedProduction;

    /**报警名称*/
    //@Excel(name = "报警名称", width = 15)
    @Schema(description = "报警名称")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String alarmName;


	/**排序*/
	//@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	//@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;

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

    /**能耗量*/
    //@Excel(name = "能耗量", width = 15)
    @Schema(description = "能耗量")
    private Double energyConsumption;


    /**产量KL数*/
//    @Excel(name = "产量KL数", width = 15)
    @Schema(description = "产量KL数")
    private Double productionKl;

    /**瓶单耗*/
    //@Excel(name = "瓶单耗", width = 15)
    @Schema(description = "瓶单耗")
    private Double bottleConsumption;

    /**酒单耗*/
    //@Excel(name = "酒单耗", width = 15)
    @Schema(description = "酒单耗")
    private Double wineConsumption;

    /**入库产量(KL数)*/
    //@Excel(name = "入库产量(KL数)", width = 15)
    @Schema(description = "入库产量(KL数)")
    private Double productionStoredCapacity;

    /**入库产量(瓶/听数)*/
    //@Excel(name = "入库产量(瓶/听数)", width = 15)
    @Schema(description = "入库产量(瓶/听数)")
    private Double productionStoredNum;

    /**生产类型*/
    //@Excel(name = "生产类型", width = 15)
    @Schema(description = "生产类型")
    private String productionType;

    /**生产周期*/
    //@Excel(name = "生产周期", width = 15)
    @Schema(description = "生产周期")
    private String productionCycle;

    /**首台设备开机时间*/
    //@Excel(name = "首台设备开机时间", width = 15)
    @Schema(description = "首台设备开机时间")
    private Date firstStartTime;

    /**末台设备开机时间*/
    //@Excel(name = "末台设备开机时间", width = 15)
    @Schema(description = "末台设备开机时间")
    private Date lastStartTime;

    @Schema(description="配置信息")
    @TableField(exist = false)
    private List<LineEfficiencyHandle> lineEfficiencyHandleList;

    private static final long serialVersionUID = 1L;
}
