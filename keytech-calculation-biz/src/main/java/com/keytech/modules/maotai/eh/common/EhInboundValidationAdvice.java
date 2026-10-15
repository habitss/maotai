package com.keytech.modules.maotai.eh.common;

import com.keytech.modules.common.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * E+H 入站接口(/api/**)参数校验异常兜底。
 *
 * <p>只扫 3 个入站包(vehiclecheck/orderupdate/orderfeedback),不扫整个 eh 包——
 * ehLoading 包的 /maotai/** 走 jeecg Result 响应,混扫会把响应格式错换成 ApiResponse,前端解析错位。
 *
 * <p>@Order(1) 是硬要求:jeecg jar 的 JeecgBootExceptionHandler 是无 basePackages 的全局 advice
 * 且有兜底 handleException,若它排在前面,@Valid 失败会落兜底返回英文长串 500。
 *
 * <p>返回体用 E+H 约定的 ApiResponse{code,content,data},校验错误拼成中文短句
 * (如"订单号(OrderNo)不能为空; 车牌号(carNo)长度不能超过50")。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {
        "com.keytech.modules.maotai.eh.vehiclecheck.controller",
        "com.keytech.modules.maotai.eh.orderupdate.controller",
        "com.keytech.modules.maotai.eh.orderfeedback.controller"
})
public class EhInboundValidationAdvice {

    /** @Valid 注解校验失败:收集全部字段错误,拼成中文短句返回 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        log.warn("[E+H入站校验] 参数校验失败: {}", msg);
        return ApiResponse.fail("参数校验失败: " + msg);
    }

    /** 请求体 JSON 解析失败(坏 JSON/字段类型不匹配) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[E+H入站校验] 请求体解析失败: {}", e.getMessage());
        return ApiResponse.fail("请求体 JSON 格式错误或字段类型不匹配");
    }
}
