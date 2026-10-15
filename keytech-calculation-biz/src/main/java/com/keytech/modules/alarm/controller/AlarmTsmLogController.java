package com.keytech.modules.alarm.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.alarm.entity.AlarmTsmLog;
import com.keytech.modules.alarm.service.IAlarmTsmLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * TSM接口请求日志表
 * @author yepeng
 * @since 2026.02.13
 * */
@Tag(name="TSM接口请求日志表")
@RestController
@RequestMapping("/alarm/alarmTsmLog")
@Slf4j
public class AlarmTsmLogController extends BaseController<IAlarmTsmLogService, AlarmTsmLog> {

	/**
	 * 添加
	 */
	@AutoLog(value = "TSM接口请求日志表-添加")
	@Operation(summary="TSM接口请求日志表-添加")
	@RequiresPermissions("alarm:alarm_tsm_log:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AlarmTsmLog alarmTsmLog) {
		baseService.save(alarmTsmLog);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "TSM接口请求日志表-编辑")
	@Operation(summary="TSM接口请求日志表-编辑")
	@RequiresPermissions("alarm:alarm_tsm_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AlarmTsmLog alarmTsmLog) {
		baseService.updateById(alarmTsmLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "TSM接口请求日志表-通过id删除")
	@Operation(summary="TSM接口请求日志表-通过id删除")
	@RequiresPermissions("alarm:alarm_tsm_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("alarm:alarm_tsm_log:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmTsmLog alarmTsmLog) {
        return super.exportXls(request, alarmTsmLog, AlarmTsmLog.class, "TSM接口请求日志表");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("alarm:alarm_tsm_log:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmTsmLog.class);
    }

}
