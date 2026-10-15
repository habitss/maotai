package com.keytech.modules.pi.entity;

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
 * @Description: PI异常分析-异常点位记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_abnormal_analysis_point_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI异常分析-异常点位记录表")
public class PiAbnormalAnalysisPointRecord implements Serializable {


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

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**异常分析表id*/
	@Excel(name = "异常分析表id", width = 15)
    @Schema(description = "异常分析表id")
    private String recordId;

    /**点位配置表id*/
    @Excel(name = "点位配置表id", width = 15)
    @Schema(description = "点位配置表id")
    private String pointId;

    /**点位配置名称*/
    @Excel(name = "点位配置名称", width = 15)
    @Schema(description = "点位配置名称")
    private String pointName;

	/**标准值*/
	@Excel(name = "标准值", width = 15)
    @Schema(description = "标准值")
    private String standardValue;

	/**实际值*/
	@Excel(name = "实际值", width = 15)
    @Schema(description = "实际值")
    private String actualValue;

	/**原因分析*/
	@Excel(name = "原因分析", width = 15)
    @Schema(description = "原因分析")
    private String reasonAnalysis;

	/**pi报表code*/
	@Excel(name = "pi报表code", width = 15)
    @Schema(description = "pi报表code")
    private String tableCode;

	/**pi报表名称*/
	@Excel(name = "pi报表名称", width = 15)
    @Schema(description = "pi报表名称")
    private String tableName;

	/**pi列名code*/
	@Excel(name = "pi列名code", width = 15)
    @Schema(description = "pi列名code")
    private String fieldCode;

	/**pi列名*/
	@Excel(name = "pi列名", width = 15)
    @Schema(description = "pi列名")
    private String fieldName;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

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
}
