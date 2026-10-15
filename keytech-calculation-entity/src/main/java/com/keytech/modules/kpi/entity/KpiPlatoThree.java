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
 * @Description: 三级柏拉图(PI点)
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_plato_three")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="三级柏拉图(PI点)")
public class KpiPlatoThree implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
    @Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
    @Schema(description = "产线Code")
    private String lineCode;

	/**code*/
    @Schema(description = "code")
    private String code;

	/**pi_code*/
    @Schema(description = "pi_code")
    private String piCode;

    /**二级柏拉图上级id*/
    @Excel(name = "控制点名称", width = 15, dictTable ="mes3_kpi_plato_two",dicText = "controller_name",dicCode = "code")
    @Schema(description = "上级id")
    @Dict(dictTable = "mes3_kpi_plato_two", dicCode = "code", dicText = "controller_name")
    private String parentCode;

	/**pi名称*/
//	@Excel(name = "产线-设备-PI名称", width = 15)
    @Schema(description = "pi名称")
    private String piName;


	/**顶级id kpi code */
    @Dict(dictTable = "mes3_kpi_info", dicCode = "kpi_code", dicText = "kpi_name")
    @Schema(description = "顶级id kpi code")
    private String superCode;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remarks;

	/**控制活动*/
	@Excel(name = "控制活动", width = 15)
    @Schema(description = "控制活动")
    private String controlActivity;

	/**标准*/
	@Excel(name = "标准", width = 15)
    @Schema(description = "标准")
    private String standard;

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

    /**所属年份*/
//    @Excel(name = "所属年份", width = 15)
    @Schema(description = "所属年份")
    private String year;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
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

    /**PI报表id(customize)*/
    @Schema(description = "PI报表id")
    private String piTableId;

    private static final long serialVersionUID = 1L;
}
