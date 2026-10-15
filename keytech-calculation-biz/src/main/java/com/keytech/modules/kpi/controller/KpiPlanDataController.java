package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiPlanData;
import com.keytech.modules.kpi.service.IKpiPlanDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.kpi.vo.ControlConfigurationVo;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: KPI树-基础数据
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
@Tag(name="KPI树-基础数据")
@RestController
@RequestMapping("/kpi/kpiPlanData")
@Slf4j
public class KpiPlanDataController extends BaseController<IKpiPlanDataService, KpiPlanData> {
	@Autowired
	private IKpiPlanDataService kpiPlanDataService;

	//batchAddOrEdit
	@AutoLog(value = "KPI树基础数据-存储")
	@Operation(summary="KPI树基础数据-存储")
	@PostMapping(value = "/batchAddOrEdit")
	public Result<?> batchAddOrEdit(@RequestBody List<ControlConfigurationVo> controlConfigurationVos) {
		if (CollectionUtils.isEmpty(controlConfigurationVos)) return Result.error("入参异常");
		List<KpiPlanData> list =  kpiPlanDataService.batchAddOrEdit(controlConfigurationVos);
		return Result.OK("添加成功！", list);
	}
	

}
