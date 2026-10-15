package com.keytech.modules.exception.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.exception.dto.ExceptionReportVO;
import com.keytech.modules.exception.dto.ExceptionTotalVO;
import com.keytech.modules.exception.dto.ReportViewDTO;
import com.keytech.modules.exception.entity.ExceptionReport;
import com.keytech.modules.exception.service.IExceptionReportService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 异常处理报告
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
@Tag(name="异常处理报告")
@RestController
@RequestMapping("/exception/exceptionReport")
@Slf4j
public class ExceptionReportController extends BaseController<IExceptionReportService, ExceptionReport> {
	@Autowired
	private IExceptionReportService exceptionReportService;

	@Autowired
	private IActionPlanTrackService actionPlanTrackService;

	/**
	 * 分页列表查询
	 *
	 * @param exceptionReport
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="异常处理报告-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ExceptionReport>> queryPageList(ExceptionReport exceptionReport,
														@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														@RequestParam(name = "orderField",required = false) String orderField,
														@RequestParam(name = "orderType",required = false) String orderType,
														@RequestParam(name = "selectFields",required = false) String selectFields,
														HttpServletRequest req) {
		LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
		if(StringUtils.isNotEmpty(exceptionReport.getTimes())){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String[] times = exceptionReport.getTimes().split(",");
            try {
                queryWrapper.between(ExceptionReport::getExceptionTime, sdf.parse(times[0]), sdf.parse(times[1]));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
		queryWrapper.eq(StringUtils.isNotEmpty(exceptionReport.getDeptCode()),ExceptionReport::getDeptCode, exceptionReport.getDeptCode()).eq(StringUtils.isNotEmpty(exceptionReport.getLineCode()),ExceptionReport::getLineCode, exceptionReport.getLineCode()).eq(StringUtils.isNotEmpty(exceptionReport.getTriggerConditions()),ExceptionReport::getTriggerConditions, exceptionReport.getTriggerConditions());
		queryWrapper.orderByDesc(ExceptionReport::getExceptionTime);
		Page<ExceptionReport> page = new Page<>(pageNo, pageSize);
		IPage<ExceptionReport> pageList = exceptionReportService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param exceptionReport
	 * @return
	 */
	@AutoLog(value = "异常处理报告-添加")
	@Operation(summary="异常处理报告-添加")
//	@RequiresPermissions("exception:mes3_exception_report:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ExceptionReport exceptionReport) {
		if (null != exceptionReport.getExceptionTime()) {
			Instant instant = exceptionReport.getExceptionTime().toInstant();
			LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
			int year = localDate.getYear();
			int month = localDate.getMonthValue();
			exceptionReport.setYear(year);
			exceptionReport.setMonth(month);
		}
		if(StringUtils.isEmpty(exceptionReport.getLineCode())){
			exceptionReport.setLineCode(exceptionReport.getDeptCode());
		}
		exceptionReportService.save(exceptionReport);
		actionPlanTrackService.saveOrUpdateAction(exceptionReport.getId(), exceptionReport.getActionPlanTrackList());
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param exceptionReport
	 * @return
	 */
	@AutoLog(value = "异常处理报告-编辑")
	@Operation(summary="异常处理报告-编辑")
//	@RequiresPermissions("exception:mes3_exception_report:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ExceptionReport exceptionReport) {
		exceptionReportService.updateById(exceptionReport);
		actionPlanTrackService.saveOrUpdateAction(exceptionReport.getId(), exceptionReport.getActionPlanTrackList());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "异常处理报告-通过id删除")
	@Operation(summary="异常处理报告-通过id删除")
//	@RequiresPermissions("exception:mes3_exception_report:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		exceptionReportService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "异常处理报告-批量删除")
	@Operation(summary="异常处理报告-批量删除")
//	@RequiresPermissions("exception:mes3_exception_report:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.exceptionReportService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="异常处理报告-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ExceptionReport> queryById(@RequestParam(name="id") String id) {
		ExceptionReport exceptionReport = exceptionReportService.getById(id);
		if(exceptionReport==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(exceptionReport);
	}

	 /**
	  * 通过类型查询
	  *
	  * @param exceptionReport
	  * @return
	  */
	 @Operation(summary="异常处理报告-通过类型统计")
	 @PostMapping(value = "/reportView")
	 public Result<List<ReportViewDTO>> reportView(@RequestBody ExceptionReportVO exceptionReport) {
		 List<ReportViewDTO> list = exceptionReportService.reportView(exceptionReport);
		 return Result.OK(list);
	 }

	@Operation(summary="异常处理报告-统计")
	@PostMapping(value = "/reportStatistics")
	public Result<List<ExceptionTotalVO>> reportStatistics(@RequestBody ExceptionReportVO exceptionReport) {
		List<ExceptionTotalVO> list = exceptionReportService.reportStatistics(exceptionReport);
		return Result.OK(list);
	}


	/**
    * 导出excel
    *
    * @param request
    * @param exceptionReport
    */
    @RequiresPermissions("exception:mes3_exception_report:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ExceptionReport exceptionReport) {
        return super.exportXls(request, exceptionReport, ExceptionReport.class, "异常处理报告");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("exception:mes3_exception_report:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ExceptionReport.class);
    }

}
