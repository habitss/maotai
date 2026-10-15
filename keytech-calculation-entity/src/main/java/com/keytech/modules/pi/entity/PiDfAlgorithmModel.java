package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: DF算法-计算模型
 * @Author: jeecg-boot
 * @Date:   2026-01-21
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_df_algorithm_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF算法-计算模型")
public class PiDfAlgorithmModel implements Serializable {


	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private String id;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factory;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String dept;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

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
    private String dfFieldName;

	/**pi表名*/
	@Excel(name = "pi表名", width = 15)
    @Schema(description = "pi表名")
    private String piTableName;

	/**pi字段名*/
	@Excel(name = "pi字段名", width = 15)
    @Schema(description = "pi字段名")
    private String piFieldName;

	/**是否覆盖 0-否，1-是*/
	@Excel(name = "是否覆盖 0-否，1-是", width = 15)
    @Schema(description = "是否覆盖 0-否，1-是")
    private String ifCover;

	/**是否汉化 0-不汉化，1-字典表，2-主数据表*/
	@Excel(name = "是否汉化 0-不汉化，1-字典表，2-主数据表", width = 15)
    @Schema(description = "是否汉化 0-不汉化，1-字典表，2-主数据表")
    private String ifSinicized;

    /**汉化code-汉化方式为1时存储为字典code，为2时存储的主数据表名*/
    @Excel(name = "汉化方式为1时存储为字典code，为2时存储的主数据表名", width = 15)
    @Schema(description = "汉化方式为1时存储为字典code，为2时存储的主数据表名")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sinicizedDic;

    /**汉化主键字段-汉化方式为主数据表时才使用*/
    @Excel(name = "汉化主键字段-汉化方式为主数据表时才使用", width = 15)
    @Schema(description = "汉化主键字段-汉化方式为主数据表时才使用")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sinicizedKey;

    /**汉化取值字段-汉化方式为主数据表时才使用*/
    @Excel(name = "汉化取值字段-汉化方式为主数据表时才使用", width = 15)
    @Schema(description = "汉化取值字段-汉化方式为主数据表时才使用")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sinicizedValue;

    @Schema(description="结果类型")
    @TableField(value = "result_type", updateStrategy = FieldStrategy.ALWAYS)
    @Excel(name = "结果类型", width = 15)
    private String resultType;

    @Serial
    private static final long serialVersionUID = 1L;
}
