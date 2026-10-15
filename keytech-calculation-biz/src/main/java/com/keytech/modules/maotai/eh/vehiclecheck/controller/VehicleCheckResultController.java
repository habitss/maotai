package com.keytech.modules.maotai.eh.vehiclecheck.controller;

import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckResultRequest;
import com.keytech.modules.maotai.eh.vehiclecheck.service.VehicleCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车检结果回传(API11: 平台 → E+H,出站)。
 * 特例:虽是出站,但暴露在 /api/** 对外触发入口(别的平台/前端调它来把结果转发给 E+H),无鉴权(原 X-Api-Token 已移除,靠 Shiro 放行 anon;非内部 /maotai/** 的 Shiro JWT)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Tag(name = "车检结果回传(API11)")
@RestController
@RequestMapping("/api/VehicleCheck")
@RequiredArgsConstructor
public class VehicleCheckResultController {

    private final VehicleCheckService vehicleCheckService;

    /**
     * 车检结果回传 E+H(API11: 平台 → E+H, /api/Bussiness/Update, 更新业务命令 ModifyType=5)。
     * <p>单独暴露给别的平台/前端调用,把车检结果转发给 E+H。
     * ModifyType 不传默认 5(更新车检结果);ModifyValue "1"通过/"0"或其他不通过;ModifyTime 不传取当前时间。出站超时/异常降级返回 fail,不抛异常。
     * @param req 车检结果(OrderNo/CarNo/Modifyor/ModifyValue 必填;busiType/ModifyType/ModifyTime 可选)
     * @return 平台通用响应,content 含 E+H 的返回内容
     */
    @Operation(summary = "车检结果回传 E+H", description = "API11: 平台 → E+H,4.3 更新业务命令(ModifyType=5)。入站无鉴权(原 X-Api-Token 已移除)")
    @PostMapping("/Send")
    public ApiResponse send(@Valid @RequestBody VehicleCheckResultRequest req) {
        return vehicleCheckService.sendResultToEh(req);
    }
}
