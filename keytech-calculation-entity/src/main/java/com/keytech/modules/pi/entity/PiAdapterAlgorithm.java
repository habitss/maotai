package com.keytech.modules.pi.entity;

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
 * @Description: Adapter-算法配置
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_adapter_algorithm")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Adapter-算法配置")
public class PiAdapterAlgorithm implements Serializable {


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

	/**pi存储表*/
	@Excel(name = "pi存储表", width = 15)
    @Schema(description = "pi存储表")
    private String piTable;

	/**pi编码*/
	@Excel(name = "pi编码", width = 15)
    @Schema(description = "pi编码")
    private String piCode;

	/**区域*/
	@Excel(name = "区域", width = 15)
    @Schema(description = "区域")
    @Dict(dictTable = "mes3_mdm_dict where group_code = 'MES3_PI_ADAPTER_AREA'", dicText = "item_key", dicCode = "item_val")
    private String areaNo;

    /**PCU编码*/
    @Excel(name = "PCU编码", width = 15)
    @Schema(description = "PCU编码")
    private Integer pcuNo;

	/**设备编码*/
	@Excel(name = "设备编码", width = 15)
    @Schema(description = "设备编码")
    private Integer unitNo;

	/**EOP号*/
	@Excel(name = "EOP号", width = 15)
    @Schema(description = "EOP号")
    private Integer eopNo;

	/**取值(时间/时长/参数)*/
	@Excel(name = "取值", width = 15)
    @Schema(description = "取值")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String val;

    /**取值类型(值为参数-0设定值/1实际值)*/
    @Excel(name = "取值类型", width = 15)
    @Schema(description = "取值类型")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String valType;

	/**取第几次*/
	@Excel(name = "取第几次", width = 15)
    @Schema(description = "取第几次")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer several;

    /**ropId*/
    @Excel(name = "ropId", width = 15)
    @Schema(description = "ropId")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String ropId;

    /**算法类型 1 Aadapte 2 RA*/
    @Excel(name = "算法类型", width = 15)
    @Schema(description = "算法类型")
    private String type;

    /**RA 产线id*/
    @Excel(name = "产线id", width = 15)
    @Schema(description = "产线id")
    private String lineId;

    /**步骤筛选条件*/
    @Excel(name = "步骤筛选条件", width = 15)
    @Schema(description = "步骤筛选条件")
    private String stepCondition;

    /**时间格式 1分  2时  3天*/
    @Excel(name = "产线id", width = 15)
    @Schema(description = "产线id")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer timeFormat;

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
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;

    @Schema(description="约束条件-描述(说明)")
    @TableField(exist = false)
    private String conditionExplanation;

    @Schema(description="设备名称")
    @TableField(exist = false)
    private String unitName;

    @Schema(description="EOP名称")
    @TableField(exist = false)
    private String eopName;


}
