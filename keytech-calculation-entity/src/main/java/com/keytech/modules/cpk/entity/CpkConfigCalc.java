package com.keytech.modules.cpk.entity;

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
 * @Description: CPK计算结果
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_cpk_config_calc")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="CPK计算结果")
public class CpkConfigCalc implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户id*/
	@Excel(name = "租户id", width = 15)
    @Schema(description = "租户id")
    private Integer tenantId;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factoryCode;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String deptCode;

	/**产线code*/
	@Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

	/**是否删除*/
	@Excel(name = "是否删除", width = 15)
    @Schema(description = "是否删除")
    @TableLogic
    private Integer delFlag;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**主配置id*/
	@Excel(name = "主配置id", width = 15)
    @Schema(description = "主配置id")
    private String configId;

    /**年*/
    @Excel(name = "年", width = 15)
    @Schema(description = "年")
    private String calcYear;

    /**月*/
    @Excel(name = "月", width = 15)
    @Schema(description = "月")
    private String calcMonth;

    /**记录数*/
    @Excel(name = "记录数", width = 15)
    @Schema(description = "记录数")
    private String recordNum;

    /**记录和*/
    @Excel(name = "记录和", width = 15)
    @Schema(description = "记录和")
    private String recordSum;

    /**记录平均值*/
    @Excel(name = "记录平均值", width = 15)
    @Schema(description = "记录平均值")
    private String recordMean;

	/**cp值*/
	@Excel(name = "cp值", width = 15)
    @Schema(description = "cp值")
    private String cp;

	/**cpk值*/
	@Excel(name = "cpk值", width = 15)
    @Schema(description = "cpk值")
    private String cpk;

	/**cpk上限值*/
	@Excel(name = "cpk上限值", width = 15)
    @Schema(description = "cpk上限值")
    private String cpku;

	/**cpk下限值*/
	@Excel(name = "cpk下限值", width = 15)
    @Schema(description = "cpk下限值")
    private String cpkl;

    private static final long serialVersionUID = 1L;
}
