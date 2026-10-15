package com.keytech.modules.maotai.eh.ehLoading.controller;

import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingCloseService;
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
 * 工单结束推送(对接 E+H 装卸车系统)。API19
 * 手动入口,联调/测试用:直接把 CloseRequest 推给 E+H /api/Bussiness/Close(不写 loading_order 工单表;loading_push_log 仍记)。
 * 与 API04 的 LoadingPushController 对称(一个 Create、一个 Close)。出站内部触发,走 /maotai/** Shiro JWT 鉴权。
 *
 * @author maotai
 * @since 2026-07-23
 */
@Tag(name = "工单结束推送(API19)")
@RestController
@RequestMapping("/maotai/loadingClose")
@Slf4j
@RequiredArgsConstructor
public class LoadingCloseController {

    private final LoadingCloseService loadingCloseService;

    /**
     * 手动推送工单结束到 E+H(不写 loading_order 工单表;loading_push_log 仍记)。
     * <p>直接把 CloseRequest 推给 E+H;出站容错:超时/异常返回 fail 不抛;token 由 EhLoadingClient 自动管理。
     * @param close 结束请求(busiType/OrderNo/CarNo/Modifyor/ModifyTime/ABV;不传 CloseType)
     * @return E+H 响应(code 1=成功/0=失败 + content)
     */
    @AutoLog(value = "工单结束-推送E+H")
    @Operation(summary = "推送工单结束到 E+H",
            description = "调用 E+H /api/Bussiness/Close;内部自动取/缓存 token")
    @PostMapping("/close")
    public Result<EhResponse> close(@Valid @RequestBody CloseRequest close) {
        EhResponse resp = loadingCloseService.closeOrder(close);
        if (resp.isSuccess()) {
            return Result.OK("结束成功", resp);
        }
        return Result.error("结束失败: " + resp.getContent());
    }
}
