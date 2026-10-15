package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

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
 * @Description: PI-结果转换模型配置-主表
 * @Author: jeecg-boot
 * @Date:   2026-02-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_result_transformation_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI-结果转换模型配置-主表")
public class PiResultTransformationModel implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**工厂code*/
	@Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factory;

	/**部门code*/
	@Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String dept;

	/**模型名称*/
	@Excel(name = "模型名称", width = 15)
    @Schema(description = "模型名称")
    private String modelName;

	/**结果输入类型0时间类型、1字符类型、2数字类型*/
	@Excel(name = "结果输入类型0时间类型、1字符类型、2数字类型", width = 15)
    @Schema(description = "结果输入类型0时间类型、1字符类型、2数字类型")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_RESULT_TYPE'", dicText = "item_key", dicCode = "item_val")
    private Integer resultType;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

 	/**结果转换模型配置-详情表*/
    @Schema(description="结果转换模型配置-详情表")
    @TableField(exist = false)
    private List<PiResultTransformationModelDetail> piResultTransformationModelDetailList;

    private static final long serialVersionUID = 1L;
}
