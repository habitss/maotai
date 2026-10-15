package com.keytech.modules.maotai.scada.controller;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadCommand;
import com.keytech.modules.maotai.scada.service.ScadaWorkOrderService;
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
 * 中集自控(SCADA)工单接口。API05
 * 出站(平台 → 中集自控系统),与 E+H 对接完全分开;鉴权暂未实装。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Tag(name = "中集自控工单(API05)")
@RestController
@RequestMapping("/maotai/scada")
@Slf4j
@RequiredArgsConstructor
public class ScadaWorkOrderController {

    private final ScadaWorkOrderService scadaWorkOrderService;

    /**
     * 启动工单(API05:平台 → 中集自控 /api/WorkOrder/Start)。
     * <p>把请求转发给中集自控;rqi 为请求流水Id,来自另一系统,原样转发、响应回传。
     * @param req 启动工单请求(8 业务字段 + rqi 必填;sourceTankCode 选填)
     * @return Result&lt;ScadaResponse&gt;(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @AutoLog(value = "中集自控-启动工单")
    @Operation(summary = "启动工单(转发中集自控)",
            description = "API05: 平台 → 中集自控 /api/WorkOrder/Start。鉴权待实装")
    @PostMapping("/WorkOrder/Start")
    public Result<ScadaResponse> start(@Valid @RequestBody WorkOrderStartRequest req) {
        ScadaResponse resp = scadaWorkOrderService.startWorkOrder(req);
        if (resp.isSuccess()) {
            return Result.OK("启动成功", resp);
        }
        return Result.error("启动失败: " + resp.getResultDesp());
    }

    /**
     * 装卸车工单同步(API14:平台 → 中集自控 /WorkOrder/SyncLoad)。
     * <p>按 OrderNo 从 loading_order 取数组装后推给中集自控;鉴权待实装。
     * @param cmd 入参(OrderNo 订单号 + rqi 请求流水Id,响应回传同请求)
     * @return Result&lt;ScadaResponse&gt;(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    @AutoLog(value = "装卸车工单同步到中集自控")
    @Operation(summary = "装卸车工单同步(转发中集自控)",
            description = "API14: 平台 → 中集自控 /api/WorkOrder/SyncLoad。按 OrderNo 从 loading_order 组装;鉴权待实装")
    @PostMapping("/WorkOrder/SyncLoad")
    public Result<ScadaResponse> syncLoad(@Valid @RequestBody WorkOrderSyncLoadCommand cmd) {
        ScadaResponse resp = scadaWorkOrderService.syncLoadWorkOrder(cmd.getOrderNo(), cmd.getRqi());
        if (resp.isSuccess()) {
            return Result.OK("同步成功", resp);
        }
        return Result.error("同步失败: " + resp.getResultDesp());
    }
}
