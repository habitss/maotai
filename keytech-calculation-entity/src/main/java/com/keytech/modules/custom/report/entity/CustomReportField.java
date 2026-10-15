package com.keytech.modules.custom.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_custom_report_field")
@Schema(description="pi自定义报表字段")
public class CustomReportField implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
    @Excel(name = "id", width = 15)
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @TableField("factory_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门Code")
    @TableField("dept_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

    @Schema(description="产线Code")
    @TableField("line_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

	/**自定义表id*/
	@Excel(name = "自定义表id", width = 15)
    @Schema(description="自定义表id")
    private String tableId;

    /**自定义表表名*/
    @Excel(name = "自定义表表名", width = 15)
    @Schema(description="自定义表表名")
    private String tableName;

    /**字段名*/
    @Excel(name = "字段名", width = 15)
    @Schema(description="字段名")
    private String fieldName;

    /**字段中文名称*/
    @Excel(name = "字段中文名称", width = 15)
    @Schema(description="字段中文名称")
    private String name;

    /**字段类型*/
    @Excel(name = "字段类型", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_FIELD_TYPE'",dicText = "item_key",dicCode = "item_val")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_FIELD_TYPE'",dicText = "item_key",dicCode = "item_val")
    @Schema(description="字段类型")
    private String type;

    /**字段长度*/
    @Excel(name = "字段长度", width = 15)
    @Schema(description="字段长度")
    private Integer fieldLength;

    /**是否列表显示(0否/1是)*/
    @Excel(name = "是否列表显示", width = 15, replace = { "是_1", "否_0" })
    @Schema(description="是否列表显示(0否/1是)")
    private Integer isList;

    /**是否排序(0否/1是)*/
    @Excel(name = "是否排序", width = 15, replace = { "是_1", "否_0" })
    @Schema(description="是否排序(0否/1是)")
    private Integer isSort;

    /**排序方式(0降序/1升序)*/
    @Excel(name = "排序方式", width = 15, replace = { "升序_1", "降序_0" })
    @Schema(description="排序方式(0降序/1升序)")
    private Integer sortType;

    /**是否查询(0否/1是)*/
    @Excel(name = "是否查询", width = 15, replace = { "是_1", "否_0" })
    @Schema(description="是否查询(0否/1是)")
    private Integer isQuery;

	/**字段备注*/
	@Excel(name = "字段备注", width = 15)
    @Schema(description="字段备注")
    private String notes;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description="排序")
    private Double sort;

	/**小数点*/
    @Schema(description="小数点")
    private Integer decimalPoint;

	/**默认值*/
    @Schema(description="默认值")
    private String fieldDefault;

	/**是否表单显示(0否/1是)*/
    @Schema(description="是否表单显示(0否/1是)")
    private Integer isForm;

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

    /**是否唯一约束(0否1是)*/
    @Schema(description="是否唯一约束")
    private Integer isUnique;

    /**保留几位小数*/
    @Schema(description="保留几位小数")
    private Integer keepDecimals;

    /**取值方式*/
    @Schema(description="取值方式")
    private String takingValue;

    //字段类型
    @TableField(exist = false)
    private String fieldType;
}
