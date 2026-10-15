package com.keytech.modules.maotai.eh.ehLoading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 装卸车推送流水(平台 → E+H 的每次 Create/Close 调用记录)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@TableName("mes3_maotai_loading_push_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "装卸车推送流水")
public class LoadingPushLog extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "工单号")
    private String orderNo;

    @Schema(description = "推送类型 Create/Close")
    private String pushType;

    @Schema(description = "请求体 JSON")
    private String reqBody;

    @Schema(description = "E+H 响应码 1/0")
    private Integer respCode;

    @Schema(description = "E+H 响应内容")
    private String respContent;

    @Schema(description = "推送时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;
}
