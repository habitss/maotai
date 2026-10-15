package com.keytech.modules.maotai.eh.vehiclecheck.controller;

import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckDTO;
import com.keytech.modules.maotai.eh.vehiclecheck.service.VehicleCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车检信息接收(API08: E+H → 平台)。
 * 入站接口,走 /api/**(无鉴权,原 X-Api-Token 鉴权已移除,靠 Shiro 放行 anon)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Tag(name = "车检信息接收(API08)")
@RestController
@RequestMapping("/api/WorkshopInfo")
@RequiredArgsConstructor
public class VehicleCheckController {

    private final VehicleCheckService vehicleCheckService;
    private final ApiOpLogService opLogService;

    /**
     * 接收 E+H 车检信息(API08: E+H → 平台)。
     * <p>处理链:count(checkNo) 判重 → 落库 vehicle_check → 按 carNo 联动已推工单的 business_status
     * → 自动回传 E+H(API11,passFlag 默认通过)。
     * @param dto 车检信息(checkNo 唯一;carNo 用于找工单;checkType 1-入厂 2-出厂 决定 business_status)
     * @return 平台通用响应,content 形如"成功;{联动结果};{回传E+H结果}"
     */
    @Operation(summary = "接收 E+H 车检信息", description = "API08: E+H → 平台。入站无鉴权(原 X-Api-Token 已移除)")
    @PostMapping("/Upload")
    public ApiResponse upload(@Valid @RequestBody VehicleCheckDTO dto) {
        var resp = vehicleCheckService.receive(dto);
        opLogService.attachInboundResponse("API08", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
