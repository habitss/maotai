package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiExceptionSummary;
import com.keytech.modules.kpi.service.IKpiExceptionSummaryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: KPI异常清单
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Tag(name="KPI异常清单")
@RestController
@RequestMapping("/kpi/kpiExceptionSummary")
@Slf4j
public class KpiExceptionSummaryController extends BaseController<IKpiExceptionSummaryService, KpiExceptionSummary> {
	private static final Logger logger = LoggerFactory.getLogger(KpiThirdPlayDataController.class);
	@Autowired
	private IKpiExceptionSummaryService kpiExceptionSummaryService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiExceptionSummary
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="KPI异常清单-分页列表查询")
	@GetMapping(value = "/queryPageData")
	public Result<IPage<KpiExceptionSummary>> queryPageData(KpiExceptionSummary kpiExceptionSummary,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<KpiExceptionSummary> page = new Page<KpiExceptionSummary>(pageNo, pageSize);
		IPage<KpiExceptionSummary> pageList = kpiExceptionSummaryService.queryPageList(page, kpiExceptionSummary);
		return Result.OK(pageList);
	}


	 /**
	  * 统计
	  *
	  * @param kpiExceptionSummary
	  * @return
	  */
	 @Operation(summary="KPI异常清单-统计")
	 @GetMapping(value = "/percent")
	 public Result<?> percent(KpiExceptionSummary kpiExceptionSummary) {
		 return Result.OK(kpiExceptionSummaryService.percent(kpiExceptionSummary));
	 }

	 /**
	  * 定时任务接口-KPI异常清单
	  */
	@AutoLog(value = "定时任务接口-生成KPI异常清单")
	@Operation(summary = "定时任务接口-KPI异常清单", description = "定时任务接口-KPI异常清单")
	@PostMapping(value = "/synchronizeMonthWeekValueJob")
	public Result<String> synchronizeMonthWeekValueJob(@RequestBody Map map) {
		logger.info("生成KPI异常清单---ok");
		kpiExceptionSummaryService.synchronizeMonthWeekValueJob();
		logger.info("生成KPI异常清单---ok");
		return Result.OK("操作成功");
	}

	/**
	 * 确认(ok/nok)
	 * @param kpiExceptionSummary
	 * @return
	 */
	@Operation(summary="确认(ok/nok)")
	@GetMapping(value = "/confirm")
	public Result<?> confirm(KpiExceptionSummary kpiExceptionSummary) {
		try {
			kpiExceptionSummaryService.updateById(kpiExceptionSummary);
		} catch (Exception e) {
			return Result.error("操作失败");
		}
		return Result.OK("操作成功");
	}
 }
