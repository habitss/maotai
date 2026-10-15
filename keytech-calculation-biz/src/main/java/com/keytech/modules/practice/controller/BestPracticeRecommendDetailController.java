package com.keytech.modules.practice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.practice.entity.BestPracticeRecommendDetail;
import com.keytech.modules.practice.service.IBestPracticeRecommendDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

 /**
 * @Description: 最佳实践推荐表明细
 * @Author: jeecg-boot
 * @Date:   2025-03-24
 * @Version: V1.0
 */
@Tag(name="最佳实践推荐表明细")
@RestController
@RequestMapping("/practice/bestPracticeRecommendDetail")
@Slf4j
public class BestPracticeRecommendDetailController extends BaseController<IBestPracticeRecommendDetailService, BestPracticeRecommendDetail> {
	@Autowired
	private IBestPracticeRecommendDetailService bestPracticeRecommendDetailService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeRecommendDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践推荐表明细-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeRecommendDetail>> queryPageList(BestPracticeRecommendDetail bestPracticeRecommendDetail,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeRecommendDetail> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeRecommendDetail, req.getParameterMap());
		Page<BestPracticeRecommendDetail> page = new Page<BestPracticeRecommendDetail>(pageNo, pageSize);
		IPage<BestPracticeRecommendDetail> pageList = bestPracticeRecommendDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bestPracticeRecommendDetail
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表明细-添加")
	@Operation(summary="最佳实践推荐表明细-添加")
	@RequiresPermissions("practice:mes3_best_practice_recommend_detail:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeRecommendDetail bestPracticeRecommendDetail) {
		bestPracticeRecommendDetailService.save(bestPracticeRecommendDetail);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param bestPracticeRecommendDetail
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表明细-编辑")
	@Operation(summary="最佳实践推荐表明细-编辑")
	@RequiresPermissions("practice:mes3_best_practice_recommend_detail:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeRecommendDetail bestPracticeRecommendDetail) {
		bestPracticeRecommendDetailService.updateById(bestPracticeRecommendDetail);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表明细-通过id删除")
	@Operation(summary="最佳实践推荐表明细-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_recommend_detail:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeRecommendDetailService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践推荐表明细-批量删除")
	@Operation(summary="最佳实践推荐表明细-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_recommend_detail:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeRecommendDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
}
