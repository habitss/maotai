package com.keytech.modules.kpi.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.kpi.util.DateUtils;
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

import java.util.*;

/**
 * @Description: kpi报表实际值周表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Tag(name="kpi报表实际值周表")
@RestController
@RequestMapping("/kpi/kpiReportActualWeekValue")
@Slf4j
public class KpiReportActualWeekValueController extends BaseController<IKpiReportActualWeekValueService, KpiReportActualWeekValue> {

	private static final Logger logger = LoggerFactory.getLogger(KpiReportActualWeekValueController.class);
	@Autowired
	private IKpiReportActualWeekValueService kpiReportActualWeekValueService;
	

	/**
	 *  kpi 月报/周报表头
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "kpi月报/周报表头")
	@Operation(summary="kpi 月报/周报表头")
	@PostMapping(value = "/queryKpiReportHeader")
	public Result<?> queryKpiReportHeader(@RequestBody KpiReportValueDto dto) {
		return Result.OK(kpiReportActualWeekValueService.queryKpiReportHeader(dto));
	}

	 /**
	  *  kpi 月报/周报数据
	  * @param dto
	  * @return
	  */
	 @AutoLog(value = "kpi月报/周报数据")
	 @Operation(summary="kpi 月报/周报数据")
	 @PostMapping(value = "/listKpiReportData")
	 public Result<?> listKpiReportData(@RequestBody KpiReportValueDto dto) {
		 return Result.OK(kpiReportActualWeekValueService.listKpiReportData(dto));
	 }


    /**
    * 导出excel
    *
    * @param request
    * @param kpiReportActualWeekValue
    */
    @RequiresPermissions("kpi:mes3_kpi_report_actual_week_value:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiReportActualWeekValue kpiReportActualWeekValue) {
        return super.exportXls(request, kpiReportActualWeekValue, KpiReportActualWeekValue.class, "kpi报表实际值周表");
    }


	//重复算-retryReportData
	@AutoLog(value = "kpi颜色重算")
	@Operation(summary="kpi颜色重算")
	@PostMapping(value = "/retryReportData")
	public Result<?> retryReportData(@RequestBody KpiReportValueDto dto) {
		kpiReportActualWeekValueService.retryReportData(dto);
		return Result.OK("操作成功!");
	}


	/**
	 * 周月报-重算
	 * 定时任务-KPI重算(红区计算)
	 * year 年
	 * @param map
	 * @return
	 */
	@AutoLog(value = "定时任务接口-kpi颜色重算")
	@Operation(summary="定时任务接口-kpi颜色重算")
	@PostMapping(value = "/retryReportDataJob")
	public Result<?> retryReportDataJob(@RequestBody Map map) {
		logger.info("KPI颜色重算---ok");
		KpiReportValueDto dto = new KpiReportValueDto();
		//年 (2025)
		String year = map.get("year")+"";
		if(StrUtil.isBlank(year) || "null".equals(year)){
			//默认当前年
			year = DateUtils.formatDateByPattern(new Date(),DateUtils.YEAR_FORMAT);
		}
		//年月 (2025-03-16)
		String monthStr = map.get("months")+"";
		List<String> months = new ArrayList<>();
		if(StrUtil.isNotBlank(monthStr) && !"null".equals(monthStr)){
			months = Arrays.asList(monthStr.split(","));
		}else{
			//默认 当前年月
			String month = DateUtils.formatDateByPattern(new Date(),DateUtils.YEAR_MONTH_FORMAT);
			months.add(month);
			dto.setMonth(month);
		}

		dto.setYear(Integer.parseInt(year));
		dto.setMonths(months);
		kpiReportActualWeekValueService.retryReportData(dto);
		logger.info("KPI颜色重算---ok");
		return Result.OK("操作成功");
	}

	/**
	 * KPI 趋势表头
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "KPI 趋势表头")
	@Operation(summary="KPI 趋势表头")
	@PostMapping(value = "/trendCurveHeader")
	public Result<?> trendCurveHeader(@RequestBody KpiReportValueDto dto) {
		return Result.OK(kpiReportActualWeekValueService.trendCurveHeader(dto));
	}

	/**
	 * KPI 趋势数据
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "KPI 趋势数据")
	@Operation(summary="KPI 趋势数据")
	@PostMapping(value = "/trendCurveData")
	public Result<?> trendCurveData(@RequestBody KpiReportValueDto dto) {
		return Result.OK(kpiReportActualWeekValueService.trendCurveData(dto));
	}

}
