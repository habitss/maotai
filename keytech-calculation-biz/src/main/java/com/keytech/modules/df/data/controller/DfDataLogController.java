package com.keytech.modules.df.data.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.df.data.entity.DfDataLog;
import com.keytech.modules.df.data.service.IDfDataLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * DF数据日志表
 * @author yepeng
 * @since 2025.12.11
 * */
@Tag(name="DF数据日志表")
@RestController
@RequestMapping("/df/data/dfDataLog")
@Slf4j
public class DfDataLogController extends BaseController<IDfDataLogService, DfDataLog> {

	/**
	 * 添加
	 */
	@AutoLog(value = "DF数据日志表-添加")
	@Operation(summary="DF数据日志表-添加")
	@RequiresPermissions("mes3_df_data_log:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfDataLog dfDataLog) {
		baseService.save(dfDataLog);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "DF数据日志表-编辑")
	@Operation(summary="DF数据日志表-编辑")
	@RequiresPermissions("mes3_df_data_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfDataLog dfDataLog) {
		baseService.updateById(dfDataLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "DF数据日志表-通过id删除")
	@Operation(summary="DF数据日志表-通过id删除")
	@RequiresPermissions("mes3_df_data_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("mes3_df_data_log:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfDataLog dfDataLog) {
        return super.exportXls(request, dfDataLog, DfDataLog.class, "DF数据日志表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("mes3_df_data_log:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfDataLog.class);
    }

}
