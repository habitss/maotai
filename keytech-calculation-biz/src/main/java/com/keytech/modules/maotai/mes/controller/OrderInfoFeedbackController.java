package com.keytech.modules.maotai.mes.controller;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.OrderInfoFeedbackCommand;
import com.keytech.modules.maotai.mes.service.OrderInfoFeedbackService;
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
 * 勾储MES 工单信息回传推送(API07: 平台 → 勾储MES, 出站)。
 * 平台内部端点 /maotai/mes/**(Shiro JWT);鉴权待实装。
 *
 * @author maotai
 * @since 2026-08-06
 */
@Tag(name = "勾储MES 工单信息回传(API07)")
@RestController
@RequestMapping("/maotai/mes")
@Slf4j
@RequiredArgsConstructor
public class OrderInfoFeedbackController {

    private final OrderInfoFeedbackService orderInfoFeedbackService;

    /**
     * 工单信息回传推送(API07:平台 → 勾储MES /GZMES/notify/orderInfo)。
     * <p>按 orderNo 从 loading_order 组装 actualWeight;status 由调用方传(1/3/5/8/4);feedbackTime 为空取当前时间;鉴权待实装。
     * @param cmd 入参(orderNo + rqi + status 必填,execData/feedbackTime 可选,响应回传同请求 rqi)
     * @return Result&lt;MesResponse&gt;(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @AutoLog(value = "勾储MES-工单信息回传")
    @Operation(summary = "工单信息回传推送(转发勾储MES)",
            description = "API07: 平台 → 勾储MES /GZMES/notify/orderInfo。按 orderNo 从 loading_order 组装 actualWeight;status 由调用方传;鉴权待实装")
    @PostMapping("/orderInfo/push")
    public Result<MesResponse> push(@Valid @RequestBody OrderInfoFeedbackCommand cmd) {
        MesResponse resp = orderInfoFeedbackService.pushOrderInfo(
                cmd.getOrderNo(), cmd.getRqi(), cmd.getStatus(), cmd.getExecData(), cmd.getFeedbackTime());
        if (resp.isSuccess()) {
            return Result.OK("推送成功", resp);
        }
        return Result.error("推送失败: " + resp.getResultDesp());
    }
}
