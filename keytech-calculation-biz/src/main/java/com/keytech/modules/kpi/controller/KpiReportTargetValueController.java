package com.keytech.modules.kpi.controller;

import cn.hutool.json.JSONObject;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.dto.KpiDiffAnalysisDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.kpi.vo.KpiReportCellVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: kpi报表目标值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Tag(name="kpi报表目标值")
@RestController
@RequestMapping("/kpi/kpiReportTargetValue")
@Slf4j
public class KpiReportTargetValueController extends BaseController<IKpiReportTargetValueService, KpiReportTargetValue> {
	@Autowired
	private IKpiReportTargetValueService kpiReportTargetValueService;


	/**
	 * 获取目标值表头
	 * @param kpiReportTargetValue
	 * @return
	 */
	@AutoLog(value = "KPI目标值-获取表头")
	@Operation(summary="KPI目标值-获取表头")
	@PostMapping(value = "/queryHeader")
	public Result<?> queryHeader(@RequestBody KpiReportTargetValue kpiReportTargetValue) {
		List<KpiReportCellVo> kpiReportCellList =  kpiReportTargetValueService.queryHeader(kpiReportTargetValue);
		return Result.OK(kpiReportCellList);
	}

	/**
	 * 获取实际值数据
	 * @param
	 * @return
	 */
	@AutoLog(value = "KPI目标值-获取数据")
	@Operation(summary="KPI目标值-获取数据")
	@PostMapping(value = "/queryData")
	public Result<?> queryData(@RequestBody KpiReportValueDto dto) {
//		if(StrUtil.isBlank(kpiReportTargetValueDto.getDept())){
//			kpiReportTargetValueDto.setDept(dept);
//		}
//		kpiReportTargetValueDto.setVisibleWrite(1);
		//List<?> kpiReportCellList =  kpiReportTargetValueService.queryData(dto);
		return Result.OK(kpiReportTargetValueService.queryData(dto));
	}



	/**
	 *   添加
	 *
	 * @param kpiReportValueDto
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-保存")
	@Operation(summary="kpi报表目标值-保存")
//	@RequiresPermissions("kpi:mes3_kpi_report_target_value:add")
	@PostMapping(value = "/saveData")
//	public Result<?> saveData(@RequestBody List<Map<String, Object>> headerVoList) {
	public Result<?> saveData(@RequestBody KpiReportValueDto kpiReportValueDto) {
		return Result.OK(kpiReportTargetValueService.saveData(kpiReportValueDto));
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiReportTargetValue
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-编辑")
	@Operation(summary="kpi报表目标值-编辑")
	@RequiresPermissions("kpi:mes3_kpi_report_target_value:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiReportTargetValue kpiReportTargetValue) {
		kpiReportTargetValueService.updateById(kpiReportTargetValue);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-通过id删除")
	@Operation(summary="kpi报表目标值-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_report_target_value:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiReportTargetValueService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-批量删除")
	@Operation(summary="kpi报表目标值-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_report_target_value:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiReportTargetValueService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="kpi报表目标值-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiReportTargetValue> queryById(@RequestParam(name="id",required=true) String id) {
		KpiReportTargetValue kpiReportTargetValue = kpiReportTargetValueService.getById(id);
		if(kpiReportTargetValue==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiReportTargetValue);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiReportTargetValue
    */
    @RequiresPermissions("kpi:mes3_kpi_report_target_value:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiReportTargetValue kpiReportTargetValue) {
        return super.exportXls(request, kpiReportTargetValue, KpiReportTargetValue.class, "kpi报表目标值");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_report_target_value:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, KpiReportTargetValue.class);
    }


	/**
	 *   kpi报表目标值-差异分析-目标值调整-数据查询
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-差异分析-目标值调整-数据查询")
	@Operation(summary="kpi报表目标值-差异分析-目标值调整-数据查询")
	@PostMapping(value = "/monthTracking")
	public Result<?> monthTracking(@RequestBody KpiDiffAnalysisDto dto) {
		return Result.OK(kpiReportTargetValueService.monthTracking(dto));
	}
	/**
	 *   kpi报表目标值-差异分析-目标值调整-数据保存
	 * @param json
	 * @return
	 */
	@AutoLog(value = "kpi报表目标值-差异分析-目标值调整-数据保存")
	@Operation(summary="kpi报表目标值-差异分析-目标值调整-数据保存")
	@PostMapping(value = "/monthTrackingUpdate")
	public Result<?> monthTrackingUpdate(@RequestBody JSONObject json) {
		kpiReportTargetValueService.monthTrackingUpdate(json);
		return Result.OK("修改成功");
	}
}
