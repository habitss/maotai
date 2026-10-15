package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiRaStepParam;
import com.keytech.modules.pi.service.IPiRaStepParamService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: RA-步骤参数
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Tag(name="RA-步骤参数")
@RestController
@RequestMapping("/pi/ra/piRaStepParam")
@Slf4j
public class PiRaStepParamController extends BaseController<IPiRaStepParamService, PiRaStepParam> {
	private static final Logger logger = LoggerFactory.getLogger(PiRaStepParamController.class);
	@Autowired
	private IPiRaStepParamService piRaStepParamService;

	/**
	 * 定时任务接口-RA步骤参数抽取
	 */
	@AutoLog(value = "定时任务接口-RA步骤参数抽取")
	@Operation(summary = "定时任务接口-RA步骤抽取", description = "定时任务接口-RA步骤参数抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("RA步骤参数数据抽取执行---ok");
		piRaStepParamService.extractData();
		logger.info("RA步骤参数数据抽取---ok,");
		return Result.OK("调用成功！");
	}

}
