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
 * @Description: pi重复异常类型配置
 * @Author: jeecg-boot
 * @Date:   2025-12-03
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_repeat_exception_type")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="pi重复异常类型配置")
public class PiRepeatExceptionType implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
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

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**是否删除：1是、0否*/
	@Excel(name = "是否删除：1是、0否", width = 15)
    @Schema(description = "是否删除：1是、0否")
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

	/**pi报表Id*/
	@Excel(name = "pi报表Id", width = 15)
    @Schema(description = "pi报表Id")
    private String piTableId;

	/**pi报表实际表名*/
	@Excel(name = "pi报表实际表名", width = 15)
    @Schema(description = "pi报表实际表名")
    private String piTableCode;

	/**pi字段id*/
	@Excel(name = "pi字段id", width = 15)
    @Schema(description = "pi字段id")
    private String piFieldId;

	/**pi字段实际字段名*/
	@Excel(name = "pi字段实际字段名", width = 15)
    @Schema(description = "pi字段实际字段名")
    private String piFieldCode;

	/**重复异常名称*/
	@Excel(name = "重复异常名称", width = 15)
    @Schema(description = "重复异常名称")
    private String exceptionName;

    private static final long serialVersionUID = 1L;
}
