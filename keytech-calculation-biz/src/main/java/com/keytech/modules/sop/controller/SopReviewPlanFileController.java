package com.keytech.modules.sop.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopReviewPlanFile;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.service.ISopReviewPlanFileService;
import com.keytech.modules.sop.service.ISopReviewPlanService;
import com.keytech.modules.sop.service.impl.SopReviewPlanServiceImpl;
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
import java.util.stream.Collectors;

/**
 * @Description: SOP管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Tag(name="SOP管理回顾-文件")
@RestController
@RequestMapping("/sop/sopReviewPlanFile")
@Slf4j
public class SopReviewPlanFileController extends BaseController<ISopReviewPlanFileService, SopReviewPlanFile> {
	@Autowired
	private ISopReviewPlanFileService sopReviewPlanFileService;
	@Autowired
	private ISopReviewPlanService sopReviewPlanService;

	
	/**
	 *   添加
	 * @param input
	 * @return
	 */
	@AutoLog(value = "SOP管理回顾-文件-添加")
	@Operation(summary="SOP管理回顾-文件-添加")
	@PostMapping(value = "/addFile")
	public Result<?> addFile(@RequestBody JSONObject input) {
		return Result.OK(sopReviewPlanFileService.fileSave(input));
	}
	

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "SOP管理回顾-文件-通过id删除")
	@Operation(summary="SOP管理回顾-文件-通过id删除")
	@RequiresPermissions("sop:mes3_sop_review_plan_file:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		sopReviewPlanFileService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	

	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="SOP管理回顾-文件-通过id查询")
	@GetMapping(value = "/queryData")
	public Result<?> queryData(@RequestParam(name="id",required=false) String id, @RequestParam(name="week",required=false) String week,
							   @RequestParam(name="sopId",required=false) String sopId) {

		LambdaQueryWrapper<SopReviewPlanFile> query = new LambdaQueryWrapper<>();

		if(StrUtil.isNotBlank(sopId) && StrUtil.isBlank(id)){
			LambdaQueryWrapper<SopReviewPlan> reviweQuery = new LambdaQueryWrapper<>();
			reviweQuery.eq(SopReviewPlan::getSopId,sopId);
			List<SopReviewPlan> list = sopReviewPlanService.list(reviweQuery);
			List<String> idList = list.stream()
					.map(SopReviewPlan::getId)
					.collect(Collectors.toList());
			if(CollUtil.isEmpty(idList)){
				return null;
			}
			query.in(SopReviewPlanFile::getReviewPlanId, idList);
		}else{
			query.eq(StrUtil.isNotBlank(week), SopReviewPlanFile::getWeek, week);
			query.eq(StrUtil.isNotBlank(id), SopReviewPlanFile::getReviewPlanId, id);
		}

		query.orderByAsc(SopReviewPlanFile::getCreateTime);
		List<SopReviewPlanFile> sopReviewPlanFileList = sopReviewPlanFileService.list(query);
		return Result.OK(sopReviewPlanFileList);
	}

}
