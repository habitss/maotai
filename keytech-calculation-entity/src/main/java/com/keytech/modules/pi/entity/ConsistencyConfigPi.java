package com.keytech.modules.pi.entity;

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
 * @Description: 一致性指数配置PI表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Data
@TableName("mes3_consistency_config_pi")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="一致性指数配置PI表")
public class ConsistencyConfigPi implements Serializable {


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

	/**明细表ID*/
	@Excel(name = "明细表ID", width = 15)
    @Schema(description = "明细表ID")
    private String mxId;

    /**字段英文名称*/
    @Excel(name = "字段英文名称Id", width = 15)
    @Schema(description = "字段英文名称Id")
    @Dict(dictTable ="mes3_pi_customize_table_field", dicText = "name", dicCode = "id")
    private String fieldCodeId;

    /**字段英文名称*/
    @Excel(name = "字段英文名称", width = 15)
    @Schema(description = "字段英文名称")
    private String fieldCode;

    /**基础数据表英文名称*/
    @Excel(name = "基础数据表英文名称", width = 15)
    @Dict(dictTable ="mes3_pi_customize_table", dicText = "name", dicCode = "table_name")
    @Schema(description = "基础数据表英文名称")
    private String tableCode;

    /**查询字段英文名称*/
    @Excel(name = "查询字段英文名称Id", width = 15)
    @Dict(dictTable ="mes3_pi_customize_table_field", dicText = "name", dicCode = "id")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String queryFieldId;

    /**查询字段英文名称*/
    @Excel(name = "查询字段英文名称", width = 15)
    @Schema(description = "查询字段英文名称")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String queryField;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
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
