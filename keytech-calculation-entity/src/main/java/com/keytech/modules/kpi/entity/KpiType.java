package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: KPI分类
 * @Author: HQ
 * @Date:   2025-02-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_type")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI分类")
public class KpiType implements Serializable {

    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

	/**kpi分类名*/
	@Excel(name = "kpi分类名", width = 15)
    @Schema(description = "kpi分类名")
    private String kpiType;

	/**父编号*/
//	@Excel(name = "父编号", width = 15)
//    @Schema(description = "父编号")
//    private Integer perentId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String text;

    /**所属年份*/
//    @Excel(name = "所属年份", width = 15)
    @Schema(description = "所属年份")
    private String year;

	/**父级*/
	@Excel(name = "父级", width = 15)
    @Schema(description = "父级")
    private String parentId;

//    /**是否有子节点*/
//    @Excel(name = "是否有子节点", width = 15, replace = { "是_1", "否_0" })
//    @Schema(description = "是否有子节点")
//    private String hasChild;


    /**删除标识*/
    @Schema(description = "删除标识")
    @TableLogic
    private String delFlag;
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;
    /**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;
    /**更新人*/
    @Schema(description = "更新人")
    private String updateBy;


    @TableField(exist = false)
    private List<KpiType> children = new ArrayList<>();

}
