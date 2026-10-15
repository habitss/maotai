package com.keytech.modules.maotai.mes.controller;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.OrderFinishSummaryCommand;
import com.keytech.modules.maotai.mes.service.OrderFinishSummaryService;
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
 * 勾储MES 工单结束汇总推送(API18: 平台 → 勾储MES, 出站)。
 * 平台内部端点 /maotai/mes/**(Shiro JWT);鉴权待实装。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Tag(name = "勾储MES 工单结束汇总(API18)")
@RestController
@RequestMapping("/maotai/mes")
@Slf4j
@RequiredArgsConstructor
public class OrderFinishSummaryController {

    private final OrderFinishSummaryService orderFinishSummaryService;

    /**
     * 工单结束汇总推送(API18:平台 → 勾储MES /GZMES/notify/orderFinish)。
     * <p>按 orderNo 从 loading_order 组装(actualWeight/loadStartTime/loadEndTime/ABV + status=8 + Modifyor=当前用户)后推给勾储MES;鉴权待实装。
     * @param cmd 入参(orderNo 订单号 + rqi 请求流水Id,响应回传同请求)
     * @return Result&lt;MesResponse&gt;(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @AutoLog(value = "勾储MES-工单结束汇总")
    @Operation(summary = "工单结束汇总推送(转发勾储MES)",
            description = "API18: 平台 → 勾储MES /GZMES/notify/orderFinish。按 orderNo 从 loading_order 组装;鉴权待实装")
    @PostMapping("/orderFinish/push")
    public Result<MesResponse> push(@Valid @RequestBody OrderFinishSummaryCommand cmd) {
        MesResponse resp = orderFinishSummaryService.pushOrderFinish(cmd.getOrderNo(), cmd.getRqi());
        if (resp.isSuccess()) {
            return Result.OK("推送成功", resp);
        }
        return Result.error("推送失败: " + resp.getResultDesp());
    }
}
