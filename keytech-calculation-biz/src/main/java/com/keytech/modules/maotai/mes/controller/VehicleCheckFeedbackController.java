package com.keytech.modules.maotai.mes.controller;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.VehicleCheckFeedbackRequest;
import com.keytech.modules.maotai.mes.service.VehicleCheckFeedbackService;
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
 * 勾储MES 车检反馈接收(API10: 勾储MES → 平台)。
 * 入站接口,走 /api/**(无鉴权,原 X-Api-Token 鉴权已移除,靠 Shiro 放行 anon)。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Tag(name = "勾储MES 车检反馈(API10)")
@RestController
@RequestMapping("/api/VehicleCheck")
@RequiredArgsConstructor
public class VehicleCheckFeedbackController {

    private final VehicleCheckFeedbackService vehicleCheckFeedbackService;
    private final ApiOpLogService opLogService;

    /**
     * 接收勾储MES 车检反馈(API10):落库 + rqi 幂等,回公共响应(rqi/resultCode/resultDesp)。
     * <p>入站接口无鉴权(原 X-Api-Token 已移除)。
     * @param dto 反馈(rqi/checkNo/carNo/passFlag 必填;passReason/allowWeight 选填)
     * @return 公共响应(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @Operation(summary = "接收勾储MES 车检反馈", description = "API10: 勾储MES → 平台。入站无鉴权(原 X-Api-Token 已移除)")
    @PostMapping("/Result")
    public MesResponse result(@Valid @RequestBody VehicleCheckFeedbackRequest dto) {
        var resp = vehicleCheckFeedbackService.receive(dto);
        opLogService.attachInboundResponse("API10", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
