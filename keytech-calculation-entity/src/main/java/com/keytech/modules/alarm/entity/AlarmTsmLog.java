package com.keytech.modules.alarm.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * TSM接口请求日志表
 * @author yepeng
 * @since 2026.02.08
 * */
@Data
@TableName("alarm_tsm_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="TSM接口请求日志表")
public class AlarmTsmLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**id*/
    @TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;
    /**类型（1-推送报警，2-查询状态）*/
    @Excel(name = "类型", width = 15, dictTable = "mes3_mdm_dict WHERE group_code='TSM_LogType'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict WHERE group_code='TSM_LogType'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "类型（1-推送报警，2-查询状态）")
    private Integer type;
    /**请求参数*/
    @Excel(name = "请求参数", width = 15)
    @Schema(description = "请求参数")
    private String requestParams;
    /**返回结果*/
    @Excel(name = "返回结果", width = 15)
    @Schema(description = "返回结果")
    private String responseResult;
    /**请求时间*/
    @Excel(name = "请求时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;
    /**响应时间*/
    @Excel(name = "响应时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "响应时间")
    private LocalDateTime responseTime;
    /**请求状态（1-成功，0-失败）*/
    @Excel(name = "请求状态", width = 15, dictTable = "mes3_mdm_dict WHERE group_code='TSM_ReqStatus'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict WHERE group_code='TSM_ReqStatus'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "请求状态（1-成功，0-失败）")
    private Integer status;
    /**错误信息*/
    @Excel(name = "错误信息", width = 15)
    @Schema(description = "错误信息")
    private java.lang.String errorMsg;
}
