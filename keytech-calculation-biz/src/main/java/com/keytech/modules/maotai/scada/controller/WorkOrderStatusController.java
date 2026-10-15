package com.keytech.modules.maotai.scada.controller;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStatusUpdateRequest;
import com.keytech.modules.maotai.scada.service.WorkOrderStatusService;
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
 * 中集自控(SCADA)工单状态更新接收(API13: 中集自控 → 平台)。
 * 入站接口,走 /openapi/v1/api/**(OAuth2 Bearer 鉴权,Shiro anon 放行后由 OAuth2 拦截器校验)。
 *
 * @author maotai
 * @since 2026-07-28
 */
@Tag(name = "中集自控工单状态更新(API13)")
@RestController
@RequestMapping("/openapi/v1/api/WorkOrder")
@RequiredArgsConstructor
public class WorkOrderStatusController {

    private final WorkOrderStatusService workOrderStatusService;
    private final ApiOpLogService opLogService;

    /**
     * 接收中集自控工单状态更新(API13):落库 + rqi 幂等,回中集自控公共响应(rqi/resultCode/resultDesp)。
     * <p>入站接口无鉴权(原 X-Api-Token 已移除)。
     * @param dto 状态更新(rqi 可选;OrderNo/status/updateTime 必填;batchNo 鹤位可选)
     * @return 中集自控公共响应(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @Operation(summary = "接收中集自控工单状态更新", description = "API13: 中集自控 → 平台。入站走 OAuth2 Bearer(/openapi/v1/**)")
    @PostMapping("/StatusUpdate")
    public ScadaResponse statusUpdate(@Valid @RequestBody WorkOrderStatusUpdateRequest dto) {
        var resp = workOrderStatusService.receive(dto);
        opLogService.attachInboundResponse("API13", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
