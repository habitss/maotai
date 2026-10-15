package com.keytech.modules.maotai.eh.ehLoading.controller;

import com.keytech.modules.maotai.eh.ehLoading.dto.BusinessDetailsRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.service.BusinessDetailsService;
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
 * 查询业务命令(对接 E+H 装卸车系统)。API16
 * 出站内部手动入口,走 /maotai/** Shiro JWT 鉴权;联调/测试/兜底用:直接把请求推给 E+H /api/Bussiness/Details,
 * 查 E+H 内未归档的装卸车单据(含实装量、status 等)。不落库。
 *
 * @author maotai
 * @since 2026-07-24
 */
@Tag(name = "查询业务命令(API16)")
@RestController
@RequestMapping("/maotai/businessDetails")
@Slf4j
@RequiredArgsConstructor
public class BusinessDetailsController {

    private final BusinessDetailsService businessDetailsService;

    /**
     * 查询 E+H 业务单据详情(不落库,联调/测试/兜底用)。
     * <p>直接把请求推给 E+H,不写工单表;token 由 EhLoadingClient 自动管理,出站超时/异常降级返回 fail(不抛 500)。
     * @param req 查询请求(busiType 默认 2-卸车;OrderNo/CarNo 必填)
     * @return E+H 响应(code 1=成功/0=失败 + content;成功时 data 含 orderNo/carNo/actualWeight/loadStartTime/loadEndTime/status/feedbackTime)
     */
    @AutoLog(value = "查询业务命令-查询E+H")
    @Operation(summary = "查询 E+H 业务单据详情",
            description = "调用 E+H /api/Bussiness/Details 查未归档单据;内部自动取/缓存 token")
    @PostMapping("/query")
    public Result<EhResponse> query(@Valid @RequestBody BusinessDetailsRequest req) {
        EhResponse resp = businessDetailsService.queryBusinessDetails(req);
        if (resp.isSuccess()) {
            return Result.OK("查询成功", resp);
        }
        return Result.error("查询失败: " + resp.getContent());
    }
}
