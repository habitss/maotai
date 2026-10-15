package com.keytech.modules.ems.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.ems.entity.AggregationErrorLog;
import com.keytech.modules.ems.service.IAggregationErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

 /**
 * 聚合异常日志表
 * @author yepeng
 * @since 2025.05.12
 * */
@Tag(name="聚合异常日志表")
@RestController
@RequestMapping("/ems/aggregationErrorLog")
@Slf4j
public class AggregationErrorLogController extends BaseController<IAggregationErrorLogService, AggregationErrorLog> {

	/**
	 *   添加
	 *
	 * @param aggregationErrorLog
	 * @return
	 */
	@AutoLog(value = "聚合异常日志表-添加")
	@Operation(summary="聚合异常日志表-添加")
	@RequiresPermissions("ems:ems_aggregation_error_log:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AggregationErrorLog aggregationErrorLog) {
		baseService.save(aggregationErrorLog);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param aggregationErrorLog
	 * @return
	 */
	@AutoLog(value = "聚合异常日志表-编辑")
	@Operation(summary="聚合异常日志表-编辑")
	@RequiresPermissions("ems:ems_aggregation_error_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AggregationErrorLog aggregationErrorLog) {
		baseService.updateById(aggregationErrorLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "聚合异常日志表-通过id删除")
	@Operation(summary="聚合异常日志表-通过id删除")
	@RequiresPermissions("ems:ems_aggregation_error_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
    * 导出excel
    *
    * @param request
    * @param aggregationErrorLog
    */
    @RequiresPermissions("ems:ems_aggregation_error_log:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AggregationErrorLog aggregationErrorLog) {
        return super.exportXls(request, aggregationErrorLog, AggregationErrorLog.class, "聚合异常日志表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("ems:ems_aggregation_error_log:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AggregationErrorLog.class);
    }

}
