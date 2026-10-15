package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import jakarta.validation.constraints.NotBlank;
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
 * @Description: KPI数据修改
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_report_exception_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI数据修改")
public class KpiReportExceptionLog implements Serializable {


    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

    /**租户ID*/
    @Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

    /**工厂*/
    @Excel(name = "工厂", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description="工厂")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;
    /**部门*/
    @Schema(description="部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;
    /**产线*/
    @Schema(description="产线")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线Code", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

    /**工段*/
    @Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

    /**KPI父编号*/
    @Excel(name = "KPI父编号", width = 15)
    @Schema(description = "KPI父编号")
    private String kpiParent;

    /**KPI编码*/
    @Excel(name = "KPI编码", width = 15)
    @Schema(description = "KPI编码")
    private String kpiCode;

    /**KPI名称*/
    @Excel(name = "KPI名称", width = 15)
    @Schema(description = "KPI名称")
    private String kpiName;

    /**更新原因*/
    @Excel(name = "更新原因", width = 15)
    @Schema(description = "更新原因")
    private String updateCause;

    /**1:green、2:red、3:yellow*/
    @Excel(name = "1:green、2:red、3:yellow", width = 15)
    @Schema(description = "1:green、2:red、3:yellow")
    private Integer oldColor;

    /**原值*/
    @Excel(name = "原值", width = 15)
    @Schema(description = "原值")
    private String oldValue;

    /**1:green、2:red、3:yellow*/
    @Excel(name = "1:green、2:red、3:yellow", width = 15)
    @Schema(description = "1:green、2:red、3:yellow")
    private Integer newColor;

    /**新值*/
    @Excel(name = "新值", width = 15)
    @Schema(description = "新值")
    private String newValue;

    /**周主键*/
    @Excel(name = "周主键", width = 15)
    @Schema(description = "周主键")
    private String weekId;

    /**统计指标 1:周累 2:月累*/
    @Excel(name = "统计指标 1:周累 2:月累", width = 15)
    @Schema(description = "统计指标 1:周累 2:月累")
    private Integer indicator;

    /**KPI层级code*/
    @Excel(name = "KPI层级code", width = 15)
    @Schema(description = "KPI层级code")
    private String reportId;

    /**1:审批通过 2;驳回  3:待审批*/
    @Excel(name = "1:审批通过 2;驳回  3:待审批", width = 15)
    @Schema(description = "1:审批通过 2;驳回  3:待审批")
    private Integer approveType;

    /**审批时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审批时间")
    private Date approveTime;

    /**KPI范围key*/
    @Excel(name = "KPI范围key", width = 15)
    @Schema(description = "KPI范围key")
    private String kpiDataKey;

    /**年*/
    @Excel(name = "年", width = 15)
    @Schema(description = "年")
    private Integer year;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

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
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

    /**更新人*/
    @Schema(description = "更新人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String updateBy;

    private transient String objectName;
    private transient String typeName;
    private transient String indicatorName;

    private static final long serialVersionUID = 1L;
}
