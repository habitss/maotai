package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
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
 * @Description: 差距分析
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_gap_analysis")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="差距分析")
public class GapAnalysis implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
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

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**kpi记录单元格id*/
	@Excel(name = "kpi记录单元格id", width = 15)
    @Schema(description = "kpi记录单元格id")
    private String recordId;

	/**kpi编码*/
	@Excel(name = "kpi编码", width = 15)
    @Schema(description = "kpi编码")
    private String kpiCode;
    @Schema(description = "kpi列标识")
    private String kpiColKey;
    @Schema(description = "kpi年标识")
    private String kpiYear;

	/**问题*/
	@Excel(name = "问题", width = 15)
    @Schema(description = "问题")
    private String why;

	/**原因*/
	@Excel(name = "原因", width = 15)
    @Schema(description = "原因")
    private String reason;

	/**值*/
	@Excel(name = "值", width = 15)
    @Schema(description = "值")
    private String value;

	/**差距贡献率*/
	@Excel(name = "差距贡献率", width = 15)
    @Schema(description = "差距贡献率")
    private String diffContribute;

	/**扩展配置*/
	@Excel(name = "扩展配置", width = 15)
    @Schema(description = "扩展配置")
    private String extJson;

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

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**更新时间*/
    @TableField(exist = false)
    private List<FiveWhyDetail> detailList;

    private static final long serialVersionUID = 1L;
}
