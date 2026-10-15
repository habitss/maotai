package com.keytech.modules.line.efficiency.controller;

import com.keytech.base.controller.BaseController;
import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.keytech.modules.line.efficiency.service.IDbScaleweightService;
import com.keytech.modules.pi.controller.PiSistarRtBatchesController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description: 地磅数据
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Tag(name="地磅数据")
@RestController
@RequestMapping("/line/efficiency/dbScaleweight")
@Slf4j
public class DbScaleweightController extends BaseController<IDbScaleweightService, DbScaleweight> {
	@Autowired
	private IDbScaleweightService dbScaleweightService;



	/**
	 * 定时任务接口-地磅数据抽取
	 */
	@AutoLog(value = "定时任务接口-地磅数据抽取")
	@Operation(summary = "定时任务接口-地磅数据抽取", description = "定时任务接口-地磅数据抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		log.info("地磅数据抽取开始执行---ok");
		dbScaleweightService.extractData();
		log.info("地磅数据抽取开始执行完成---ok,");
		return Result.OK("调用成功！");
	}

}
