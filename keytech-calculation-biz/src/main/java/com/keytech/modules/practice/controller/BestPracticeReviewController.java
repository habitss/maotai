package com.keytech.modules.practice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.practice.dto.BestPracticeReviewDto;
import com.keytech.modules.practice.entity.BestPracticeFill;
import com.keytech.modules.practice.entity.BestPracticeReview;
import com.keytech.modules.practice.service.IBestPracticeFillService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 最佳实践评审
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
@Tag(name="最佳实践评审")
@RestController
@RequestMapping("/practice/bestPracticeReview")
@Slf4j
public class BestPracticeReviewController extends BaseController<IBestPracticeReviewService, BestPracticeReview> {
	@Autowired
	private IBestPracticeReviewService bestPracticeReviewService;

	@Autowired
	private IBestPracticeFillService bestPracticeFillService;

	@Autowired
	private IBestPracticePromotionService practicePromotionService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeReview
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践评审-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeReview>> queryPageList(BestPracticeReview bestPracticeReview,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeReview> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeReview, req.getParameterMap());
		Page<BestPracticeReview> page = new Page<BestPracticeReview>(pageNo, pageSize);
		IPage<BestPracticeReview> pageList = bestPracticeReviewService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-添加")
	@Operation(summary="最佳实践评审-添加")
	@RequiresPermissions("practice:mes3_best_practice_review:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeReviewDto dto) {
		bestPracticeReviewService.saveOrUpdateData(dto);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-编辑")
	@Operation(summary="最佳实践评审-编辑")
	@RequiresPermissions("practice:mes3_best_practice_review:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeReviewDto dto) {
		bestPracticeReviewService.saveOrUpdateData(dto);
		return Result.OK("编辑成功!");
	}

	/**
	 *  根据最佳实践填报id删除
	 *
	 * @param fillId
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-根据最佳实践填报id删除")
	@Operation(summary="最佳实践评审-根据最佳实践填报id删除")
	@RequiresPermissions("practice:mes3_best_practice_fill:edit")
	@DeleteMapping(value = "/deleteByFillId")
	public Result<String> deleteByFillId(@RequestParam(name="fillId") String fillId,
										 @RequestParam(name="improvementType") String improvementType,
										 @RequestParam(name="isUpdate") Boolean isUpdate) {
		if(isUpdate){
			bestPracticeFillService.lambdaUpdate()
					.set(BestPracticeFill::getShiftGroupResult, 0)
					.set(BestPracticeFill::getShiftGroupReview, null)
					.set(BestPracticeFill::getShiftGroupAvgScore, null)
					.set(BestPracticeFill::getShiftGroupReportId, null)
					.set(BestPracticeFill::getDeptResult, 0)
					.set(BestPracticeFill::getDeptReview, null)
					.set(BestPracticeFill::getDeptAvgScore, null)
					.set(BestPracticeFill::getDeptReportId, null)
					.set(BestPracticeFill::getFactoryResult, 0)
					.set(BestPracticeFill::getFactoryReview, null)
					.set(BestPracticeFill::getFactoryAvgScore, null)
					.set(BestPracticeFill::getFactoryReportId, null)
					.set(BestPracticeFill::getImprovementType, improvementType)
					.eq(BestPracticeFill::getId, fillId)
					.update();

			//处理评估数据
			practicePromotionService.saveByFill(fillId, "0");
		}
		bestPracticeReviewService.lambdaUpdate()
				.eq(BestPracticeReview::getFillId, fillId)
				.remove();
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *   删除打分表数据
	 *
	 * @param fillId 填报id
	 * @param fillType 填报类型
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-删除打分表数据")
	@Operation(summary="最佳实践评审-删除打分表数据")
	@RequiresPermissions("practice:mes3_best_practice_fill:edit")
	@DeleteMapping(value = "/deleteByType")
	public Result<String> deleteByType(@RequestParam(name="fillId") String fillId,
									   @RequestParam(name="fillType") Integer fillType) {
		bestPracticeFillService.lambdaUpdate()
				.set(1 == fillType, BestPracticeFill::getShiftGroupResult, 0)
				.set(1 == fillType, BestPracticeFill::getShiftGroupReview, null)
				.set(1 == fillType, BestPracticeFill::getShiftGroupAvgScore, null)
				.set(1 == fillType, BestPracticeFill::getShiftGroupReportId, null)
				.set(2 == fillType, BestPracticeFill::getDeptResult, 0)
				.set(2 == fillType, BestPracticeFill::getDeptReview, null)
				.set(2 == fillType, BestPracticeFill::getDeptAvgScore, null)
				.set(2 == fillType, BestPracticeFill::getDeptReportId, null)
				.set(3 >= fillType, BestPracticeFill::getFactoryResult, 0)
				.set(3 >= fillType, BestPracticeFill::getFactoryReview, null)
				.set(3 >= fillType, BestPracticeFill::getFactoryAvgScore, null)
				.set(3 >= fillType, BestPracticeFill::getFactoryReportId, null)
				.eq(BestPracticeFill::getId, fillId)
				.update();
		//如果删除的是部门或者工厂，需要同步处理评估数据
		if(3 >= fillType){
			practicePromotionService.saveByFill(fillId, "0");
		}
		//添加需要处理的类型
		List<Integer> types = new ArrayList<>();
		types.add(fillType);
		//如果删除的是部门，则需要同步处理工厂
		if(2 == fillType){
			types.add(3);
		}
		bestPracticeReviewService.lambdaUpdate()
				.eq(BestPracticeReview::getFillId, fillId)
				.in(BestPracticeReview::getFillType, types)
				.remove();
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-通过id删除")
	@Operation(summary="最佳实践评审-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_review:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeReviewService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践评审-批量删除")
	@Operation(summary="最佳实践评审-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_review:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeReviewService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="最佳实践评审-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BestPracticeReview> queryById(@RequestParam(name="id",required=true) String id) {
		BestPracticeReview bestPracticeReview = bestPracticeReviewService.getById(id);
		if(bestPracticeReview==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(bestPracticeReview);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bestPracticeReview
    */
    @RequiresPermissions("practice:mes3_best_practice_review:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BestPracticeReview bestPracticeReview) {
        return super.exportXls(request, bestPracticeReview, BestPracticeReview.class, "最佳实践评审");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("practice:mes3_best_practice_review:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BestPracticeReview.class);
    }

}
