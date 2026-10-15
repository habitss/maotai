package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiThirdPlayData;
import com.keytech.modules.kpi.service.IKpiThirdPlayDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.controller.CalculationApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 第三方接口-维护KPI
 * @Author: jeecg-boot
 * @Date:   2025-03-13
 * @Version: V1.0
 */
@Tag(name="第三方接口-维护KPI")
@RestController
@RequestMapping("/kpi/kpiThirdPlayData")
@Slf4j
public class KpiThirdPlayDataController extends BaseController<IKpiThirdPlayDataService, KpiThirdPlayData> {

	private static final Logger logger = LoggerFactory.getLogger(KpiThirdPlayDataController.class);
	@Autowired
	private IKpiThirdPlayDataService kpiThirdPlayDataService;


	 /**
	  * 定时任务接口-维护KPI数据抽取
	  */
	@AutoLog(value = "定时任务接口-维护KPI数据抽取")
	@Operation(summary = "定时任务接口-维护KPI数据抽取", description = "定时任务接口-维护KPI数据抽取")
	@PostMapping(value = "/synchronizeMonthWeekValueJob")
	public Result<String> synchronizeMonthWeekValueJob(@RequestBody Map map) {
		logger.info("维护KPI数据抽取---ok");
		kpiThirdPlayDataService.synchronizeMonthWeekValueJob();
		logger.info("维护KPI数据抽取执行---ok,");
		return Result.OK("调用成功！");
	}


	/**
	 * 定时任务接口-不达标KPI 5why分析推送
	 */
	@AutoLog(value = "定时任务接口-不达标KPI5why分析推送")
	@Operation(summary = "定时任务接口-不达标KPI5why分析推送", description = "定时任务接口-不达标KPI5why分析推送")
	@PostMapping(value = "/notStandardKpiFwhyJob")
	public Result<String> notStandardKpiFwhyJob(@RequestBody Map map) {
		logger.info("不达标KPI5why分析推送执行---ok");
		kpiThirdPlayDataService.notStandardKpiFwhyJob();
		logger.info("不达标KPI5why分析推送---ok,");
		return Result.OK("调用成功！");
	}



	/**
	 * 不达标KPI推送
	 */
	@AutoLog(value = "不达标KPI推送")
	@Operation(summary = "不达标KPI推送", description = "不达标KPI推送")
	@PostMapping(value = "/kpiNotStandard")
	public Result<String> kpiNotStandard(@RequestBody Map map) {
		logger.info("不达标KPI推送---ok");
		Long analyseId = Long.parseLong(map.get("analyseId")+"");
		List<Long> list = new ArrayList<>();
		list.add(analyseId);
		kpiThirdPlayDataService.kpiNotStandard(list);
		logger.info("不达标KPI推送执行---ok,");
		return Result.OK("调用成功！");
	}



}
