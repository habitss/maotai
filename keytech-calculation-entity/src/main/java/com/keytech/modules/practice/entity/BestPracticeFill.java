package com.keytech.modules.practice.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.keytech.modules.common.DataBase;
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
 * @Description: 最佳实践-填报
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Data
@TableName("mes3_best_practice_fill")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="最佳实践-填报")
public class BestPracticeFill implements Serializable {
    @Schema(description="id")
    @Excel(name = "主键(新增数据时请删除此列)", width = 15)
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

    /**工厂*/
    @Schema(description="工厂")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

    /**部门*/
    @Schema(description="部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

    /**产线*/
    @Schema(description="产线")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**名称*/
	@Excel(name = "名称", width = 15)
    @Schema(description = "名称")
    private String title;

	/**年月*/
    @Schema(description = "年月")
    private String yearMon;

	/**提报员工*/
	@Excel(name = "提报员工", width = 15,dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "提报员工")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String userId;

	/**提报班组*/
	@Excel(name = "提报班组", width = 15,dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    @Schema(description = "提报班组")
    @Dict(dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    private String shiftGroupId;

	/**提报部门编码*/
	@Excel(name = "提报部门", width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "提报部门编码")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String reportDeptId;

	/**提报部门名称*/
    @Schema(description = "提报部门名称")
    private String reportDeptName;

	/**提报时间*/
	@Excel(name = "提报时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "提报时间")
    private Date reportTime;

	/**改善类型*/
	@Excel(name = "改善类型", width = 15,dictTable ="mes3_best_practice_type",dicText = "type_name",dicCode = "id")
    @Schema(description = "改善类型")
    @Dict(dictTable ="mes3_best_practice_type",dicText = "type_name",dicCode = "id")
    private String improvementType;

    /**改善效果*/
    @Excel(name = "改善效果", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_BEST_EFFECT'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "改善效果")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_BEST_EFFECT'", dicText = "item_key", dicCode = "item_val")
    private String improvementEffect;

    /**对应KPI年份*/
    @Schema(description = "对应KPI年份")
    private String kpiYear;

    /**对应KPI编码*/
    @Schema(description = "对应KPI编码")
    @Dict(dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "id")
    private String kpiCode;

    /**是否填写班组打分表1：是；0：否*/
    @Schema(description = "是否填写班组打分表1：是；0：否")
    private Integer shiftGroupResult;

    /**班组平均分*/
    @Schema(description = "班组平均分")
    private String shiftGroupAvgScore;

    /**班组评审报表id*/
    @Schema(description = "班组评审报表id")
    private String shiftGroupReportId;

    /**班组评审意见*/
    @Schema(description = "班组评审意见")
    private String shiftGroupReview;

    /**是否填写部门打分表1：是；0：否*/
    @Schema(description = "是否填写部门打分表1：是；0：否")
    private Integer deptResult;

    /**部门平均分*/
    @Schema(description = "部门平均分")
    private String deptAvgScore;

    /**部门评审报表id*/
    @Schema(description = "部门评审报表id")
    private String deptReportId;

    /**部门评审意见*/
    @Schema(description = "部门评审意见")
    private String deptReview;

    /**是否填写工厂打分表1：是；0：否*/
    @Schema(description = "是否填写工厂打分表1：是；0：否")
    private Integer factoryResult;

    /**工厂平均分*/
    @Schema(description = "工厂平均分")
    private String factoryAvgScore;

    /**工厂评审报表id*/
    @Schema(description = "工厂评审报表id")
    private String factoryReportId;

    /**工厂评审意见*/
    @Schema(description = "工厂评审意见")
    private String factoryReview;

    /**排序*/
    @Schema(description="排序")
    private Double sort;
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

    private static final long serialVersionUID = 1L;
}
