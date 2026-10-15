package com.keytech.modules.df.mdm.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: DF数据库表基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
@Data
@TableName("mes3_mdm_df_table_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="DF数据库表基础信息")
public class DfTableInfo implements Serializable {


	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂编码*/
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "工厂编码", width = 15)
    @Schema(description = "工厂编码")
    private String factoryCode;

	/**部门编码*/
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "部门编码", width = 15)
    @Schema(description = "部门编码")
    private String deptCode;

	/**产线编码*/
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
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

	/**DF数据库表名*/
	@Excel(name = "DF数据库表名", width = 15)
    @Schema(description = "DF数据库表名")
    private String dfTableName;

	/**DF数据库表描述*/
	@Excel(name = "DF数据库表描述", width = 15)
    @Schema(description = "DF数据库表描述")
    private String dfTableDesc;

    private static final long serialVersionUID = 1L;
}
