package com.keytech.modules.calculation;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @ClassName: CalculationBaseApi
 * @Description: TODO
 * @Author: aolei
 * @Date: 2024/11/26 09:36
 **/
@Component
@FeignClient(contextId = "calculationApi", value = "keytech-calculation-cloud")
//@FeignClient(name = "keytech-calculation" , url = "http://127.0.0.1:8080/keytech-boot")
@ConditionalOnMissingClass("com.keytech.modules.pi.service.impl.CalculationBaseApiServiceImpl")
public interface CalculationBaseApi {

    /**
     * 定时任务计算
     * @param parameterObject
     */
    @Operation(summary = "calculation", description = "calculation")
    @PostMapping(value = "/calculation/piSignal")
    void piCalculateSignal(String parameterObject);

    /**
     * 重算
     * @param tableName 计算表
     * @param listPI 计算的PI
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     */
//    @Operation(summary = "calculation", description = "calculation")
//    @PostMapping(value = "/calculation/rePiSignal")
//    void rePiCalculateSignal(String tableName,List<String> listPI, LocalDateTime startTime, LocalDateTime endTime);

//    /**
//     * 测试
//     */
//    @PostMapping(value = "/calculation/taskTest")
//    void taskTest();
}
