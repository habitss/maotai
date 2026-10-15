package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiThirdQualityData;
import com.keytech.modules.kpi.service.IKpiThirdQualityDataService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: KPI第三方接口-质量KPI
 * @Author: jeecg-boot
 * @Date:   2025-08-18
 * @Version: V1.0
 */
@Tag(name="KPI第三方接口-质量KPI")
@RestController
@RequestMapping("/kpi/kpiThirdQualityData")
@Slf4j
public class KpiThirdQualityDataController extends BaseController<IKpiThirdQualityDataService, KpiThirdQualityData> {
	@Autowired
	private IKpiThirdQualityDataService kpiThirdQualityDataService;


	/**
	 *  质量KPI数据接收
	 * @param kpiThirdQualityDataList
	 * @return
	 */
	@AutoLog(value = "KPI第三方接口-质量KPI-数据接收")
	@Operation(summary="KPI第三方接口-质量KPI-数据接收")
	@PostMapping(value = "/qualityData")
	public Result<String> qualityData(@RequestBody List<KpiThirdQualityData> kpiThirdQualityDataList) {
		if(kpiThirdQualityDataList != null && !kpiThirdQualityDataList.isEmpty()){
			kpiThirdQualityDataService.qualityData(kpiThirdQualityDataList);
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}


}
