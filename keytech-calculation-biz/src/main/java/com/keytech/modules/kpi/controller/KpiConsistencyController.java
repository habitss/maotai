package com.keytech.modules.kpi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.dto.ConsistencyDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiConsistencyReport;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.service.IKpiConsistencyReportService;
import com.keytech.modules.kpi.service.IKpiInfoService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Tag(name="KPI清单")
@RestController
@RequestMapping("/api/kpi/consistency")
@Slf4j
public class KpiConsistencyController extends BaseController<IKpiConsistencyReportService, KpiConsistencyReport> {

	@Autowired
	private IKpiConsistencyReportService kpiConsistencyReportService;


	/**
	 * 分页列表查询
	 *
	 * @param kpiConsistencyReport
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="一致KPI数据-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiConsistencyReport>> queryPageList(KpiConsistencyReport kpiConsistencyReport,
															 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															 @RequestParam(name = "orderField",required = false) String orderField,
															 @RequestParam(name = "orderType",required = false) String orderType,
															 @RequestParam(name = "selectFields",required = false) String selectFields,
															 HttpServletRequest req) {
		QueryWrapper<KpiConsistencyReport> queryWrapper = QueryGenerator.initQueryWrapper(kpiConsistencyReport, req.getParameterMap());
		Page<KpiConsistencyReport> page = new Page<KpiConsistencyReport>(pageNo, pageSize);
		IPage<KpiConsistencyReport> pageList = kpiConsistencyReportService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 获取一致性指数数据
	 * @param consistencyDto
	 * @return
	 */
	@Operation(summary="获取一致性指数数据")
	@GetMapping(value = "/report")
	public Result<List<ConsistencyDto>> queryPageList(ConsistencyDto consistencyDto) {
		return Result.OK("成功",kpiConsistencyReportService.queryPageList(consistencyDto));
	}
 }
