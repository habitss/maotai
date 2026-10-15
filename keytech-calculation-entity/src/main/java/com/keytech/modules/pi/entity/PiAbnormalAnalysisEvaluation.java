package com.keytech.modules.pi.entity;

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
 * @Description: 异常分析评价
 * @Author: jeecg-boot
 * @Date:   2025-03-03
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_abnormal_analysis_evaluation")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="异常分析评价")
public class PiAbnormalAnalysisEvaluation implements Serializable {


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

	/**评价内容*/
	@Excel(name = "评价内容", width = 15)
    @Schema(description = "评价内容")
    private String evaluationContent;

	/**评价得分*/
	@Excel(name = "评价得分", width = 15)
    @Schema(description = "评价得分")
    private String evaluationScore;

	/**评价结果0：不合格；1：合格*/
	@Excel(name = "评价结果0：不合格；1：合格", width = 15)
    @Schema(description = "评价结果0：不合格；1：合格")
    private String evaluationResult;

	/**评价用户id*/
	@Excel(name = "评价用户id", width = 15)
    @Schema(description = "评价用户id")
    private String evaluationUser;

	/**评价类型1：班组评价；2：车间评价；3：员工评价*/
	@Excel(name = "评价类型1：班组评价；2：车间评价；3：员工评价", width = 15)
    @Schema(description = "评价类型1：班组评价；2：车间评价；3：员工评价")
    private String evaluationType;

	/**异常分析数据id*/
	@Excel(name = "异常分析数据id", width = 15)
    @Schema(description = "异常分析数据id")
    private String recordId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
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

    private static final long serialVersionUID = 1L;
}
