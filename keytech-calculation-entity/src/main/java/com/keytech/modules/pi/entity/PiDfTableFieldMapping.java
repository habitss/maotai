package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: PI字段与DF字段映射
 * @Author: jeecg-boot
 * @Date:   2026-01-15
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_df_table_field_mapping")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI字段与DF字段映射")
public class PiDfTableFieldMapping implements Serializable {


	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂编码*/
	@Excel(name = "工厂编码", width = 15)
    @Schema(description = "工厂编码")
    private String factoryCode;

	/**部门编码*/
	@Excel(name = "部门编码", width = 15)
    @Schema(description = "部门编码")
    private String deptCode;

	/**产线编码*/
	@Excel(name = "产线编码", width = 15)
    @Schema(description = "产线编码")
    private String lineCode;

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

	/**数据库表id*/
	@Excel(name = "数据库表id", width = 15)
    @Schema(description = "数据库表id")
    private String tableId;

	/**数据库表名*/
	@Excel(name = "数据库表名", width = 15)
    @Schema(description = "数据库表名")
    private String tableName;

	/**表字段id*/
	@Excel(name = "表字段id", width = 15)
    @Schema(description = "表字段id")
    private String fieldId;

	/**表字段名*/
	@Excel(name = "表字段名", width = 15)
    @Schema(description = "表字段名")
    private String fieldName;

	/**df数据库表id*/
	@Excel(name = "df数据库表id", width = 15)
    @Schema(description = "df数据库表id")
    private String dfTableId;

	/**df数据库表名*/
	@Excel(name = "df数据库表名", width = 15)
    @Schema(description = "df数据库表名")
    private String dfTableName;

	/**df表字段id*/
	@Excel(name = "df表字段id", width = 15)
    @Schema(description = "df表字段id")
    private String dfFieldId;

	/**df表字段名*/
	@Excel(name = "df表字段名", width = 15)
    @Schema(description = "df表字段名")
    private String dfFieldName;

    private static final long serialVersionUID = 1L;
}
