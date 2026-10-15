package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * PI标准管理
 * @TableName mes3_pi_standard_manage
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_standard_manage")
@Schema(description="PI标准管理")
public class PiStandardManage implements Serializable {

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    @Excel(name = "id", width = 25)
    private String id;
    /**租户ID**/
    @Schema(description="租户ID")
    private Integer tenantId;
    /**工厂*/
    @Schema(description="工厂")
    private String factoryCode;
    /**部门*/
    @Schema(description="部门")
    private String deptCode;
    /**产线*/
    @Schema(description="产线")
    private String lineCode;

    /**
     * pi基础数据表(mes3_pi_customize_table)id
     */
    @Schema(description="pi基础数据表Id")
    @Excel(name = "PI基础数据表Id", width = 25)
    private String customizeId;

    /**
     * 关联的数据库表名
     */
    @Schema(description="关联的数据库表名")
    @Excel(name = "关联的数据库表名", width = 25)
    private String tableName;

    /**
     * 标准名称
     */
    @Schema(description="标准名称")
    @Excel(name = "标准名称", width = 15)
    private String standardName;

    /**
     * 启用时间
     */
    @Schema(description="启用时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Excel(name = "启用时间", width = 15, exportFormat = "yyyy-MM-dd", importFormat = "yyyy-MM-dd")
    private Date enableTime;

    /**
     * 停用时间
     */
    @Schema(description="停用时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Excel(name = "停用时间", width = 15, exportFormat = "yyyy-MM-dd", importFormat = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date stopTime;

    /**
     * 版本号
     */
    @Schema(description="版本号")
    @Excel(name = "版本号", width = 15)
    private String standardVersion;

    /**
     * 计算时间对照字段
     */
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "field_name")
    @Schema(description="计算时间对照字段")
    @Excel(name = "重算时间对照字段", width = 15)
    private String calcTimeField;

    /**
     * 条件文本
     */
    @Schema(description="条件文本")
    @Excel(name = "条件", width = 15)
    private String conditionText;

    /**
     * 条件json
     */
    @Schema(description="条件json")
    @Excel(name = "JSON格式条件", width = 15)
    private String conditionJson;

    /**排序*/
    @Schema(description="排序")
    @Excel(name = "排序", width = 15)
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

    //字段英文
    @TableField(exist = false)
    private String fieldName;

    private static final long serialVersionUID = 1L;
}