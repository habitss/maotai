package com.keytech.modules.kpi.entity;

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
 * @Description: kpi不达标推送记录
 * @Author: jeecg-boot
 * @Date:   2025-07-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_notstandard_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="kpi不达标推送记录")
public class KpiNotstandardLog implements Serializable {


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

	/**5why id*/
	@Excel(name = "5why id", width = 15)
    @Schema(description = "5why id")
    private String analyseId;

	/**第三方维护KPI id*/
	@Excel(name = "第三方维护KPI id", width = 15)
    @Schema(description = "第三方维护KPI id")
    private String thirdPlayId;

	/**kpi code*/
	@Excel(name = "kpi code", width = 15)
    @Schema(description = "kpi code")
    private String kpiCode;

	/**推送参数*/
	@Excel(name = "推送参数", width = 15)
    @Schema(description = "推送参数")
    private String push;

	/**返回结果*/
	@Excel(name = "返回结果", width = 15)
    @Schema(description = "返回结果")
    private String result;

	/**推送状态 0未推送 1已推送(新增) 3已推送(修改) 4.已推送(删除) -1.推送失败*/
	@Excel(name = "推送状态 0未推送 1已推送(新增) 3已推送(修改) 4.已推送(删除) -1.推送失败", width = 15)
    @Schema(description = "推送状态 0未推送 1已推送(新增) 3已推送(修改) 4.已推送(删除) -1.推送失败")
    private String state;

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
}
