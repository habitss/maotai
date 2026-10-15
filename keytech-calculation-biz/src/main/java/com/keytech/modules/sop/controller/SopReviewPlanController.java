package com.keytech.modules.sop.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.service.ISopReviewPlanService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: SOP管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Tag(name="SOP管理回顾")
@RestController
@RequestMapping("/sop/sopReviewPlan")
@Slf4j
public class SopReviewPlanController extends BaseController<ISopReviewPlanService, SopReviewPlan> {
	@Autowired
	private ISopReviewPlanService sopReviewPlanService;
	
	/**
	 * 分页列表查询
	 *
	 * @param sopReviewPlan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="SOP管理回顾-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<SopReviewPlan>> queryPageList(SopReviewPlan sopReviewPlan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		// 从请求中获取月份参数
		String[] monthsArray = req.getParameterValues("months");
		List<String> months = null;
		if (monthsArray != null && monthsArray.length > 0) {
			months = Arrays.asList(monthsArray);
		}
		IPage<SopReviewPlan> pageList = sopReviewPlanService.listData(pageNo,pageSize, sopReviewPlan, months, req);
		return Result.OK(pageList);
	}
	

	/**
	 *  修改
	 *
	 * @param input
	 * @return
	 */
	@AutoLog(value = "SOP管理回顾-编辑")
	@Operation(summary="SOP管理回顾-编辑")
	@RequiresPermissions("sop:mes3_sop_review_plan:edit")
	@RequestMapping(value = "/sopSubmit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> sopSubmit(@RequestBody List<JSONObject> input) {
		sopReviewPlanService.sopSubmit(input);
		return Result.OK("保存成功!");
	}


	/**
	 * 导出excel
	 *
	 * @param request
	 * @param sopReviewPlan
	 */
	@RequiresPermissions("sop:mes3_sop_review_plan:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SopReviewPlan sopReviewPlan) {
		//return super.exportXls(request, sopReviewPlan, SopReviewPlan.class, "SOP管理回顾");
		return sopReviewPlanService.exportXlsAll(request, sopReviewPlan, SopReviewPlan.class, "SOP管理回顾");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sop:mes3_sop_review_plan:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return sopReviewPlanService.sopImportExcel(request, response, SopReviewPlan.class);
	}


}
