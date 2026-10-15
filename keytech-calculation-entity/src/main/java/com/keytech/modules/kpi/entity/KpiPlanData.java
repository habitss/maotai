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
 * @Description: KPI树-基础数据
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_plan_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI树-基础数据")
public class KpiPlanData implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Integer id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

    /**工厂*/
    @Schema(description = "工厂")
    private String factoryCode;

    /**部门*/
    @Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门")
    private String deptCode;

    /**产线*/
    @Schema(description = "产线")
    private String lineCode;

	/**三级柏拉图主键*/
	@Excel(name = "三级柏拉图主键", width = 15)
    @Schema(description = "三级柏拉图主键")
    private String threeId;

	/**排班类别*/
	@Excel(name = "排班类别", width = 15)
    @Schema(description = "排班类别")
    private String classTitle;

	/**日期*/
	@Excel(name = "日期", width = 15)
    @Schema(description = "日期")
    private String timeDay;

	/**统计规则*/
	@Excel(name = "统计规则", width = 15)
    @Schema(description = "统计规则")
    private String planName;

	/**统计值*/
	@Excel(name = "统计值", width = 15)
    @Schema(description = "统计值")
    private String planValues;


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



    private static final long serialVersionUID = 1L;
}
