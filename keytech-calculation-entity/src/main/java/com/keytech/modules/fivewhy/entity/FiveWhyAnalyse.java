package com.keytech.modules.fivewhy.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: 5Why分析
 * @Author: jeecg-boot
 * @Date:   2025-01-20
 * @Version: V1.0
 */
@Data
@TableName("mes3_five_why_analyse")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="5Why分析")
public class FiveWhyAnalyse implements Serializable {

	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
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

	/**问题*/
	@Excel(name = "问题", width = 15)
    @Schema(description = "问题")
    private String problem;

    /**问题描述*/
    @Excel(name = "问题描述", width = 15)
    private String problemDesc;

	/**字段中文*/
	@Excel(name = "字段中文", width = 15)
    @Schema(description = "字段中文")
    private String fieldName;

	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @Schema(description = "字段名")
    private String fieldCode;

	/**表名中文*/
	@Excel(name = "表名中文", width = 15)
    @Schema(description = "表名中文")
    private String tableName;

    /**表名*/
    @Excel(name = "表名", width = 15)
    @Schema(description = "表名")
    private String tableCode;

    /**报表数据id*/
    @Excel(name = "报表数据id", width = 15)
    @Schema(description = "报表数据id")
    private String dataId;

    @Schema(description = "kpi编码")
    private String kpiCode;
    @Schema(description = "kpi列标识")
    private String kpiColKey;
    @Schema(description = "kpi年标识")
    private String kpiYear;

    /**类型：1、pi；2、kpi*/
    @Excel(name = "类型：1、pi；2、kpi", width = 15)
    @Schema(description = "类型：1、pi；2、kpi")
    private String type;

	/**原因*/
	@Excel(name = "原因", width = 15)
    @Schema(description = "原因")
    private String reason;

	/**标题*/
	@Excel(name = "标题", width = 15)
    @Schema(description = "标题")
    private String title;

    /**5Wjson数据合并行规则*/
    @Schema(description = "5Wjson数据合并行规则")
    private String mergeRule;

	/**5Wjson数据*/
    @Excel(name = "5Wjson数据", width = 15)
    @Schema(description = "5Wjson数据")
    private String reasonJson;

    /**措施json数据*/
    @Excel(name = "措施json数据", width = 15)
    private String measuresJson;

    /**why数量*/
    @Excel(name = "why数量", width = 15)
    @Schema(description = "why数量")
    private Integer whyNum;

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

    private static final long serialVersionUID = 1L;
}
