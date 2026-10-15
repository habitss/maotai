package com.keytech.modules.ems.vo;

import com.keytech.modules.ems.entity.CollectPointStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采集结果 VO
 * @author yepeng
 * @since 2025.05.04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="采集结果VO")
public class CollectResultVo {
    /** 点位状态 */
    @Schema(description="点位状态")
    private CollectPointStatus pointStatus;
    /** 底数值 */
    @Schema(description="底数值")
    private BigDecimal value;
    /** 用量值 */
    @Schema(description="用量值")
    private BigDecimal usage;
    /** 是否成功 */
    @Schema(description="是否成功")
    private boolean success;
    /** 错误消息 */
    @Schema(description="错误消息")
    private String errorMsg;
    /** 错误类型 */
    @Schema(description="错误类型")
    private Integer errorType;

    /**
     * 成功
     * @param pointStatus 点位状态
     * @param value 值
     * @param usage 用量
     */
    public static CollectResultVo success(CollectPointStatus pointStatus, BigDecimal value, BigDecimal usage, Integer errorType) {
        CollectResultVo r = new CollectResultVo();
        r.pointStatus = pointStatus;
        r.value = value;
        r.usage = usage;
        r.errorType = errorType;
        r.success = true;
        return r;
    }

    /**
     * 失败
     * @param pointStatus 点位状态
     * @param errorMsg 失败消息
     * @param errorType  失败类型
     */
    public static CollectResultVo fail(CollectPointStatus pointStatus, String errorMsg, Integer errorType) {
        CollectResultVo r = new CollectResultVo();
        r.pointStatus = pointStatus;
        r.errorMsg = errorMsg;
        r.errorType = errorType;
        r.success = false;
        return r;
    }

    public LocalDateTime getCollectTime() {
        return pointStatus.getCollectTime();
    }

    public String getTagId() {
        return pointStatus.getTagId();
    }

}