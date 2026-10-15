package com.keytech.modules.cpk.entity;

import java.io.Serializable;
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
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Description: CPK配置
 * @Author: jeecg-boot
 * @Date:   2025-12-11
 * @Version: V1.0
 */
@Data
@TableName("mes3_cpk_config")
@Schema(description="CPK配置")
public class CpkConfig implements Serializable {
    private static final long serialVersionUID = 1L;

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
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
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
    private java.util.Date createTime;
	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private java.util.Date updateTime;
	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;
    /**节点名称*/
    @Excel(name = "节点名称", width = 15)
    @Schema(description = "节点名称")
    private String nodeName;
    /**节点层级*/
    @Excel(name = "节点层级", width = 15)
    @Schema(description = "节点层级")
    private Integer nodeLevel;
	/**节点编码*/
	@Excel(name = "节点编码", width = 15)
    @Schema(description = "节点编码")
    private String nodeCode;
	/**父节点id*/
	@Excel(name = "父节点id", width = 15)
    @Schema(description = "父节点id")
    private String parentId;
	/**父节点编码*/
	@Excel(name = "父节点编码", width = 15)
    @Schema(description = "父节点编码")
    private String parentCode;
	/**是否有子节点*/
	@Excel(name = "是否有子节点", width = 15)
    @Schema(description = "是否有子节点")
    private String hasChild;
    /**父级节点*/
    @Excel(name = "父级节点", width = 15)
    @Schema(description = "父级节点")
    private String pid;

    /**报表实际名称*/
    @Excel(name = "报表实际名称", width = 15)
    @Schema(description = "报表实际名称")
    private String tableCode;

    /**字段实际名称*/
    @Excel(name = "字段实际名称", width = 15)
    @Schema(description = "字段实际名称")
    private String fieldCode;

    /**上限*/
    @Excel(name = "上限", width = 15)
    @Schema(description = "上限")
    private String upperLimit;

    /**下限*/
    @Excel(name = "下限", width = 15)
    @Schema(description = "下限")
    private String lowerLimit;

    /**计算时长(天)*/
    @Excel(name = "计算时长(天)", width = 15)
    @Schema(description = "计算时长(天)")
    private String calcTime;

    /**生效日期*/
    @Excel(name = "生效日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效日期")
    private Date effectiveDate;

    /**评价维度*/
    @Excel(name = "评价维度", width = 15)
    @Schema(description = "评价维度")
    private String evaluation;

    /**目标值*/
    @Excel(name = "目标值", width = 15)
    @Schema(description = "目标值")
    private String targetValue;
    /**子节点*/
    @TableField(exist = false)
    @Schema(description = "子节点")
    private List<CpkConfig> children;
    /**节点层级*/
    @TableField(exist = false)
    @Schema(description = "节点层级")
    private Integer level;
    /**详细配置*/
    @TableField(exist = false)
    @Schema(description = "详细配置")
    private List<CpkConfigDetail> detailList;
}
