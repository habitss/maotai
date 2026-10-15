package com.keytech.modules.alarm.controller;

import com.keytech.base.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.alarm.entity.AlarmIngestLog;
import com.keytech.modules.alarm.service.IAlarmIngestLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


 /**
 * 报警接入日志
 * @author liu
 * @since 2026.01.12
 * */
@Tag(name="报警接入日志")
@RestController
@RequestMapping("/alarm/alarmIngestLog")
@Slf4j
public class AlarmIngestLogController extends BaseController<IAlarmIngestLogService, AlarmIngestLog> {

	/**
	 * 添加
	 */
	@AutoLog(value = "报警接入日志-添加")
	@Operation(summary="报警接入日志-添加")
	@RequiresPermissions("alarm:alarm_ingest_log:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AlarmIngestLog alarmIngestLog) {
		baseService.save(alarmIngestLog);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "报警接入日志-编辑")
	@Operation(summary="报警接入日志-编辑")
	@RequiresPermissions("alarm:alarm_ingest_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AlarmIngestLog alarmIngestLog) {
		baseService.updateById(alarmIngestLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "报警接入日志-通过id删除")
	@Operation(summary="报警接入日志-通过id删除")
	@RequiresPermissions("alarm:alarm_ingest_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("alarm:alarm_ingest_log:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmIngestLog alarmIngestLog) {
        return super.exportXls(request, alarmIngestLog, AlarmIngestLog.class, "报警接入日志");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("alarm:alarm_ingest_log:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmIngestLog.class);
    }

}
