package com.keytech.modules.custom.report.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 自定义报表模板配置
 * @TableName mes3_custom_report_template
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_custom_report_template")
@Schema(description="自定义报表模板配置")
public class CustomReportTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**
     * 租户id
     */
    @Schema(description="租户id")
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


    /**报表名称*/
    @Excel(name = "报表名称", width = 15)
    @Schema(description="报表名称")
    private String reportName;
    /**绑定表*/
    @Excel(name = "绑定表", width = 15)
    @Dict(dictTable ="mes3_custom_report",dicText = "name",dicCode = "id")
    @Schema(description="绑定表")
    private String tableId;
    /**表名*/
    @Excel(name = "表名", width = 15)
    @Schema(description="表名")
    private String tableName;
    /**
     * 模板配置JSON
     */
    @Schema(description="模板配置JSON")
    private String reportConfig;
    /**
     * 配置类型，1：自定义报表；2：pi报表  5: 表头配置
     */
    @Schema(description="配置类型，1：自定义报表；2：pi报表")
    private String reportType;

    /**
     * 关联表id
     */
    @Schema(description="关联表id")
    private String dataId;


    /**排序*/
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