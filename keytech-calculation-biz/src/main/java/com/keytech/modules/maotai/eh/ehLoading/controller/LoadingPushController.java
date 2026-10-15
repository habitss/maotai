package com.keytech.modules.maotai.eh.ehLoading.controller;

import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingPushService;
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
 * 装卸车计划推送(对接 E+H 装卸车系统)。API04
 * 文档建议为 MES 设计"手动推送"机制作为自动流程的备份,此即手动入口。
 * 出站内部触发,走 /maotai/** Shiro JWT 鉴权。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Tag(name = "装卸车计划推送")
@RestController
@RequestMapping("/maotai/loading")
@Slf4j
@RequiredArgsConstructor
public class LoadingPushController {

    private final LoadingPushService loadingPushService;

    /**
     * 手动推送装卸车计划到 E+H(不写 loading_order 工单表;loading_push_log 仍记)。
     * <p>直接把 DTO 推给 E+H,不写工单表;出站容错:超时/异常返回 fail 不抛;token 由 EhLoadingClient 自动管理。
     * @param plan 装卸车计划(@Valid 校验 9 必填;JSON 字段名按 PDF 原大小写)
     * @return E+H 响应(code 1=成功/0=失败 + content)
     */
    @AutoLog(value = "装卸车计划-推送E+H")
    @Operation(summary = "推送装卸车计划到 E+H",
            description = "调用 E+H /api/Bussiness/Create 推送单据;内部自动取/缓存 token")
    @PostMapping("/push")
    public Result<EhResponse> push(@Valid @RequestBody LoadingPlanDTO plan) {
        EhResponse resp = loadingPushService.pushPlan(plan);
        if (resp.isSuccess()) {
            return Result.OK("推送成功", resp);
        }
        return Result.error("推送失败: " + resp.getContent());
    }
}
