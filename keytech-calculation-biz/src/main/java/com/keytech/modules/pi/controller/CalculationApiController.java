package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.calculation.CalculationBaseApi;
import com.keytech.modules.pi.service.IPiAdapterAlgorithmService;
import com.keytech.modules.pi.service.IPiComputationModelService;
import com.keytech.modules.pi.service.IPiStandardDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import com.keytech.modules.pi.service.IPiStepService;
import com.keytech.modules.pi.entity.PiStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 计算程序对外提供API接口
 * @Author: 敖磊
 * @Date:   2024-11-27
 * @Version: V1.0
 */
@Tag(name="计算程序对外提供API接口")
@RestController
@RequestMapping("/calculation")
@Slf4j
public class CalculationApiController extends BaseController<IPiStepService,PiStep> {

    private static final Logger logger = LoggerFactory.getLogger(CalculationApiController.class);

    @Autowired
    private CalculationBaseApi calculationBaseApi;
    @Autowired
    private IPiStandardDataService piStandardDataService;
    @Autowired
    private IPiComputationModelService piComputationModelService;
    @Autowired
    private IPiAdapterAlgorithmService piAdapterAlgorithmService;


    /**
     * PI计算定时计算
     * parameterObject 任务id
     */
    @AutoLog(value = "PI计算定时计算")
    @Operation(summary = "PI计算定时计算", description = "PI计算定时计算")
    @PostMapping(value = "/piSignal")
    public Result<String> piCalculateSignal(@RequestBody Map map) {
        logger.info("PI计算调用成功---ok-"+map.get("parameterObject")+"");
        calculationBaseApi.piCalculateSignal(map.get("parameterObject")+"");
        logger.info("PI计算执行---ok,");
        return Result.OK("调用成功！");
    }

    /**
     * 红黄区计算定时计算
     * parameterObject 任务id
     */
    @AutoLog(value = "红黄区计算定时计算")
    @Operation(summary = "红黄区计算定时计算", description = "红黄区计算定时计算")
    @PostMapping(value = "/startStandardCalculate")
    public Result<String> startStandardCalculate(@RequestBody Map map) {
        logger.info("红黄区计算调用成功---ok-"+map.get("parameterObject")+"");
        piStandardDataService.startStandardCalculate();
        logger.info("红黄区计算执行---ok,");
        return Result.OK("调用成功！");
    }

    /**
     * PI定时重算
     * parameterObject 任务id
     */
    @AutoLog(value = "PI定时重算")
    @Operation(summary = "PI定时重算", description = "PI定时重算")
    @PostMapping(value = "/automaticRePiCalculateSignal")
    public Result<String> automaticRePiCalculateSignal(@RequestBody Map map) {
        logger.info("PI定时重算调用成功---ok-"+map.get("parameterObject")+"");
        piComputationModelService.automaticRePiCalculateSignal(map.get("parameterObject")+"");
        logger.info("PI定时重算执行---ok,");

        logger.info("PI Adapter定时重算调用成功---ok-"+map.get("parameterObject")+"");
        piAdapterAlgorithmService.taskRecalculate(map.get("parameterObject")+"");
        logger.info("PI Adapter定时重算执行---ok,");

        logger.info("PI Ra定时重算调用成功---ok-"+map.get("parameterObject")+"");
        piAdapterAlgorithmService.taskRecalculateRa(map.get("parameterObject")+"");
        logger.info("PI Ra定时重算执行---ok,");
        return Result.OK("调用成功！");
    }

    /**
     * PI数据定时清理
     */
    @AutoLog(value = "PI数据定时清理")
    @Operation(summary = "PI数据定时清理", description = "PI数据定时清理")
    @PostMapping(value = "/dataCleansing")
    public Result<String> dataCleansing() {
        logger.info("PI数据定时清理调用成功---ok-");
        piComputationModelService.dataCleansing();
        logger.info("PI数据定时清理调用成功---ok,");
        return Result.OK("调用成功！");
    }


    /**
     * 定时任务测试接口
     * @param parameterObject
     * @return
     */
    @AutoLog(value = "接口测试")
    @Operation(summary = "接口测试", description = "接口测试")
    @GetMapping(value = "/taskTest")
    public Result<String> taskTest(String parameterObject) {
        logger.info("调用成功---ok"+parameterObject);
        return Result.OK("调用成功！"+ parameterObject);
    }

}
