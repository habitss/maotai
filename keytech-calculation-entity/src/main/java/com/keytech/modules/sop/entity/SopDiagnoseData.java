package com.keytech.modules.sop.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
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
 * @Description: SOP诊断计划-工作诊断
 * @Author: hq
 * @Date:   2025-04-26
 * @Version: V1.0
 */
@Data
@TableName("mes3_sop_diagnose_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="SOP诊断计划-工作诊断")
public class SopDiagnoseData implements Serializable {


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

	/**诊断计划id*/
	@Excel(name = "诊断计划id", width = 15)
    @Schema(description = "诊断计划id")
    private String sopStatisticId;

	/**a1*/
	@Excel(name = "a1", width = 15)
    @Schema(description = "a1")
    private String a1;

	/**a2*/
	@Excel(name = "a2", width = 15)
    @Schema(description = "a2")
    private String a2;

	/**a3*/
	@Excel(name = "a3", width = 15)
    @Schema(description = "a3")
    private String a3;

	/**a4*/
	@Excel(name = "a4", width = 15)
    @Schema(description = "a4")
    private String a4;

	/**a5*/
	@Excel(name = "a5", width = 15)
    @Schema(description = "a5")
    private String a5;

	/**b1*/
	@Excel(name = "b1", width = 15)
    @Schema(description = "b1")
    private String b1;

	/**b2*/
	@Excel(name = "b2", width = 15)
    @Schema(description = "b2")
    private String b2;

	/**b3*/
	@Excel(name = "b3", width = 15)
    @Schema(description = "b3")
    private String b3;

	/**b4*/
	@Excel(name = "b4", width = 15)
    @Schema(description = "b4")
    private String b4;

	/**b5*/
	@Excel(name = "b5", width = 15)
    @Schema(description = "b5")
    private String b5;

	/**b6*/
	@Excel(name = "b6", width = 15)
    @Schema(description = "b6")
    private String b6;

	/**b7*/
	@Excel(name = "b7", width = 15)
    @Schema(description = "b7")
    private String b7;

	/**b8*/
	@Excel(name = "b8", width = 15)
    @Schema(description = "b8")
    private String b8;

	/**b9*/
	@Excel(name = "b9", width = 15)
    @Schema(description = "b9")
    private String b9;

	/**b10*/
	@Excel(name = "b10", width = 15)
    @Schema(description = "b10")
    private String b10;

	/**b11*/
	@Excel(name = "b11", width = 15)
    @Schema(description = "b11")
    private String b11;

	/**b12*/
	@Excel(name = "b12", width = 15)
    @Schema(description = "b12")
    private String b12;

	/**b13*/
	@Excel(name = "b13", width = 15)
    @Schema(description = "b13")
    private String b13;

	/**b14*/
	@Excel(name = "b14", width = 15)
    @Schema(description = "b14")
    private String b14;

	/**b15*/
	@Excel(name = "b15", width = 15)
    @Schema(description = "b15")
    private String b15;

	/**b16*/
	@Excel(name = "b16", width = 15)
    @Schema(description = "b16")
    private String b16;

	/**b17*/
	@Excel(name = "b17", width = 15)
    @Schema(description = "b17")
    private String b17;

	/**b18*/
	@Excel(name = "b18", width = 15)
    @Schema(description = "b18")
    private String b18;

	/**b19*/
	@Excel(name = "b19", width = 15)
    @Schema(description = "b19")
    private String b19;

	/**b20*/
	@Excel(name = "b20", width = 15)
    @Schema(description = "b20")
    private String b20;

	/**b21*/
	@Excel(name = "b21", width = 15)
    @Schema(description = "b21")
    private String b21;

	/**b22*/
	@Excel(name = "b22", width = 15)
    @Schema(description = "b22")
    private String b22;

	/**b23*/
	@Excel(name = "b23", width = 15)
    @Schema(description = "b23")
    private String b23;

	/**b24*/
	@Excel(name = "b24", width = 15)
    @Schema(description = "b24")
    private String b24;

	/**b25*/
	@Excel(name = "b25", width = 15)
    @Schema(description = "b25")
    private String b25;

	/**cData*/
	@Excel(name = "cData", width = 15)
    @Schema(description = "cData")
    private String cData;

	/**d1*/
	@Excel(name = "d1", width = 15)
    @Schema(description = "d1")
    private String d1;

	/**d2*/
	@Excel(name = "d2", width = 15)
    @Schema(description = "d2")
    private String d2;

	/**SOP清单id*/
	@Excel(name = "SOP清单id", width = 15)
    @Schema(description = "SOP清单id")
    private String sopid;

	/**诊断表年份*/
	@Excel(name = "诊断表年份", width = 15)
    @Schema(description = "诊断表年份")
    private Integer year;

	/**诊断表周数*/
	@Excel(name = "诊断表周数", width = 15)
    @Schema(description = "诊断表周数")
    private String week;

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

    @TableField(exist = false)
    private List<ActionPlanTrack> cDataList;
}
