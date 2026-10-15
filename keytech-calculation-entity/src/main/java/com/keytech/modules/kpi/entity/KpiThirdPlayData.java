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
 * @Description: 第三方接口-维护KPI
 * @Author: jeecg-boot
 * @Date:   2025-03-13
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_third_play_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="第三方接口-维护KPI")
public class KpiThirdPlayData implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
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

	/**维护KPI-主键*/
	@Excel(name = "维护KPI-主键", width = 15)
    @Schema(description = "维护KPI-主键")
    private Integer indocno;

	/**?*/
	@Excel(name = "?", width = 15)
    @Schema(description = "?")
    private Integer sregid;

	/**指标创建时间*/
	@Excel(name = "指标创建时间", width = 15)
    @Schema(description = "指标创建时间")
    private String dregt;

	/**唯一键*/
	@Excel(name = "唯一键", width = 15)
    @Schema(description = "唯一键")
    private String skey;

	/**指标ID*/
	@Excel(name = "指标ID", width = 15)
    @Schema(description = "指标ID")
    private String ilibraryid;

	/**指标名称*/
	@Excel(name = "指标名称", width = 15)
    @Schema(description = "指标名称")
    private String stitle;

	/**单位(%,h,min,元/吨,万元,次)*/
	@Excel(name = "单位(%,h,min,元/吨,万元,次)", width = 15)
    @Schema(description = "单位(%,h,min,元/吨,万元,次)")
    private String sunit;

	/**日期*/
	@Excel(name = "日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "日期")
    private Date datekpi;

	/**计划值*/
	@Excel(name = "计划值", width = 15)
    @Schema(description = "计划值")
    private String iplanvalue;

	/**实际值*/
	@Excel(name = "实际值", width = 15)
    @Schema(description = "实际值")
    private String iactualvalue;

	/**状态 (白班1,中班2,夜班3,日累4,周累5,月累6,年累7)*/
	@Excel(name = "状态 (白班1,中班2,夜班3,日累4,周累5,月累6,年累7)", width = 15)
    @Schema(description = "状态 (白班1,中班2,夜班3,日累4,周累5,月累6,年累7)")
    private String state;

	/**指标排序*/
	@Excel(name = "指标排序", width = 15)
    @Schema(description = "指标排序")
    private String isortlib;

	/**指标对象排序*/
	@Excel(name = "指标对象排序", width = 15)
    @Schema(description = "指标对象排序")
    private String isortobj;

	/**状态标记(KPI模板 1线数据2 默认0)*/
	@Excel(name = "状态标记(KPI模板 1线数据2 默认0)", width = 15)
    @Schema(description = "状态标记(KPI模板 1线数据2 默认0)")
    private String istate;

	/**区域ID*/
	@Excel(name = "区域ID", width = 15)
    @Schema(description = "区域ID")
    private String isystemid;

	/**部门ID*/
	@Excel(name = "部门ID", width = 15)
    @Schema(description = "部门ID")
    private String ideptid;

	/**人员ID*/
	@Excel(name = "人员ID", width = 15)
    @Schema(description = "人员ID")
    private String iuserid;

	/**作业班组*/
	@Excel(name = "作业班组", width = 15)
    @Schema(description = "作业班组")
    private String swbz;

	/**工厂ID*/
	@Excel(name = "工厂ID", width = 15)
    @Schema(description = "工厂ID")
    private String icompanyid;

	/**标识*/
	@Excel(name = "标识", width = 15)
    @Schema(description = "标识")
    private String itype;

	/**实际值分子*/
	@Excel(name = "实际值分子", width = 15)
    @Schema(description = "实际值分子")
    private String inumerator;

	/**实际值分母*/
	@Excel(name = "实际值分母", width = 15)
    @Schema(description = "实际值分母")
    private String idenominator;

	/**未减免kpi实际值*/
	@Excel(name = "未减免kpi实际值", width = 15)
    @Schema(description = "未减免kpi实际值")
    private String iorgvalue;

	/**?*/
	@Excel(name = "?", width = 15)
    @Schema(description = "?")
    private String dstitle;

	/**?*/
	@Excel(name = "?", width = 15)
    @Schema(description = "?")
    private String unid;

	/**?*/
	@Excel(name = "?", width = 15)
    @Schema(description = "?")
    private Integer idel;

	/**?*/
	@Excel(name = "?", width = 15)
    @Schema(description = "?")
    private String dmodt;

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

    @TableField(exist = false)
    private String yyyyMMdd;

    /**
     * 周范围 (w_年_周数)
     */
    @TableField(exist = false)
    private String weekRange;

    /**
     * 保留小数位数
     */
    @TableField(exist = false)
    private Integer decimalNum;

    /**
     * 所属年
     */
    @TableField(exist = false)
    private Integer year;

    private static final long serialVersionUID = 1L;
}
