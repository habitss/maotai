package com.keytech.modules.practice.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.modules.practice.entity.BestPracticeFill;
import com.keytech.modules.practice.entity.BestPracticeReview;
import com.keytech.modules.practice.service.IBestPracticeFillService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.practice.service.IBestPracticePromotionService;
import com.keytech.modules.practice.service.IBestPracticeReviewService;
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
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * @Description: 最佳实践-填报
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Tag(name="最佳实践-填报")
@RestController
@RequestMapping("/practice/bestPracticeFill")
@Slf4j
public class BestPracticeFillController extends BaseController<IBestPracticeFillService, BestPracticeFill> {
	@Autowired
	private IBestPracticeFillService bestPracticeFillService;

	@Autowired
	private IBestPracticeReviewService bestPracticeReviewService;

	@Autowired
	private IBestPracticePromotionService practicePromotionService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeFill
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践-填报-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeFill>> queryPageList(BestPracticeFill bestPracticeFill,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeFill> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeFill, req.getParameterMap());
		Page<BestPracticeFill> page = new Page<BestPracticeFill>(pageNo, pageSize);
		IPage<BestPracticeFill> pageList = bestPracticeFillService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bestPracticeFill
	 * @return
	 */
	@AutoLog(value = "最佳实践-填报-添加")
	@Operation(summary="最佳实践-填报-添加")
	@RequiresPermissions("practice:mes3_best_practice_fill:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeFill bestPracticeFill) {
		String tempId = bestPracticeFill.getId();
		String newId = IdUtil.getSnowflakeNextIdStr();
		bestPracticeReviewService.update(
				new LambdaUpdateWrapper<BestPracticeReview>()
						.set(BestPracticeReview::getFillId, newId)
						.eq(BestPracticeReview::getFillId, tempId)
		);

		bestPracticeFill.setYearMon(DateUtil.format(new Date(), "yyyy-MM"));
		bestPracticeFill.setId(newId);
		bestPracticeFillService.save(bestPracticeFill);
		practicePromotionService.saveByFill(newId, bestPracticeFill.getFactoryAvgScore());
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param bestPracticeFill
	 * @return
	 */
	@AutoLog(value = "最佳实践-填报-编辑")
	@Operation(summary="最佳实践-填报-编辑")
	@RequiresPermissions("practice:mes3_best_practice_fill:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeFill bestPracticeFill) {
		bestPracticeFillService.updateById(bestPracticeFill);
		practicePromotionService.saveByFill(bestPracticeFill.getId(), bestPracticeFill.getFactoryAvgScore());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践-填报-通过id删除")
	@Operation(summary="最佳实践-填报-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_fill:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeFillService.removeById(id);
		practicePromotionService.deleteByFillIds(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践-填报-批量删除")
	@Operation(summary="最佳实践-填报-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_fill:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeFillService.removeByIds(Arrays.asList(ids.split(",")));
		practicePromotionService.deleteByFillIds(ids);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="最佳实践-填报-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BestPracticeFill> queryById(@RequestParam(name="id",required=true) String id) {
		BestPracticeFill bestPracticeFill = bestPracticeFillService.getById(id);
		if(bestPracticeFill==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(bestPracticeFill);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bestPracticeFill
    */
    @RequiresPermissions("practice:mes3_best_practice_fill:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BestPracticeFill bestPracticeFill) {
        return super.exportXls(request, bestPracticeFill, BestPracticeFill.class, "最佳实践-填报");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("practice:mes3_best_practice_fill:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return bestPracticeFillService.importExcelBy(request, response);
    }

}
