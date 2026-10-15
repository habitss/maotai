package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: pi报表字段分配
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_allocation_table_field")
@Schema(description="pi报表字段分配")
public class PiAllocationTableField implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description="id")
    private String id;

    /**租户id**/
    @Schema(description="租户id")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description="工厂")
    private String factoryCode;
	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description="部门")
    private String deptCode;
	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description="产线")
    private String lineCode;


    /**报表分配主表id*/
    @Excel(name = "报表分配主表id", width = 15)
    @Schema(description="报表分配主表id")
    private String allocationTableId;

	/**自定义表id*/
	@Excel(name = "自定义表id", width = 15)
    @Schema(description="自定义表id")
    private String tableId;
    /**自定义表表名*/
    @Excel(name = "自定义表表名", width = 15)
    @Schema(description="自定义表表名")
    private String tableName;

    /**自定义表字段id*/
    @Excel(name = "自定义表字段id", width = 15)
    @Schema(description="自定义表字段id")
    private String fieldId;

    /**字段中文名称*/
    @Excel(name = "字段中文名称", width = 15)
    @Schema(description="字段中文名称")
    private String name;

	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @Schema(description="字段名")
    private String fieldName;
	/**字段备注*/
	@Excel(name = "字段备注", width = 15)
    @Schema(description="字段备注")
    private String notes;
	/**字段长度*/
	@Excel(name = "字段长度", width = 15)
    @Schema(description="字段长度")
    private Integer fieldLength;
	/**小数点*/
	@Excel(name = "小数点", width = 15)
    @Schema(description="小数点")
    private Integer decimalPoint;
	/**默认值*/
	@Excel(name = "默认值", width = 15)
    @Schema(description="默认值")
    private String fieldDefault;
	/**字段类型*/
	@Excel(name = "字段类型", width = 15)
    @Schema(description="字段类型")
    private String type;
	/**是否表单显示(0否/1是)*/
	@Excel(name = "是否表单显示", width = 15)
    @Schema(description="是否表单显示(0否/1是)")
    private Integer isForm;
	/**是否列表显示(0否/1是)*/
	@Excel(name = "是否列表显示", width = 15)
    @Schema(description="是否列表显示(0否/1是)")
    private Integer isList;
	/**是否排序(0否/1是)*/
	@Excel(name = "是否排序", width = 15)
    @Schema(description="是否排序(0否/1是)")
    private Integer isSort;
	/**排序方式(0降序/1升序)*/
	@Excel(name = "排序方式", width = 15)
    @Schema(description="排序方式(0降序/1升序)")
    private Integer sortType;
	/**是否查询(0否/1是)*/
	@Excel(name = "是否查询", width = 15)
    @Schema(description="是否查询(0否/1是)")
    private Integer isQuery;

    /**是否模糊查询(0否/1是)*/
    @Excel(name = "是否模糊查询", width = 15)
    @Schema(description="是否模糊查询(0否/1是)")
    private Integer isLike;

    /**是否编辑(0否/1是)*/
    @Excel(name = "是否编辑", width = 15)
    @Schema(description="是否编辑(0否/1是)")
    private String isEdit;

    /**录入控件类型(组件)*/
    @Excel(name = "录入控件类型", width = 15)
    @Schema(description="录入控件类型")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String editWidgetType;

    /**下拉数据源*/
    @Excel(name = "下拉数据源", width = 15)
    @Schema(description="下拉数据源")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String dropdownData;

    /**是否固定列(0否/1是)*/
    @Excel(name = "是否固定列", width = 15)
    @Schema(description="是否固定列(0否/1是)")
    private String isFixed;

    /**是否重点关注(0否/1是)*/
    @Excel(name = "是否重点关注", width = 15)
    @Schema(description="是否重点关注(0否/1是)")
    private String isFocus;

    /**是否工厂关注(0否/1是)*/
    @Excel(name = "是否工厂关注", width = 15)
    @Schema(description="是否工厂关注(0否/1是)")
    private String isFactoryFocus;

    /**是否参与绿区率统计(0否/1是)*/
    @Excel(name = "是否参与绿区率统计", width = 15)
    @Schema(description="是否参与绿区率统计(0否/1是)")
    private String isStatistics;

    /**-绿区率目标值*/
    @Excel(name = "-绿区率目标值", width = 15)
    @Schema(description="-绿区率目标值")
    private Double greenTarget;

    /**单位*/
    @Excel(name = "单位", width = 15)
    @Schema(description="单位")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String piUnit;

    /**是否启用设定值（0-禁用，1-启用）*/
    @Excel(name = "是否启用设定值", width = 15)
    @Schema(description="是否启用设定值")
    private String isValueEnabled;

    /**设定值*/
    @Excel(name = "设定值", width = 15)
    @Schema(description="设定值")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String setValue;

    /**时间展示格式（1-年月日时分秒，2-年月日，3-时分秒，4-时分，5-时）*/
    @Excel(name = "时间展示格式", width = 15)
    @Schema(description="时间展示格式")
    private String timeFormat;

    /**查询是否显示历史数据*/
    @Excel(name = "查询是否显示历史数据", width = 15)
    @Schema(description="查询是否显示历史数据")
    private String historyData;

    /**展示宽度*/
    @Excel(name = "展示宽度", width = 15)
    @Schema(description="展示宽度")
    private Integer width;

    /**排序*/
    @Excel(name = "排序", width = 15)
    @Schema(description="排序")
    private Double sort;
    /**删除状态(0未删,1已删)*/
    @Excel(name = "删除状态", width = 15)
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

    /**是否唯一约束(0否1是)*/
    @Schema(description="是否唯一约束")
    private Integer isUnique;

    /**保留几位小数*/
    @Schema(description="保留几位小数")
    private Integer keepDecimals;

    /**取值方式*/
    @Schema(description="取值方式")
    private String takingValue;

    @Schema(description="是否显示标准值1是，0否")
    private Boolean showStandard;

    @Schema(description="是否显示偏差值1是，0否")
    private Boolean showDeviation;

    @Schema(description="是否显示实际值1是，0否")
    private Boolean showActual;

    /**是否自动PI 1自动PI 0手动PI*/
    @Schema(description="是否自动PI")
    private String automaticPi;

    /**自定义字段id集合*/
    @TableField(exist = false)
    private String fields;
    /**重复引用功能使用，字段id*/
    @TableField(exist = false)
    private String citeField;
    /**查询类型 1基础报表/ 2PI日报 */
    @TableField(exist = false)
    private String reportType;
    /**
     * 自定义报表-(上下页查询)
     * 0下一批 / 1上一批
     */
    @TableField(exist = false)
    private String upOrDown;

    /**
     * 多表关联条件字段
     */
    @TableField(exist = false)
    private String conditionJson;

    /**
     * 当前记录的排序字段值JSON（用于上一批/下一批查询）
     * 格式: {"field1":"value1","field2":"value2"}
     */
    @TableField(exist = false)
    private String currentSortValues;

    /**
     * 是否分配多表(0否/1是)
     */
    @TableField(exist = false)
    private String multipleTables;
    /**
     * 多表关联关联表集合
     */
    @TableField(exist = false)
    private String tableNames;

    /**
     * 关联方式
     */
    @TableField(exist = false)
    private String correlation;
}
