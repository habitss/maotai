package com.keytech.modules.practice.controller;

import com.keytech.modules.practice.entity.BestPracticeType;
import com.keytech.modules.practice.service.IBestPracticeTypeService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;

 /**
 * @Description: 最佳实践类型
 * @Author: jeecg-boot
 * @Date:   2025-02-21
 * @Version: V1.0
 */
@Tag(name="最佳实践类型")
@RestController
@RequestMapping("/practice/bestPracticeType")
@Slf4j
public class BestPracticeTypeController extends BaseController<IBestPracticeTypeService, BestPracticeType> {
	@Autowired
	private IBestPracticeTypeService bestPracticeTypeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeType
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践类型-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeType>> queryPageList(BestPracticeType bestPracticeType,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeType> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeType, req.getParameterMap());
		Page<BestPracticeType> page = new Page<BestPracticeType>(pageNo, pageSize);
		IPage<BestPracticeType> pageList = bestPracticeTypeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bestPracticeType
	 * @return
	 */
	@AutoLog(value = "最佳实践类型-添加")
	@Operation(summary="最佳实践类型-添加")
	@RequiresPermissions("practice:mes3_best_practice_type:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeType bestPracticeType) {
		bestPracticeTypeService.save(bestPracticeType);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param bestPracticeType
	 * @return
	 */
	@AutoLog(value = "最佳实践类型-编辑")
	@Operation(summary="最佳实践类型-编辑")
	@RequiresPermissions("practice:mes3_best_practice_type:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeType bestPracticeType) {
		bestPracticeTypeService.updateById(bestPracticeType);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践类型-通过id删除")
	@Operation(summary="最佳实践类型-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_type:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeTypeService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践类型-批量删除")
	@Operation(summary="最佳实践类型-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_type:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeTypeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="最佳实践类型-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BestPracticeType> queryById(@RequestParam(name="id",required=true) String id) {
		BestPracticeType bestPracticeType = bestPracticeTypeService.getById(id);
		if(bestPracticeType==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(bestPracticeType);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bestPracticeType
    */
    @RequiresPermissions("practice:mes3_best_practice_type:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BestPracticeType bestPracticeType) {
        return super.exportXls(request, bestPracticeType, BestPracticeType.class, "最佳实践类型");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("practice:mes3_best_practice_type:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BestPracticeType.class);
    }

}
