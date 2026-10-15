package com.keytech.modules.maotai.mes.controller;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.service.WorkOrderListQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 勾储MES 工单查询(API02: 平台 → 勾储MES, 出站)。
 * 平台内部端点 /maotai/mes/**(Shiro JWT);按员工工号查(empNo 动态传参),
 * 响应工单解析落库 + 同工单号重复校验(一致幂等/不一致记异常表+返回异常+不落库)。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Tag(name = "勾储MES 工单查询(API02)")
@RestController
@RequestMapping("/maotai/mes")
@Slf4j
@RequiredArgsConstructor
public class WorkOrderListQueryController {

    private final WorkOrderListQueryService workOrderListQueryService;

    /**
     * 工单查询(API02:平台 → 勾储MES /GZMES/workOrders/list)。
     * <p>按员工工号查,响应工单解析落库 + 重复校验;鉴权 password(AES-ECB) 取 gzmes 配置,密钥待联调补。
     * @param empNo 员工工号(必填,动态查询条件)
     * @param rqi   请求流水Id(可选,空自动生成)
     * @return Result&lt;MesResponse&gt;(resultCode 1=成功 0=失败;rqi 回传;resultDesp 含落库/不一致汇总)
     */
    @AutoLog(value = "勾储MES-工单查询")
    @Operation(summary = "工单查询(转发勾储MES,按工号)",
            description = "API02: 平台 → 勾储MES /GZMES/workOrders/list。按 empNo 查询,响应工单解析落库+重复校验")
    @PostMapping("/workOrder/list")
    public Result<MesResponse> list(@RequestParam("empNo") String empNo,
                                    @RequestParam(value = "rqi", required = false) String rqi) {
        MesResponse resp = workOrderListQueryService.queryWorkOrderList(rqi, empNo);
        if (resp.isSuccess()) {
            return Result.OK("查询成功", resp);
        }
        return Result.error("查询失败: " + resp.getResultDesp());
    }
}
