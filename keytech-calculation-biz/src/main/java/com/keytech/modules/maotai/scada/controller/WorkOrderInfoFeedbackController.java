package com.keytech.modules.maotai.scada.controller;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderInfoFeedbackRequest;
import com.keytech.modules.maotai.scada.service.WorkOrderFeedbackService;
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
 * 中集自控(SCADA)工单信息回传接收(API17: 中集自控 → 平台)。
 * 入站接口,走 /openapi/v1/api/**(OAuth2 Bearer 鉴权,Shiro anon 放行后由 OAuth2 拦截器校验)。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Tag(name = "中集自控工单信息回传(API17)")
@RestController
@RequestMapping("/openapi/v1/api/WorkOrder")
@RequiredArgsConstructor
public class WorkOrderInfoFeedbackController {

    private final WorkOrderFeedbackService workOrderFeedbackService;
    private final ApiOpLogService opLogService;

    /**
     * 接收中集自控工单信息回传(API17):落库 + rqi 幂等,回中集自控公共响应(rqi/resultCode/resultDesp)。
     * <p>入站接口无鉴权(原 X-Api-Token 已移除)。
     * @param dto 回传(rqi/OrderNo/status/feedbackTime 必填;infoData 回传信息JSON 选填)
     * @return 中集自控公共响应(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @Operation(summary = "接收中集自控工单信息回传", description = "API17: 中集自控 → 平台。入站走 OAuth2 Bearer(/openapi/v1/**)")
    @PostMapping("/InfoFeedback")
    public ScadaResponse infoFeedback(@Valid @RequestBody WorkOrderInfoFeedbackRequest dto) {
        var resp = workOrderFeedbackService.receive(dto);
        opLogService.attachInboundResponse("API17", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
