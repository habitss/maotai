package com.keytech.modules.oauth2.controller.openapi;

import com.keytech.modules.oauth2.annotation.RequireScope;
import com.keytech.modules.oauth2.context.OAuthClientContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方开放数据接口示例。
 * 实际接入时：
 *   1. 新增更多 Controller / 方法，均以 /openapi/v1 为前缀，会被 OAuth2Config 自动纳入鉴权；
 *   2. 每个方法用 @RequireScope("xxx") 声明所需权限；
 *   3. 内部转调现有业务service，返回值用专门的对外DTO做字段裁剪，不要直接暴露内部entity；
 *   4. 涉及租户维度的数据，用 OAuthClientContext.get().getTenantId() 过滤，不要信任第三方传入的租户参数。
 * @author claude
 * @since 2026.08.19
 */
@Tag(name = "第三方开放数据接口(示例)")
@RestController
@RequestMapping("/openapi/v1")
@Slf4j
@RequiredArgsConstructor
public class OpenApiDemoController {

    @RequireScope("data:read")
    @Operation(summary = "示例：查询某日KPI数据")
    @GetMapping("/kpi/daily")
    public Result<Object> dailyKpi(@RequestParam String date) {
        OAuthClientContext.Principal principal = OAuthClientContext.get();
        log.info("openapi调用, clientId={}, tenantId={}, date={}", principal.getClientId(), principal.getTenantId(), date);
        // TODO: 替换为真实业务查询，例如：
        // List<XxxKpiOpenApiDTO> data = xxxKpiService.queryOpenApiDailyKpi(principal.getTenantId(), date);
        // return Result.OK(data);
        return Result.OK("示例接口，请替换为真实业务逻辑");
    }
}
