package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
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
import java.util.Map;

/**
 * @Description: kpi报表实际值月表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Tag(name="kpi报表实际值月表")
@RestController
@RequestMapping("/kpi/kpiReportActualMonthValue")
@Slf4j
public class KpiReportActualMonthValueController extends BaseController<IKpiReportActualMonthValueService, KpiReportActualMonthValue> {
	@Autowired
	private IKpiReportActualMonthValueService kpiReportActualMonthValueService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiReportActualMonthValue
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="kpi报表实际值月表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiReportActualMonthValue>> queryPageList(KpiReportActualMonthValue kpiReportActualMonthValue,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiReportActualMonthValue> queryWrapper = QueryGenerator.initQueryWrapper(kpiReportActualMonthValue, req.getParameterMap());
		Page<KpiReportActualMonthValue> page = new Page<KpiReportActualMonthValue>(pageNo, pageSize);
		IPage<KpiReportActualMonthValue> pageList = kpiReportActualMonthValueService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param map
	 * @return
	 */
	@AutoLog(value = "kpi报表实际值月表-保存")
	@Operation(summary="kpi报表实际值月表-保存")
//	@RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:add")
	@PostMapping(value = "/saveData")
	public Result<String> saveData(@RequestBody Map map) {
		System.out.println(map);
		//kpiReportActualMonthValueService.save(kpiReportActualMonthValue);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiReportActualMonthValue
	 * @return
	 */
	@AutoLog(value = "kpi报表实际值月表-编辑")
	@Operation(summary="kpi报表实际值月表-编辑")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiReportActualMonthValue kpiReportActualMonthValue) {
		kpiReportActualMonthValueService.updateById(kpiReportActualMonthValue);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "kpi报表实际值月表-通过id删除")
	@Operation(summary="kpi报表实际值月表-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiReportActualMonthValueService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "kpi报表实际值月表-批量删除")
	@Operation(summary="kpi报表实际值月表-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiReportActualMonthValueService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="kpi报表实际值月表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiReportActualMonthValue> queryById(@RequestParam(name="id",required=true) String id) {
		KpiReportActualMonthValue kpiReportActualMonthValue = kpiReportActualMonthValueService.getById(id);
		if(kpiReportActualMonthValue==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiReportActualMonthValue);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiReportActualMonthValue
    */
    @RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiReportActualMonthValue kpiReportActualMonthValue) {
        return super.exportXls(request, kpiReportActualMonthValue, KpiReportActualMonthValue.class, "kpi报表实际值月表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_report_actual_month_value:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, KpiReportActualMonthValue.class);
    }

}
