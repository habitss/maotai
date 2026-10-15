package com.keytech.modules.maotai.eh.orderfeedback.controller;

import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.orderfeedback.dto.OrderFeedbackDTO;
import com.keytech.modules.maotai.eh.orderfeedback.service.OrderFeedbackService;
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
 * 工单信息回传接收(API15: E+H → 平台)。
 * 入站接口,走 /api/**(无鉴权,原 X-Api-Token 鉴权已移除,靠 Shiro 放行 anon)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Tag(name = "工单信息回传(API15)")
@RestController
@RequestMapping("/api/Bussiness")
@RequiredArgsConstructor
public class OrderFeedbackController {

    private final OrderFeedbackService orderFeedbackService;
    private final ApiOpLogService opLogService;

    /**
     * 接收 E+H 工单信息回传(API15: E+H → 平台)。
     * <p>E+H 装卸完成后,把实际装卸量、开始/结束时间、业务状态回传给平台。
     * 字段已去掉 ABV(酒精度数)、flowMeterTagNumber(流量计位号)。入站无幂等,重复推会落重复回传记录。
     * @param dto 回传信息(OrderNo + CarNo + feedbackTime 必填)
     * @return 平台通用响应
     */
    @Operation(summary = "接收 E+H 工单信息回传", description = "API15: E+H → 平台。入站无鉴权(原 X-Api-Token 已移除)")
    @PostMapping("/Finish")
    public ApiResponse finish(@Valid @RequestBody OrderFeedbackDTO dto) {
        var resp = orderFeedbackService.receive(dto);
        opLogService.attachInboundResponse("API15", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
