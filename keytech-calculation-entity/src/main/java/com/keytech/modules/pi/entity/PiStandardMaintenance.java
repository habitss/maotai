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
 * PI标准维护
 * @TableName mes3_pi_standard_maintenance
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_standard_maintenance")
@Schema(description="PI标准维护")
public class PiStandardMaintenance implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * pi标准管理id
     */
    @Schema(description="pi标准管理id")
    @Excel(name = "标准id", width = 20)
    private String manageId;

    /**
     * pi标准管理名称
     */
    @Schema(description="pi标准管理名称")
    @Excel(name = "标准名称", width = 20)
    private String manageName;

    /**
     * 表英文名
     */
    @Schema(description="报表名称")
    @Excel(name = "表英文名", width = 20)
    private String tableText;

    /**
     * 表中文名
     */
    @Schema(description="表名")
    @Excel(name = "表中文名", width = 20)
    private String tableName;

    /**
     * 表字段英文
     */
    @Schema(description="PI字段")
    @Excel(name = "表字段英文", width = 25)
    private String fieldName;

    /**
     * 表字段中文
     */
    @Schema(description="PI名称")
    @Excel(name = "表字段中文", width = 25)
    private String fieldText;

    /**
     * pi名称
     */
    @Schema(description="pi名称")
    private String piName;

    /**
     * 指标类型
     */
    @Schema(description="指标类型")
    @Excel(name = "指标类型", width = 15, replace = { "实际值_1", "偏差值_2" })
    private String indicatorType;

    /**
     * 红下
     */
    @Schema(description="红下")
    @Excel(name = "红下", width = 15)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double redDown;

    /**
     * 黄下
     */
    @Schema(description="黄下")
    @Excel(name = "黄下", width = 15)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double yellowDown;

    /**
     * 标准值
     */
    @Schema(description="标准值")
    @Excel(name = "标准值", width = 15)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double standardValue;

    /**
     * 黄上
     */
    @Schema(description="黄上")
    @Excel(name = "黄上", width = 15)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double yellowUp;

    /**
     * 红上
     */
    @Schema(description="红上")
    @Excel(name = "红上", width = 15)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double redUp;

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



    /**
     * 标准名称
     */
    @Schema(description="标准名称")
    @TableField(exist = false)
    private String standardName;
    /**
     * 启用时间
     */
    @Schema(description="启用时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(exist = false)
    private Date enableTime;

    /**
     * 停用时间
     */
    @Schema(description="停用时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @TableField(exist = false)
    private Date stopTime;

    /**
     * 版本号
     */
    @Schema(description="版本号")
    @TableField(exist = false)
    private String standardVersion;

    /**
     * 计算时间对照字段
     */
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "field_name")
    @Schema(description="计算时间对照字段")
    @TableField(exist = false)
    private String calcTimeField;

    /**
     * 计算时间对照字段-中文描述
     */
    @Schema(description="计算时间对照字段-中文描述")
    @TableField(exist = false)
    private String calcTimeFieldName;

    /**
     * 条件文本
     */
    @Schema(description="条件文本")
    @TableField(exist = false)
    private String conditionText;


}