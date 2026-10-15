package com.keytech.modules.maotai.scada.common;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
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
 * 中集自控(SCADA)入站接口参数校验异常兜底。
 *
 * <p>与 {@link com.keytech.modules.maotai.eh.common.EhInboundValidationAdvice} 同款做法:
 * basePackages 扫 scada.controller 整包——入站4个(/openapi/v1/api/** 的 API06/13/17/报警,ScadaResponse 格式)是主目标;
 * 同包的 ScadaAlarmQueryController(/maotai/**)与 ScadaWorkOrderController(出站触发)走 jeecg Result,
 * 它们的 @Valid 失败若被拦成 ScadaResponse 会格式错位——handler 里按请求 URI 分流:
 * /openapi/v1/api/** 返回 ScadaResponse(中集方按 resultCode 解析),其余返回 jeecg Result(前端解析)。
 *
 * <p>@Order(HIGHEST_PRECEDENCE) 硬要求:抢在 jeecg 全局 advice 前面,否则英文长串兜底。
 * 鉴权层由 OAuth2 拦截器(/openapi/v1/** Bearer)负责,本 Advice 只管校验层。
 *
 * @author maotai
 * @since 2026-08-21
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {
        "com.keytech.modules.maotai.scada.controller"
})
public class ScadaInboundValidationAdvice {

    /** @Valid 校验失败:按 URI 分流——入站(/openapi/v1/api/**)拼中文短句回 ScadaResponse;内部端点回 jeecg Result */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException e, jakarta.servlet.http.HttpServletRequest req) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("; "));
        log.warn("[SCADA入站校验] 参数校验失败: {}", msg);
        String uri = req == null ? "" : req.getRequestURI();
        if (uri.contains("/openapi/v1/api/")) {
            return ScadaResponse.fail(null, "参数校验失败: " + msg);
        }
        return Result.error("参数校验失败: " + msg);
    }

    /** 请求体 JSON 解析失败(坏 JSON/字段类型不匹配),同样按 URI 分流 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleNotReadable(HttpMessageNotReadableException e, jakarta.servlet.http.HttpServletRequest req) {
        log.warn("[SCADA入站校验] 请求体解析失败: {}", e.getMessage());
        String uri = req == null ? "" : req.getRequestURI();
        if (uri.contains("/openapi/v1/api/")) {
            return ScadaResponse.fail(null, "请求体 JSON 格式错误或字段类型不匹配");
        }
        return Result.error("请求体 JSON 格式错误或字段类型不匹配");
    }
}
