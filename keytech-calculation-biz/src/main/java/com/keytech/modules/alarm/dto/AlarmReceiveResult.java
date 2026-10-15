package com.keytech.modules.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 报警数据统一接口返回结果
 * @author yepeng
 * @since 2026.01.20
 */
@Data
@AllArgsConstructor
public class AlarmReceiveResult {
    /**
     * 状态码
     */
    private String code;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 消息
     */
    private String message;
    /**
     * 报警事件ID
     */
    private String requestId;

}