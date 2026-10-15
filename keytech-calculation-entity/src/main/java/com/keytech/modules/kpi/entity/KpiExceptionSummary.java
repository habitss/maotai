package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.common.DataBase;
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
 * @Description: KPI异常清单
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_exception_summary")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI异常清单")
public class KpiExceptionSummary extends DataBase implements Serializable {

    private static final long serialVersionUID = 1L;


	/**标识id*/
	@Excel(name = "标识id", width = 15)
    @Schema(description = "标识id")
    private String identify;

	/**0:周  1:月*/
	@Excel(name = "0:周  1:月", width = 15)
    @Schema(description = "0:周  1:月")
    private Integer identifyType;

	/**所属周/月/年*/
	@Excel(name = "所属周/月/年", width = 15)
    @Schema(description = "所属周/月/年")
    private String dateIdentify;

	/**kpiCode*/
	@Excel(name = "kpiCode", width = 15)
    @Schema(description = "kpiCode")
    private String kpiCode;

	/**kpiName*/
	@Excel(name = "kpiName", width = 15)
    @Schema(description = "kpiName")
    private String kpiName;

	/**分类*/
	@Excel(name = "分类", width = 15)
    @Schema(description = "分类")
    private String kpiType;

	/**异常情况描述*/
	@Excel(name = "异常情况描述", width = 15)
    @Schema(description = "异常情况描述")
    private String anomalyDescription;

	/**红黄绿 结果 1 green 2:red 3:yellow*/
	@Excel(name = "红黄绿 结果 1 green 2:red 3:yellow", width = 15)
    @Schema(description = "红黄绿 结果 1 green 2:red 3:yellow")
    private Integer color;

	/**5why tile*/
	@Excel(name = "5why tile", width = 15)
    @Schema(description = "5why tile")
    private String whyTitle;

    /**年份*/
    @Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private Integer year;

    /**年份*/
    @Schema(description = "kpi列标识")
    private transient String kpiColKey;

    //原因
    private transient String reason;
    //行动
    private transient String action;
    private transient List<String> actionList;

    //刷选  开始时间
    private transient String startTime;
    //筛选  结束时间
    private transient String endTime;
    //车间
    private transient String hierarchy;
    //筛选 1:分析5w  0:差距分析
    private transient Boolean type = true;
    //Kpi 数据title
    private transient Boolean isAllType = false;

    /**
     * 5why
     */
    private transient Boolean fiveType= false;
    /**
     * 差距分析
     */
    private transient Boolean diffType= false;

    /**
     * 部门
     */
    @TableField(exist = false)
    private String queryDept;


    /**部门经理抽查-是否抽查(1是/0否)*/
    @Excel(name = "部门经理抽查-是否抽查", width = 15)
    @Schema(description = "部门经理抽查-是否抽查")
    private Integer managerCheck;

    /**部门经理抽查-是否合格(1是/0否)*/
    @Excel(name = "部门经理抽查-是否合格", width = 15)
    @Schema(description = "部门经理抽查-是否合格")
    private Integer managerQualified;

    /**企管部抽查-是否抽查(1是/0否)*/
    @Excel(name = "企管部抽查-是否抽查", width = 15)
    @Schema(description = "企管部抽查-是否抽查")
    private Integer controlCheck;

    /**企管部抽查-是否合格(1是/0否)*/
    @Excel(name = "企管部抽查-是否合格", width = 15)
    @Schema(description = "企管部抽查-是否合格")
    private Integer controlQualified;
}
