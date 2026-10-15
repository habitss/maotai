package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: PI标准比对数据存储
 * @Author: aolei
 * @Date:   2024-12-16
 * @Version: V1.0
 */

@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_standard_data")
@Schema(description="PI标准比对数据存储")
public class PiStandardData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;
	/**工厂*/
    @Schema(description="工厂")
    private String factoryCode;
	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description="部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;
	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description="产线")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;


	/**数据存储表名*/
	@Excel(name = "数据存储表名", width = 15)
    @Schema(description="数据存储表名")
    private String tableName;

	/**数据主键*/
	@Excel(name = "数据主键", width = 15)
    @Schema(description="数据主键")
    private String dataId;

	/**数据pi字段的红黄区*/
	@Excel(name = "数据pi字段的红黄区", width = 15)
    @Schema(description="数据pi字段的红黄区")
    private String standardJson;

    /**数据标准id*/
    @Excel(name = "数据标准id", width = 15)
    @Schema(description="数据标准id")
    private String manageId;


    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description="排序")
    private Integer sort;
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
}
