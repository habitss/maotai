package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.service.IKpiReportActualDayValueService;
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
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: KPI实际值
 * @Author: HQ
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Tag(name="KPI实际值")
@RestController
@RequestMapping("/kpi/kpiReportActualDayValue")
@Slf4j
public class KpiReportActualDayValueController extends BaseController<IKpiReportActualDayValueService, KpiReportActualDayValue> {
	@Autowired
	private IKpiReportActualDayValueService kpiReportActualDayValueService;

	 /**
	  * 获取实际值表头
	  * @param kpiReportActualDayValue
	  * @return
	  */
	 @AutoLog(value = "KPI实际值-获取表头")
	 @Operation(summary="KPI实际值-获取表头")
	 @PostMapping(value = "/queryHeader")
	 public Result<?> queryHeader(@RequestBody KpiReportActualDayValue kpiReportActualDayValue) {
		 List<KpiReportCellVo> kpiReportCellList =  kpiReportActualDayValueService.queryHeader(kpiReportActualDayValue);
		 return Result.OK(kpiReportCellList);
	 }

	/**
	 * 获取实际值数据
	 * @param
	 * @return
	 */
	@AutoLog(value = "KPI实际值-获取数据")
	@Operation(summary="KPI实际值-获取数据")
	@PostMapping(value = "/queryData")
	public Result<?> queryData(@RequestBody KpiReportValueDto dto) {
//		if(StrUtil.isBlank(dto.getDept())){
//			dto.setDept(dept);
//		}
		List<?> kpiReportCellList =  kpiReportActualDayValueService.queryData(dto);
		return Result.OK(kpiReportCellList);
	}



	/**
	 *   添加
	 *
	 * @param mapList
	 * @return
	 */
	@AutoLog(value = "KPI实际值-添加")
	@Operation(summary="KPI实际值-添加")
	//@RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:save")
	@PostMapping(value = "/saveData")
	public Result<?> saveData(@RequestBody List<Map<String, Object>> mapList) {
//		kpiReportActualDayValueService.saveData(headerVoList);
		return Result.OK(kpiReportActualDayValueService.saveData(mapList));
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiReportActualDayValue
	 * @return
	 */
	@AutoLog(value = "KPI实际值-编辑")
	@Operation(summary="KPI实际值-编辑")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiReportActualDayValue kpiReportActualDayValue) {
		kpiReportActualDayValueService.updateById(kpiReportActualDayValue);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "KPI实际值-通过id删除")
	@Operation(summary="KPI实际值-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiReportActualDayValueService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "KPI实际值-批量删除")
	@Operation(summary="KPI实际值-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiReportActualDayValueService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="KPI实际值-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiReportActualDayValue> queryById(@RequestParam(name="id",required=true) String id) {
		KpiReportActualDayValue kpiReportActualDayValue = kpiReportActualDayValueService.getById(id);
		if(kpiReportActualDayValue==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiReportActualDayValue);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiReportActualDayValue
    */
    @RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiReportActualDayValue kpiReportActualDayValue) {
        return super.exportXls(request, kpiReportActualDayValue, KpiReportActualDayValue.class, "KPI实际值");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_report_actual_day_value:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, KpiReportActualDayValue.class);
    }

}
