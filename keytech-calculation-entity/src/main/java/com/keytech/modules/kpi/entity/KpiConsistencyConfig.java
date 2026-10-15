package com.keytech.modules.kpi.entity;

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
 * @Description: 一致KPI数据配置
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_consistency_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="一致性KPI数据配置")
public class KpiConsistencyConfig implements Serializable {


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

	/**工厂编码*/
	@Excel(name = "工厂编码", width = 15)
    @Schema(description = "工厂编码")
    private String factory;

	/**工厂名称*/
	@Excel(name = "工厂名称", width = 15)
    @Schema(description = "工厂名称")
    private String factoryName;

	/**KPI编码*/
	@Excel(name = "KPI编码", width = 15)
    @Schema(description = "KPI编码")
    private String kpiCode;

	/**KPI名称*/
	@Excel(name = "KPI名称", width = 15)
    @Schema(description = "KPI名称")
    private String kpiName;

	/**PI编码*/
	@Excel(name = "PI编码", width = 15)
    @Schema(description = "PI编码")
    private String piCode;

	/**PI名称*/
	@Excel(name = "PI名称", width = 15)
    @Schema(description = "PI名称")
    private String piName;

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

	/**权重*/
	@Excel(name = "权重", width = 15)
    @Schema(description = "权重")
    private String weight;

	/**目标值*/
	@Excel(name = "目标值", width = 15)
    @Schema(description = "目标值")
    private String targetValue;

	/**层级(0工厂/1一级/2二级/3三级)*/
	@Excel(name = "层级(0工厂/1一级/2二级/3三级)", width = 15)
    @Schema(description = "层级(0工厂/1一级/2二级/3三级)")
    private Integer level;

	/**
     * 配置配置-PI
     * 格式
     * {"type":2,"datas":[{"tableId":"1","fieldId":"field_0001","timeFieldId":"field_0003"},
     *                   {"tableId":"1","fieldId":"field_0003","timeFieldId":"field_0004"}]
     * }
     * */
	@Excel(name = "配置配置-PI", width = 15)
    @Schema(description = "配置配置-PI")
    private String batchConfig;

	/**
     * 合格率配置-KPI
     * 格式 {"type":1,"kpiCode":"809"}
     * */
	@Excel(name = "合格率配置-KPI", width = 15)
    @Schema(description = "合格率配置-KPI")
    private String passRateConfig;

	/**重复次数配置-PI*/
	@Excel(name = "重复次数配置-PI", width = 15)
    @Schema(description = "重复次数配置-PI")
    private String repeatCountConfig;

	/**重复率配置-PI*/
	@Excel(name = "重复率配置-PI", width = 15)
    @Schema(description = "重复率配置-PI")
    private String repeatRateConfig;

	/**是否需要配置 1是 0否*/
	@Excel(name = "是否需要配置 1是 0否", width = 15)
    @Schema(description = "是否需要配置 1是 0否")
    private Integer needConfig;

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

    /**KPI编码 (mes平台)*/
    @TableField(exist = false)
    private String kpiMesCode;

    /**月累*/
    @TableField(exist = false)
    private String monthCumulative;
    /**月年累*/
    @TableField(exist = false)
    private String yearMonthCumulative;
    /**周累*/
    //@TableField(exist = false)
    //private String weekCumulative;

    //绿区批次green_batch_count
    @TableField(exist = false)
    private Integer greenBatchCount;
    //总批次
    @TableField(exist = false)
    private Integer totalBatchCount;
    //合格率
    @TableField(exist = false)
    private String passRate;
    //重复次数
    @TableField(exist = false)
    private Integer repeatCount;
    //重复率
    @TableField(exist = false)
    private String repeatRate;
    //week_date日期(周) 格式：yyyy-MM-dd
    @TableField(exist = false)
    private String weekDate;

}
