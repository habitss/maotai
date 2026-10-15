package com.keytech.modules.pi.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: DF算法-唯一约束
 * @Author: jeecg-boot
 * @Date:   2026-01-20
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_df_algorithm_unique")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF算法-唯一约束")
public class PiDfAlgorithmUnique implements Serializable {


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

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**基础信息表id*/
	@Excel(name = "基础信息表id", width = 15)
    @Schema(description = "基础信息表id")
    private String dataId;

    /**df表名*/
    @Excel(name = "df表名", width = 15)
    @Schema(description = "df表名")
    private String dfTableName;

    /**df表字段*/
    @Excel(name = "df表字段", width = 15)
    @Schema(description = "df表字段")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dfFieldName;

    /**pi表名*/
    @Excel(name = "pi表名", width = 15)
    @Schema(description = "pi表名")
    private String piTableName;

    /**pi表字段*/
    @Excel(name = "pi表字段", width = 15)
    @Schema(description = "pi表字段")
    private String piFieldName;

    /**自定义内容*/
    @Excel(name = "自定义内容", width = 15)
    @Schema(description = "自定义内容")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String inputText;

    /**自定义时间*/
    @Excel(name = "自定义时间", width = 15)
    @Schema(description = "自定义时间")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dateType;

    @Serial
    private static final long serialVersionUID = 1L;
}
