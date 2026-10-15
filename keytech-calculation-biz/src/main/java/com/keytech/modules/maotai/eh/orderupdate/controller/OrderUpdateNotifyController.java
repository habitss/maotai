package com.keytech.modules.maotai.eh.orderupdate.controller;

import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.orderupdate.dto.OrderUpdateNotifyDTO;
import com.keytech.modules.maotai.eh.orderupdate.service.OrderUpdateNotifyService;
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
 * 工单更新通知接收(API12: E+H → 平台)。
 * 入站接口,走 /api/**(无鉴权,原 X-Api-Token 鉴权已移除,靠 Shiro 放行 anon)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Tag(name = "工单更新通知(API12)")
@RestController
@RequestMapping("/api/Bussiness")
@RequiredArgsConstructor
public class OrderUpdateNotifyController {

    private final OrderUpdateNotifyService orderUpdateNotifyService;
    private final ApiOpLogService opLogService;

    /**
     * 接收 E+H 工单更新通知(API12: E+H → 平台,鹤位/车位更新)。
     * <p>处理:落库通知记录 + 按 OrderNo 找工单,把 batchNo(鹤位)/seatNo(车位)写回 loading_order,
     * 哪个非空更新哪个。工单不存在/未携带更新值也会落通知记录。入站无幂等,重复推会落重复通知记录。
     * @param dto 更新通知(OrderNo 必填定位工单;batchNo 鹤位;seatNo 车位;notifyTime 推送时间)
     * @return 平台通用响应,content 含联动结果(如"已更新 鹤位 batchNo=HP-05")
     */
    @Operation(summary = "接收 E+H 工单更新通知(鹤位/车位)", description = "API12: E+H → 平台。入站无鉴权(原 X-Api-Token 已移除)")
    @PostMapping("/UpdateNotify")
    public ApiResponse updateNotify(@Valid @RequestBody OrderUpdateNotifyDTO dto) {
        var resp = orderUpdateNotifyService.receive(dto);
        opLogService.attachInboundResponse("API12", JSONUtil.toJsonStr(resp));
        return resp;
    }
}
