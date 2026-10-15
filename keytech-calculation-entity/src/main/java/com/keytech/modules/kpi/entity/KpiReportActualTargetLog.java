package com.keytech.modules.kpi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: kpi目标值调整记录
 * @Author: jeecg-boot
 * @Date:   2025-03-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_report_actual_target_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="kpi目标值调整记录")
public class KpiReportActualTargetLog extends DataBase implements Serializable {

    /**目标id*/
    @Excel(name = "目标id", width = 15)
    @Schema(description = "目标id")
    private String targetId;

	/**月份*/
	@Excel(name = "月份", width = 15)
    @Schema(description = "月份")
    private String updateMonth;

	/**值*/
	@Excel(name = "值", width = 15)
    @Schema(description = "值")
    private String value;

    private static final long serialVersionUID = 1L;
}
