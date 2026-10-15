package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: 二级柏拉图（控制点）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_plato_two")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="二级柏拉图（控制点）")
public class KpiPlatoTwo implements Serializable {

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

    /**工厂*/
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description="工厂")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;
    /**部门*/
    @Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description="部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    /**产线*/
    @Schema(description="产线")
    private java.lang.String lineCode;

    /**code*/
    @Schema(description = "code")
    private String code;

	/**控制点名称*/
	@Excel(name = "控制点名称", width = 15)
    @Schema(description = "控制点名称")
    private String controllerName;


	/**上级code*/
	@Excel(name = "生产流程名称", width = 15, dictTable ="mes3_kpi_plato_one",dicText = "process_name",dicCode = "code")
    @Schema(description = "上级code")
    @Dict(dictTable = "mes3_kpi_plato_one", dicCode = "code", dicText = "process_name")
    private String parentCode;

	/**kpi_code*/
    @Schema(description = "kpi_code")
    @Dict(dictTable = "mes3_kpi_info", dicCode = "kpi_code", dicText = "kpi_name")
    private String superCode;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remarks;

    /**所属年份*/
//    @Excel(name = "所属年份", width = 15)
    @Schema(description = "所属年份")
    private String year;

    private static final long serialVersionUID = 1L;

    /**排序*/
    @Schema(description="排序")
    private Double sort;
    /**删除状态(0未删,1已删)*/
    @Schema(description="删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="创建时间")
    private Date createTime;
    /**修改时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description="修改时间")
    private Date updateTime;
    /**创建人*/
    @Schema(description="创建人")
    private String createBy;
    /**修改人*/
    @Schema(description="修改人")
    private String updateBy;
}
