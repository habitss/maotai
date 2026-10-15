package com.keytech.modules.line.efficiency.controller;

import com.keytech.modules.line.efficiency.entity.ManualFtLog;
import com.keytech.modules.line.efficiency.service.IManualFtLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.controller.PiSistarRtBatchesController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 上位机系统手动操作记录
 * @Author: jeecg-boot
 * @Date:   2025-11-13
 * @Version: V1.0
 */
@Tag(name="上位机系统手动操作记录")
@RestController
@RequestMapping("/line/manualFtLog")
@Slf4j
public class ManualFtLogController extends BaseController<IManualFtLogService, ManualFtLog> {
	@Autowired
	private IManualFtLogService manualFtLogService;

	 /**
	  * 定时任务接口-上位机系统手动操作记录数据抽取(黑龙江)
	  */
	 @AutoLog(value = "定时任务接口-上位机系统手动操作记录取")
	 @Operation(summary = "定时任务接口-上位机系统手动操作记录取", description = "定时任务接口-上位机系统手动操作记录取")
	 @PostMapping(value = "/extractData")
	 public Result<String> extractData(@RequestBody Map map) {
		 log.info("上位机系统手动操作记录取开始执行---ok");
		 manualFtLogService.extractData();
		 log.info("上位机系统手动操作记录取执行完成---ok,");
		 return Result.OK("调用成功！");
	 }

	/**
	 * 定时任务接口-上位机系统手动操作记录数据抽取(其他)
	 */
	@AutoLog(value = "定时任务接口-上位机系统手动操作记录取")
	@Operation(summary = "定时任务接口-上位机系统手动操作记录取", description = "定时任务接口-上位机系统手动操作记录取")
	@PostMapping(value = "/extractManualData")
	public Result<String> extractManualData(@RequestBody Map map) {
		log.info("上位机系统手动操作记录取开始执行---ok");
		manualFtLogService.extractManualData();
		log.info("上位机系统手动操作记录取执行完成---ok,");
		return Result.OK("调用成功！");
	}

}
