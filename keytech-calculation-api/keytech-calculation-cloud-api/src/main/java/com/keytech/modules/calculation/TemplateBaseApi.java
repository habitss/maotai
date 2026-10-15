package com.keytech.modules.calculation;


import com.keytech.modules.pi.entity.PiBasePoint;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @ClassName: PlanBaseApi
 * @Description: TODO
 * @Author: Jun
 * @Date: 2024/1/14 11:56
 **/
@Component
@FeignClient(contextId = "sysTemplateRemoteApi", value = "keytech-template")
@ConditionalOnMissingClass("com.keytech.modules.plan.service.impl.PlanBaseApiServiceImpl")
public interface TemplateBaseApi {
    @Operation(summary = "template", description = "template")
    @PostMapping(value = "/template/user")
    List<Object> queryDoingCellPlan(List<Object> cellPlans);
}
