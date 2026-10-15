package com.keytech.modules.home.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.home.service.IAppService;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: APP查询接口
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Tag(name="App查询接口")
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {
	@Autowired
	private IAppService appService;
	@Autowired
	private IKpiReportActualWeekValueService kpiReportActualWeekValueService;
	@Autowired
	private IKpiInfoService kpiInfoService;

	/**
	 * App首页-统计看板
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "App首页-统计看板")
	@Operation(summary="App首页-统计看板")
	@PostMapping(value = "/workbenches/st")
	public Result<?> workbenchesSt(@RequestBody WSActionStatisticsDto dto) {
		return Result.OK(appService.workbenchesSt(dto));
	}

	/**
	 * 分页列表查询
	 *
	 * @param dto
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "App-异常清单")
	@Operation(summary="App-异常清单")
	@PostMapping(value = "/exception/findExcTaskList")
	public Result<IPage<PiAbnormalAnalysis>> findExcTaskList(@RequestBody PiAbnormalAnalysis dto,
													  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
													  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
													  @RequestParam(name = "orderField",required = false) String orderField,
													  @RequestParam(name = "orderType",required = false) String orderType,
													  @RequestParam(name = "selectFields",required = false) String selectFields,
													  HttpServletRequest req) {
		//QueryWrapper<PiAbnormalAnalysis> queryWrapper = QueryGenerator.initQueryWrapper(dto, req.getParameterMap());
		Page<PiAbnormalAnalysis> page = new Page<PiAbnormalAnalysis>(pageNo, pageSize);
		IPage<PiAbnormalAnalysis> pageList = appService.findExcTaskList(page, dto, req);
		return Result.OK(pageList);
	}

	/**
	 * App-树形结构
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "App首页-树形结构")
	@Operation(summary="App首页-树形结构")
	@GetMapping(value = "/kpi/kpiTree")
	public Result<?> kpiTree(KpiTypeDto dto) {
		return Result.OK(kpiInfoService.kpiTree(dto));
	}

	/**
	 * App-kpi月报/周报表头
	 */
	@AutoLog(value = "App-kpi月报/周报表头")
	@Operation(summary="App-kpi月报/周报表头")
	@PostMapping(value = "/kpi/queryKpiReportHeader")
	public Result<?> queryKpiReportHeader(@RequestBody KpiReportValueDto dto) {
		return Result.OK(kpiReportActualWeekValueService.queryKpiReportHeader(dto));
	}

	/**
	 * App-KPI数据
	 */
	@AutoLog(value = "App-KPI数据")
	@Operation(summary="App-KPI数据")
	@PostMapping(value = "/kpi/listKpiReportData")
	public Result<?> listKpiReportData(@RequestBody KpiReportValueDto dto) {
		if(StrUtil.isBlank(dto.getKpiCode())){
			dto.setKpiCode(dto.getTypeId());
			dto.setTypeId("");
		}
		return Result.OK(kpiReportActualWeekValueService.listKpiReportData(dto));
	}


	/**
	 * App-PI报表
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "App首页-PI报表")
	@Operation(summary="App首页-PI报表")
	@GetMapping(value = "/pi/queryPiTable")
	public Result<?> queryPiTable(PiCustomizeTable dto) {
		return Result.OK(appService.piTable(dto));
	}

	/**
	 * App-PI报表
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "App首页-PI")
	@Operation(summary="App首页-PI")
	@GetMapping(value = "/pi/queryPiList")
	public Result<?> queryPiList(PiCustomizeTableField dto) {
		return Result.OK(appService.piList(dto));
	}

}
