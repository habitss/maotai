package com.keytech.modules.pi.controller;

import com.keytech.modules.kpi.controller.KpiThirdPlayDataController;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.pi.entity.PiSistarRtStepParams;
import com.keytech.modules.pi.service.IPiSistarRtStepParamsService;
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
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Tag(name="Adapter数据-步骤明细")
@RestController
@RequestMapping("/pi/adapter/piSistarRtStepParams")
@Slf4j
public class PiSistarRtStepParamsController extends BaseController<IPiSistarRtStepParamsService, PiSistarRtStepParams> {

	private static final Logger logger = LoggerFactory.getLogger(PiSistarRtStepParamsController.class);
	@Autowired
	private IPiSistarRtStepParamsService piSistarRtStepParamsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piSistarRtStepParams
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Adapter数据-步骤明细-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiSistarRtStepParams>> queryPageList(PiSistarRtStepParams piSistarRtStepParams,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiSistarRtStepParams> queryWrapper = QueryGenerator.initQueryWrapper(piSistarRtStepParams, req.getParameterMap());
		Page<PiSistarRtStepParams> page = new Page<PiSistarRtStepParams>(pageNo, pageSize);
		queryWrapper.ge(null != piSistarRtStepParams.getInsertTimestampBegin(),
				"insert_timestamp",piSistarRtStepParams.getInsertTimestampBegin());
		queryWrapper.le(null != piSistarRtStepParams.getInsertTimestampEnd(),
				"insert_timestamp",piSistarRtStepParams.getInsertTimestampEnd());
		IPage<PiSistarRtStepParams> pageList = piSistarRtStepParamsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 定时任务接口-步骤详情数据抽取
	 */
	@AutoLog(value = "定时任务接口-Adapter步骤详情数据抽取")
	@Operation(summary = "定时任务接口-Adapter步骤详情数据抽取", description = "定时任务接口-步骤详情数据抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("Adapter步骤详情数据抽取执行---ok");
		piSistarRtStepParamsService.extractData();
		logger.info("Adapter步骤详情数据抽取---ok,");
		return Result.OK("调用成功！");
	}


}
