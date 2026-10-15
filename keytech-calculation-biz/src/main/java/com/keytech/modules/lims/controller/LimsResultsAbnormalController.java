package com.keytech.modules.lims.controller;

import com.keytech.modules.kpi.controller.KpiThirdPlayDataController;
import com.keytech.modules.lims.dto.LimsResultsAbnormalDto;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.lims.service.ILimsResultsAbnormalService;
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
 * @Description: 超标不合格台账(LIMS)
 * @Author: HQ
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Tag(name="超标不合格台账(LIMS)")
@RestController
@RequestMapping("/lims/limsResultsAbnormal")
@Slf4j
public class LimsResultsAbnormalController extends BaseController<ILimsResultsAbnormalService, LimsResultsAbnormal> {
	private static final Logger logger = LoggerFactory.getLogger(LimsResultsAbnormalController.class);
	@Autowired
	private ILimsResultsAbnormalService limsResultsAbnormalService;

	
	/**
	 * 分页列表查询
	 *
	 * @param limsResultsAbnormal
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="超标不合格台账(LIMS)-分页列表查询")
	@GetMapping(value = "/queryPageData")
	public Result<IPage<LimsResultsAbnormal>> queryPageData(LimsResultsAbnormal limsResultsAbnormal,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {

		Page<LimsResultsAbnormal> page = new Page<LimsResultsAbnormal>(pageNo, pageSize);
		IPage<LimsResultsAbnormal> pageList = limsResultsAbnormalService.queryPageList(page, limsResultsAbnormal, req);
		return Result.OK(pageList);
	}


	/**
	 *  更新5why分析状态
	 *
	 * @param limsResultsAbnormal
	 * @return
	 */
	@AutoLog(value = "超标不合格台账(LIMS)-更新5why状态")
	@Operation(summary="超标不合格台账(LIMS)-更新5why状态")
//	@RequiresPermissions("lims:mes3_lims_results_abnormal:edit")
	@RequestMapping(value = "/updateFwhy", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> updateFwhy(@RequestBody LimsResultsAbnormal limsResultsAbnormal) {
		limsResultsAbnormalService.updateFwhy(limsResultsAbnormal);
		return Result.OK("编辑成功!");
	}

	/**
	 *   审批
	 *
	 * @param limsResultsAbnormal
	 * @return
	 */
	@AutoLog(value = "超标不合格台账(LIMS)-添加")
	@Operation(summary="超标不合格台账(LIMS)-添加")
//	@RequiresPermissions("lims:mes3_lims_results_abnormal:add")
	@PostMapping(value = "/approval")
	public Result<String> approval(@RequestBody LimsResultsAbnormal limsResultsAbnormal) {
		Result result = limsResultsAbnormalService.approval(limsResultsAbnormal);
		return result;
	}

	/**
	 *   根本原因审批
	 *   审批通过状态为1，不通过状态为2
	 * @param limsResultsAbnormal
	 * @return
	 */
	@AutoLog(value = "超标不合格台账(LIMS)-根本原因审批")
	@Operation(summary="超标不合格台账(LIMS)-根本原因审批")
//	@RequiresPermissions("lims:mes3_lims_results_abnormal:add")
	@PostMapping(value = "/causeApproval")
	public Result<String> causeApproval(@RequestBody LimsResultsAbnormal limsResultsAbnormal) {
		limsResultsAbnormalService.updateById(limsResultsAbnormal);
		return Result.OK("根本原因审批成功!");
	}

	 /**
	  *   接收LIMS推送的数据
	  * @param limsResultsAbnormalDto
	  * @return
	  */
	 @AutoLog(value = "接收LIMS推送的数据")
	 @Operation(summary="接收LIMS推送的数据")
	 @PostMapping(value = "/abnormalData")
	 public Result<?> abnormalData(@RequestBody LimsResultsAbnormalDto limsResultsAbnormalDto) {
		 limsResultsAbnormalService.abnormalSave(limsResultsAbnormalDto);
		 return Result.OK("接收成功");
	 }

	 /**
	  * 定时任务接口-超标不合格定时推送(LIMS)
	  */
	 @AutoLog(value = "定时任务接口-超标不合格定时推送")
	 @Operation(summary = "定时任务接口-超标不合格定时推送", description = "定时任务接口-超标不合格定时推送")
	 @PostMapping(value = "/limsMessageSendJob")
	 public Result<String> limsMessageSendJob(@RequestBody Map map) {
		 logger.info("超标不合格定时推送---ok");
		 limsResultsAbnormalService.limsMessageSendJob();
		 logger.info("超标不合格定时推送---ok");
		 return Result.OK("操作成功");
	 }



}
