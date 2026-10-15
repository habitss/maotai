package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

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
 * @Description: PI-数据抽取配置主表
 * @Author: HQ
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_extract_table")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI-数据抽取配置主表")
public class PiExtractTable implements Serializable {


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

    /**源-库名称*/
    @Excel(name = "源-库名称", width = 15)
    @Schema(description = "源-库名称")
    private String dataSourceName;

	/**源-表名*/
	@Excel(name = "源-表名", width = 15)
    @Schema(description = "源-表名")
    private String sourceTable;

	/**源-表名称(中文)*/
	@Excel(name = "源-表名称(中文)", width = 15)
    @Schema(description = "源-表名称(中文)")
    private String sourceTableName;

	/**目标-表名*/
	@Excel(name = "目标-表名", width = 15)
    @Schema(description = "目标-表名")
    private String targetTable;

	/**目标-表名称(中文)*/
	@Excel(name = "目标-表名称(中文)", width = 15)
    @Schema(description = "目标-表名称(中文)")
    private String targetTableName;

	/**自定义报表主表id*/
	@Excel(name = "自定义报表主表id", width = 15)
    @Schema(description = "自定义报表主表id")
    private Integer tableId;

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

    /**字段集合*/
    @TableField(exist = false)
    private List<PiExtractTableField> fields;
}
