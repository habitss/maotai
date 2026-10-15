package com.keytech.modules.line.efficiency.entity;

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
 * @Description: 外排水点位配置
 * @Author: jeecg-boot
 * @Date:   2025-07-29
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_ehs_points")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="外排水点位配置")
public class LineEfficiencyEhsPoints implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**ehs代码*/
	@Excel(name = "ehs代码", width = 15)
    @Schema(description = "ehs代码")
    private String code;

	/**ehs名称*/
	@Excel(name = "ehs名称", width = 15)
    @Schema(description = "ehs名称")
    private String name;

	/**是否必填*/
	@Excel(name = "是否必填", width = 15)
    @Schema(description = "是否必填")
    private String status;

	/**点位代码*/
	@Excel(name = "点位代码", width = 15)
    @Schema(description = "点位代码")
    private String pointCode;

	/**点位名称*/
	@Excel(name = "点位名称", width = 15)
    @Schema(description = "点位名称")
    private String pointName;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag = 0;

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

    private static final long serialVersionUID = 1L;
}
