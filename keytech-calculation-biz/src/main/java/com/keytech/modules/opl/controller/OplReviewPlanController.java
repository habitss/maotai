package com.keytech.modules.opl.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.opl.entity.OplReviewPlan;
import com.keytech.modules.opl.service.IOplReviewPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Opl管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Tag(name="Opl管理回顾")
@RestController
@RequestMapping("/opl/oplReviewPlan")
@Slf4j
public class OplReviewPlanController extends BaseController<IOplReviewPlanService, OplReviewPlan> {
	@Autowired
	private IOplReviewPlanService oplReviewPlanService;
	
	/**
	 * 分页列表查询
	 *
	 * @param oplReviewPlan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Opl管理回顾-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<OplReviewPlan>> queryPageList(OplReviewPlan oplReviewPlan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		IPage<OplReviewPlan> pageList = oplReviewPlanService.listData(pageNo,pageSize, oplReviewPlan, req);
		return Result.OK(pageList);
	}
	

	/**
	 *  修改
	 *
	 * @param input
	 * @return
	 */
	@AutoLog(value = "Opl管理回顾-编辑")
	@Operation(summary="Opl管理回顾-编辑")
	@RequiresPermissions("opl:mes3_opl_review_plan:edit")
	@RequestMapping(value = "/oplSubmit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> oplSubmit(@RequestBody List<JSONObject> input) {
		oplReviewPlanService.oplSubmit(input);
		return Result.OK("保存成功!");
	}


	/**
	 * 导出excel
	 *
	 * @param request
	 * @param oplReviewPlan
	 */
	@RequiresPermissions("opl:mes3_opl_review_plan:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, OplReviewPlan oplReviewPlan) {
		//return super.exportXls(request, oplReviewPlan, OplReviewPlan.class, "Opl管理回顾");
		return oplReviewPlanService.exportXlsAll(request, oplReviewPlan, OplReviewPlan.class, "Opl管理回顾");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("opl:mes3_opl_review_plan:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return oplReviewPlanService.oplImportExcel(request, response, OplReviewPlan.class);
	}


}
