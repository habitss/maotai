package com.keytech.modules.kpi.entity;

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
 * @Description: 一级柏拉图（生产流程）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_plato_one")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="一级柏拉图（生产流程）")
public class KpiPlatoOne implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "工厂")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线*/
    @Schema(description = "产线")
    private String lineCode;

	/**Code*/
    @Schema(description = "Code")
    private String code;

	/**流程名称*/
	@Excel(name = "流程名称", width = 15)
    @Schema(description = "流程名称")
    private String processName;

	/**KPI上级code 关联kpiCode*/
	@Excel(name = "kpi名称", width = 15 ,dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    @Dict(dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    @Schema(description = "上级code 关联kpiCode")
    private String parentCode;

	/**kpi_code*/
    @Schema(description = "kpi_code")
    private String superCode;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remarks;

    /**所属年份*/
//    @Excel(name = "所属年份", width = 15)
    @Schema(description = "所属年份")
    private String year;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;

    private static final long serialVersionUID = 1L;
}
