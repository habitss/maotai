package com.keytech.modules.kpi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.entity.KpiReportExceptionLog;
import com.keytech.modules.kpi.service.IKpiReportExceptionLogService;
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

import java.util.Arrays;
import java.util.List;

/**
 * @Description: KPI数据修改
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="KPI数据修改")
@RestController
@RequestMapping("/kpi/kpiReportExceptionLog")
@Slf4j
public class KpiReportExceptionLogController extends BaseController<IKpiReportExceptionLogService, KpiReportExceptionLog> {
	@Autowired
	private IKpiReportExceptionLogService kpiReportExceptionLogService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiReportExceptionLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="KPI数据修改-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiReportExceptionLog>> queryPageList(KpiReportExceptionLog kpiReportExceptionLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiReportExceptionLog> queryWrapper = QueryGenerator.initQueryWrapper(kpiReportExceptionLog, req.getParameterMap());
		Page<KpiReportExceptionLog> page = new Page<KpiReportExceptionLog>(pageNo, pageSize);
		IPage<KpiReportExceptionLog> pageList = kpiReportExceptionLogService.queryKpiExcList(kpiReportExceptionLog, page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param kpiReportExceptionLogs
	 * @return
	 */
	@AutoLog(value = "KPI数据修改-添加")
	@Operation(summary="KPI数据修改-添加")
	@RequiresPermissions("kpi:mes3_kpi_report_exception_log:add")
	@PostMapping(value = "/batchAddOrEdit")
	public Result<String> batchAddOrEdit(@RequestBody List<KpiReportExceptionLog> kpiReportExceptionLogs) {
		kpiReportExceptionLogService.batchAddOrEdit(kpiReportExceptionLogs);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiReportExceptionLog
	 * @return
	 */
	@AutoLog(value = "KPI数据修改-编辑")
	@Operation(summary="KPI数据修改-编辑")
	@RequiresPermissions("kpi:mes3_kpi_report_exception_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiReportExceptionLog kpiReportExceptionLog) {
		kpiReportExceptionLogService.updateById(kpiReportExceptionLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "KPI数据修改-通过id删除")
	@Operation(summary="KPI数据修改-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_report_exception_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiReportExceptionLogService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "KPI数据修改-批量删除")
	@Operation(summary="KPI数据修改-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_report_exception_log:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiReportExceptionLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="KPI数据修改-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiReportExceptionLog> queryById(@RequestParam(name="id",required=true) String id) {
		KpiReportExceptionLog kpiReportExceptionLog = kpiReportExceptionLogService.getById(id);
		if(kpiReportExceptionLog==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiReportExceptionLog);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiReportExceptionLog
    */
    @RequiresPermissions("kpi:mes3_kpi_report_exception_log:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiReportExceptionLog kpiReportExceptionLog) {
        return super.exportXls(request, kpiReportExceptionLog, KpiReportExceptionLog.class, "KPI数据修改");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_report_exception_log:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, KpiReportExceptionLog.class);
    }
}
