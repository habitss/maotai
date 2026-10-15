package com.keytech.modules.pi.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: DF算法-基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_df_algorithm_baseinfo")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF算法-基础信息")
public class PiDfAlgorithmBaseinfo implements Serializable {
	/**id*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private String id;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**是否删除1：是，0：否*/
	@Excel(name = "是否删除1：是，0：否", width = 15)
    @Schema(description = "是否删除1：是，0：否")
    @TableLogic
    private Integer delFlag;

	/**pi基础表id*/
	@Excel(name = "pi基础表id", width = 15)
    @Schema(description = "pi基础表id")
    private String piTableId;

	/**pi基础表名*/
	@Excel(name = "pi基础表名", width = 15)
    @Schema(description = "pi基础表名")
    private String piTableName;

	/**pi基础表字段id*/
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name", dicCode = "id")
	@Excel(name = "pi基础表字段id", width = 15)
    @Schema(description = "pi基础表字段id")
    private String piFieldId;

	/**pi基础表字段名*/
	@Excel(name = "pi基础表字段名", width = 15)
    @Schema(description = "pi基础表字段名")
    private String piFieldName;

	/**数据表名*/
    @Dict(dictTable ="mes3_mdm_df_table_info",dicText = "df_table_desc", dicCode = "df_table_name")
	@Excel(name = "数据表名", width = 15)
    @Schema(description = "数据表名")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dataTableName;

    /**数据表时间查询字段*/
    @Excel(name = "数据表时间查询字段", width = 15)
    @Schema(description = "数据表时间查询字段")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dataFieldName;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String factoryCode;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String deptCode;

	/**产线code*/
	@Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

    /**租户ID*/
    @Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

    /**指标标准*/
    @Dict(dictTable ="mes3_mdm_df_ind_stand",dicText = "ind_name", dicCode = "ind_stand_code")
    @Excel(name = "指标标准", width = 15)
    @Schema(description = "指标标准")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String indStand;

    /**实例化指标*/
    @Dict(dictTable ="mes3_mdm_df_indicator",dicText = "funloc_desc", dicCode = "inst_use_code")
    @Excel(name = "实例化指标", width = 15)
    @Schema(description = "实例化指标")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String indicator;

    /**实例化指标字段*/
    @Excel(name = "实例化指标字段", width = 15)
    @Schema(description = "实例化指标字段")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String indicatorFieldName;

    /**工厂查询字段*/
    @Excel(name = "工厂查询字段", width = 15)
    @Schema(description = "工厂查询字段")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String factoryField;

    @Serial
    private static final long serialVersionUID = 1L;
}
