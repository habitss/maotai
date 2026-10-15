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
 * @Description: 曲线步骤配置
 * @Author: jeecg-boot
 * @Date:   2026-01-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_curve_step")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="曲线步骤配置")
public class PiCurveStep implements Serializable {


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

	/**区域*/
	@Excel(name = "区域", width = 15)
    @Schema(description = "区域")
    private Integer areaNo;

	/**年*/
	@Excel(name = "年", width = 15)
    @Schema(description = "年")
    private Integer yearNo;

	/**reccat*/
	@Excel(name = "reccat", width = 15)
    @Schema(description = "reccat")
    private Integer reccatNo;

	/**pcu*/
	@Excel(name = "pcu", width = 15)
    @Schema(description = "pcu")
    private String pcuNo;

	/**设备code*/
	@Excel(name = "设备code", width = 15)
    @Schema(description = "设备code")
    private String unitNo;

    /**配方*/
    @Excel(name = "配方", width = 15)
    @Schema(description = "配方")
    private Integer recipeNo;

	/**步骤集合(eopNo)*/
	@Excel(name = "步骤集合(eopNo)", width = 15)
    @Schema(description = "步骤集合(eopNo)")
    private String eopNos;

    /**rop集合(rop_ids)*/
    @Excel(name = "步骤集合(rop_ids)", width = 15)
    @Schema(description = "步骤集合(rop_ids)")
    private String ropIds;

    /**排除rop集合(rop_ids)*/
    @Excel(name = "排除rop集合(rop_ids)", width = 15)
    @Schema(description = "排除rop集合(rop_ids)")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String excludeRopIds;

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

    @Schema(description = "步骤ID集合")
    @TableField(exist = false)
    private String stepIds;
    @Schema(description = "步骤ID集合(排除)")
    @TableField(exist = false)
    private String excludeIds;

    //数据类型 1 adapter 3 pi
    @Schema(description = "数据类型")
    @TableField(exist = false)
    private String curveDataType;

    //批次
    @TableField(exist = false)
    private String batchNo;
}
