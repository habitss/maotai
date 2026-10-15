package com.keytech.modules.maotai.eh.common;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * E+H 出站/内部接口(/maotai/**,ehLoading 包)参数校验异常兜底。
 *
 * <p>与 {@link EhInboundValidationAdvice} 成对:那个管入站 3 包返回 E+H 约定 ApiResponse;
 * 这个管出站包返回 jeecg Result(前端按 Result 格式解析,不能错换成 ApiResponse)。
 * 没有它,@Valid 失败落 jeecg jar 全局兜底,返回"Validation failed for argument [0]..."英文长串,
 * DTO 注解里精心写的中文 message 被埋在长串里,前端弹出来没法看。
 *
 * <p>@Order(1) 同样是硬要求(抢在 jeecg 全局 advice 前面)。
 * 只扫 ehLoading.controller 一个包,不影响其它 /maotai/** 端点既有报错行为。
 *
 * @author maotai
 * @since 2026-08-18
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {
        "com.keytech.modules.maotai.eh.ehLoading.controller"
})
public class EhOutboundValidationAdvice {

    /** @Valid 注解校验失败:收集全部字段错误,拼成中文短句返回(jeecg Result 格式) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("; "));
        log.warn("[E+H出站校验] 参数校验失败: {}", msg);
        return Result.error("参数校验失败: " + msg);
    }

    /** 请求体 JSON 解析失败(坏 JSON/字段类型不匹配) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[E+H出站校验] 请求体解析失败: {}", e.getMessage());
        return Result.error("请求体 JSON 格式错误或字段类型不匹配");
    }
}
