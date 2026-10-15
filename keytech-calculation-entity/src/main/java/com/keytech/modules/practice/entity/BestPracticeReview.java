package com.keytech.modules.practice.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 最佳实践评审
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_review")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践评审")
public class BestPracticeReview implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**填报ID*/
	@Excel(name = "填报ID", width = 15)
    @Schema(description = "填报ID")
    private String fillId;

	/**fillType*/
	@Excel(name = "fillType", width = 15)
    @Schema(description = "fillType")
    private Integer fillType;

	/**评审人*/
	@Excel(name = "评审人", width = 15)
    @Schema(description = "评审人")
    private String assessor;

	/**评审人岗位*/
	@Excel(name = "评审人岗位", width = 15)
    @Schema(description = "评审人岗位")
    private String assessorPosition;

	/**红线指标*/
	@Excel(name = "红线指标", width = 15)
    @Schema(description = "红线指标")
    private Integer redType;

	/**过程数据*/
	@Excel(name = "过程数据", width = 15)
    @Schema(description = "过程数据")
    private String processData;

	/**汇总值*/
	@Excel(name = "汇总值", width = 15)
    @Schema(description = "汇总值")
    private Integer collectValue;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注说明*/
	@Excel(name = "备注说明", width = 15)
    @Schema(description = "备注说明")
    private String remark;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

    /**评审表id*/
    @Schema(description = "评审表id")
    @TableField(exist = false)
    private String reportId;

    private static final long serialVersionUID = 1L;
}
