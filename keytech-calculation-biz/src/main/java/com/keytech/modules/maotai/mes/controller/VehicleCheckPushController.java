package com.keytech.modules.maotai.mes.controller;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.VehicleCheckPushCommand;
import com.keytech.modules.maotai.mes.service.VehicleCheckPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 勾储MES 车检信息推送(API09: 平台 → 勾储MES, 出站)。
 * 平台内部端点 /maotai/mes/**(Shiro JWT);鉴权待实装。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Tag(name = "勾储MES 车检信息推送(API09)")
@RestController
@RequestMapping("/maotai/mes")
@Slf4j
@RequiredArgsConstructor
public class VehicleCheckPushController {

    private final VehicleCheckPushService vehicleCheckPushService;

    /**
     * 车检信息推送(API09:平台 → 勾储MES /GZMES/notify/vehicleCheck)。
     * <p>按 checkNo 从 mes3_maotai_vehicle_check 组装后推给勾储MES;鉴权待实装。
     * @param cmd 入参(checkNo 车检流水号 + rqi 请求流水Id,响应回传同请求)
     * @return Result&lt;MesResponse&gt;(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @AutoLog(value = "勾储MES-车检信息推送")
    @Operation(summary = "车检信息推送(转发勾储MES)",
            description = "API09: 平台 → 勾储MES /GZMES/notify/vehicleCheck。按 checkNo 从 vehicle_check 组装;鉴权待实装")
    @PostMapping("/vehicleCheck/push")
    public Result<MesResponse> push(@Valid @RequestBody VehicleCheckPushCommand cmd) {
        MesResponse resp = vehicleCheckPushService.pushVehicleCheck(cmd.getCheckNo(), cmd.getRqi());
        if (resp.isSuccess()) {
            return Result.OK("推送成功", resp);
        }
        return Result.error("推送失败: " + resp.getResultDesp());
    }
}
