package com.keytech.modules.practice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.practice.entity.BestPracticePromotionLog;
import com.keytech.modules.practice.service.IBestPracticePromotionLogService;
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

 /**
 * @Description: 最佳实践推广记录
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
@Tag(name="最佳实践推广记录")
@RestController
@RequestMapping("/practice/bestPracticePromotionLog")
@Slf4j
public class BestPracticePromotionLogController extends BaseController<IBestPracticePromotionLogService, BestPracticePromotionLog> {
	@Autowired
	private IBestPracticePromotionLogService bestPracticePromotionLogService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticePromotionLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践推广记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticePromotionLog>> queryPageList(BestPracticePromotionLog bestPracticePromotionLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticePromotionLog> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticePromotionLog, req.getParameterMap());
		Page<BestPracticePromotionLog> page = new Page<BestPracticePromotionLog>(pageNo, pageSize);
		IPage<BestPracticePromotionLog> pageList = bestPracticePromotionLogService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bestPracticePromotionLog
	 * @return
	 */
	@AutoLog(value = "最佳实践推广记录-添加")
	@Operation(summary="最佳实践推广记录-添加")
	@RequiresPermissions("practice:mes3_best_practice_promotion_log:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticePromotionLog bestPracticePromotionLog) {
		bestPracticePromotionLogService.save(bestPracticePromotionLog);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param bestPracticePromotionLog
	 * @return
	 */
	@AutoLog(value = "最佳实践推广记录-编辑")
	@Operation(summary="最佳实践推广记录-编辑")
	@RequiresPermissions("practice:mes3_best_practice_promotion_log:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticePromotionLog bestPracticePromotionLog) {
		bestPracticePromotionLogService.updateById(bestPracticePromotionLog);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践推广记录-通过id删除")
	@Operation(summary="最佳实践推广记录-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_promotion_log:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticePromotionLogService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践推广记录-批量删除")
	@Operation(summary="最佳实践推广记录-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_promotion_log:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticePromotionLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="最佳实践推广记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BestPracticePromotionLog> queryById(@RequestParam(name="id",required=true) String id) {
		BestPracticePromotionLog bestPracticePromotionLog = bestPracticePromotionLogService.getById(id);
		if(bestPracticePromotionLog==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(bestPracticePromotionLog);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bestPracticePromotionLog
    */
    @RequiresPermissions("practice:mes3_best_practice_promotion_log:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BestPracticePromotionLog bestPracticePromotionLog) {
        return super.exportXls(request, bestPracticePromotionLog, BestPracticePromotionLog.class, "最佳实践推广记录");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("practice:mes3_best_practice_promotion_log:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BestPracticePromotionLog.class);
    }

}
