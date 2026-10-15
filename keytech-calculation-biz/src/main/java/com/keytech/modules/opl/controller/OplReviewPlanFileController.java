package com.keytech.modules.opl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.entity.OplReviewPlanFile;
import com.keytech.modules.opl.service.IOplReviewPlanFileService;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: Opl管理回顾-文件
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Tag(name="Opl管理回顾-文件")
@RestController
@RequestMapping("/opl/oplReviewPlanFile")
@Slf4j
public class OplReviewPlanFileController extends BaseController<IOplReviewPlanFileService, OplReviewPlanFile> {
	@Autowired
	private IOplReviewPlanFileService oplReviewPlanFileService;
	@Autowired
	private IOplReviewPlanService oplReviewPlanService;


	/**
	 *   添加
	 * @param input
	 * @return
	 */
	@AutoLog(value = "Opl管理回顾-文件-添加")
	@Operation(summary="Opl管理回顾-文件-添加")
	@PostMapping(value = "/addFile")
	public Result<?> addFile(@RequestBody JSONObject input) {
		return Result.OK(oplReviewPlanFileService.fileSave(input));
	}


	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Opl管理回顾-文件-通过id删除")
	@Operation(summary="Opl管理回顾-文件-通过id删除")
	@RequiresPermissions("opl:mes3_opl_review_plan_file:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		oplReviewPlanFileService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}



	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Opl管理回顾-文件-通过id查询")
	@GetMapping(value = "/queryData")
	public Result<?> queryData(@RequestParam(name="id",required=false) String id, @RequestParam(name="week",required=false) String week,
							   @RequestParam(name="oplId",required=false) String oplId) {

		LambdaQueryWrapper<OplReviewPlanFile> query = new LambdaQueryWrapper<>();

		if(StrUtil.isNotBlank(oplId) && StrUtil.isBlank(id)){
			LambdaQueryWrapper<OplReviewPlan> reviweQuery = new LambdaQueryWrapper<>();
			reviweQuery.eq(OplReviewPlan::getOplId,oplId);
			List<OplReviewPlan> list = oplReviewPlanService.list(reviweQuery);
			List<String> idList = list.stream()
					.map(OplReviewPlan::getId)
					.collect(Collectors.toList());
			if(CollUtil.isEmpty(idList)){
				return null;
			}
			query.in(OplReviewPlanFile::getReviewPlanId, idList);
		}else{
			query.eq(StrUtil.isNotBlank(week), OplReviewPlanFile::getWeek, week);
			query.eq(StrUtil.isNotBlank(id), OplReviewPlanFile::getReviewPlanId, id);
		}

		query.orderByAsc(OplReviewPlanFile::getCreateTime);
		List<OplReviewPlanFile> oplReviewPlanFileList = oplReviewPlanFileService.list(query);
		return Result.OK(oplReviewPlanFileList);
	}

}
