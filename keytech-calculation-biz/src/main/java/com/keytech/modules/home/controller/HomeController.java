package com.keytech.modules.home.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.home.service.IHomeService;
import com.keytech.modules.home.vo.ExceptionVo;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @Description: 首页统计
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Tag(name="首页统计")
@RestController
@RequestMapping("/home")
@Slf4j
public class HomeController extends BaseController<IHomeService, KpiInfo> {
	@Autowired
	private IHomeService homeService;
	@Autowired
	private IKpiReportActualWeekValueService kpiReportActualWeekValueService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysAnnouncementService sysAnnouncementService;
	@Autowired
	private IKpiInfoService kpiInfoService;

    /**
     * 首页功能统计
	 * @param dto
	 * functionName
	 * 	kpiComplianceRate (工厂KPI达标率)
	 * 	kpiComplianceRank (KPI达标率排名)
	 * 	piComplianceRate  (工厂PI达标率)
	 * 	piComplianceRank  (PI达标率排名)
	 * 	trackPlan         (行动计划)
	 * 	manageCheck       (分层检查)
	 * 	cop               (COP完成率排名)
	 * 	periodPlan        (期间核查完成率排名)
	 * 	workshopStatistics()
	 * @return
	 */
	@AutoLog(value = "首页-首页功能统计")
	@Operation(summary="首页-首页功能统计")
	@PostMapping(value = "/pageHome")
	public Result<?> pageHome(@RequestBody WSActionStatisticsDto dto) {

		Map<String, Function<WSActionStatisticsDto, JSONObject>> functionMap = homeService.getFunctionMap();
		if (!functionMap.containsKey(dto.getFunctionName())) {
			return Result.error("模块名称不存在");
		}
		return Result.OK(functionMap.get(dto.getFunctionName()).apply(dto));
	}


	/**
	 * 重点关注kpi指标
	 * @param dto
	 * functionName
	 * @return
	 */
	@AutoLog(value = "首页-重点关注kpi指标")
	@Operation(summary="首页-重点关注kpi指标")
	@PostMapping(value = "/factoryActionStatisticsC")
	public Result<?> factoryActionStatisticsC(@RequestBody KpiReportValueDto dto) {
		return Result.OK(kpiReportActualWeekValueService.factoryActionStatisticsC(dto));
	}

	/**
	 *
	 */
	@AutoLog(value = "首页-重点关注kpi指标")
	@Operation(summary="首页-重点关注kpi指标")
	@GetMapping(value = "/getDeptName")
	public Result<?> getDeptName(String dept) {
		LambdaUpdateWrapper<SysDepart> updateWrapper = Wrappers.lambdaUpdate(SysDepart.class);
		updateWrapper.eq(SysDepart::getOrgCode, dept);
		List<SysDepart> list = sysDepartService.list(updateWrapper);
		if(list != null && !list.isEmpty()){
			Map<String,String> map = new HashMap<>();
			map.put("name",list.get(0).getDepartName());
			map.put("id",list.get(0).getId());
			return Result.OK(map);
		}
		return Result.OK("");
	}


	/**
	 * 重点关注pi指标
	 * @param dto
	 * functionName
	 * @return
	 */
	@AutoLog(value = "首页-重点关注pi指标")
	@Operation(summary="首页-重点关注pi指标")
	@PostMapping(value = "/factoryActionStatisticsPi")
	public Result<?> factoryActionStatisticsPi(@RequestBody WSActionStatisticsDto dto) {
		String factory = dto.getFactory();
		String startTime = dto.getOpTime();
		String endTime = dto.getEdTime();
		//获取重点PI




		return Result.OK(homeService.factoryActionStatisticsPi(dto));
	}

	/**
	 * 获取通知公告
	 * @param type
	 * functionName
	 * @return
	 */
	@AutoLog(value = "首页-重点关注pi指标")
	@Operation(summary="首页-重点关注pi指标")
	@GetMapping(value = "/getAnnountCement")
	public Result<?> getAnnountCement(@RequestParam(name = "type", required = false) Integer type) {
		List<SysAnnouncement> list = sysAnnouncementService.list(new LambdaQueryWrapper<SysAnnouncement>()
				.eq(Objects.nonNull(type), SysAnnouncement::getBusId, type)
				.isNotNull(SysAnnouncement::getBusId).orderByDesc(SysAnnouncement::getBusId, SysAnnouncement::getUpdateTime));
		return Result.OK(list);
	}
}
