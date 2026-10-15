package com.keytech.modules.practice.controller;

import com.keytech.modules.practice.entity.BestPracticeConfig;
import com.keytech.modules.practice.service.IBestPracticeConfigService;
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
 * @Description: 最佳实践配置
 * @Author: jeecg-boot
 * @Date:   2025-02-21
 * @Version: V1.0
 */
@Tag(name="最佳实践配置")
@RestController
@RequestMapping("/practice/bestPracticeConfig")
@Slf4j
public class BestPracticeConfigController extends BaseController<IBestPracticeConfigService, BestPracticeConfig> {
	@Autowired
	private IBestPracticeConfigService bestPracticeConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bestPracticeConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="最佳实践配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<BestPracticeConfig>> queryPageList(BestPracticeConfig bestPracticeConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<BestPracticeConfig> queryWrapper = QueryGenerator.initQueryWrapper(bestPracticeConfig, req.getParameterMap());
		Page<BestPracticeConfig> page = new Page<BestPracticeConfig>(pageNo, pageSize);
		IPage<BestPracticeConfig> pageList = bestPracticeConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bestPracticeConfig
	 * @return
	 */
	@AutoLog(value = "最佳实践配置-添加")
	@Operation(summary="最佳实践配置-添加")
	@RequiresPermissions("practice:mes3_best_practice_config:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BestPracticeConfig bestPracticeConfig) {
		bestPracticeConfigService.save(bestPracticeConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param bestPracticeConfig
	 * @return
	 */
	@AutoLog(value = "最佳实践配置-编辑")
	@Operation(summary="最佳实践配置-编辑")
	@RequiresPermissions("practice:mes3_best_practice_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BestPracticeConfig bestPracticeConfig) {
		bestPracticeConfigService.updateById(bestPracticeConfig);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "最佳实践配置-通过id删除")
	@Operation(summary="最佳实践配置-通过id删除")
	@RequiresPermissions("practice:mes3_best_practice_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bestPracticeConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "最佳实践配置-批量删除")
	@Operation(summary="最佳实践配置-批量删除")
	@RequiresPermissions("practice:mes3_best_practice_config:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bestPracticeConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="最佳实践配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BestPracticeConfig> queryById(@RequestParam(name="id",required=true) String id) {
		BestPracticeConfig bestPracticeConfig = bestPracticeConfigService.getById(id);
		if(bestPracticeConfig==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(bestPracticeConfig);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bestPracticeConfig
    */
    @RequiresPermissions("practice:mes3_best_practice_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BestPracticeConfig bestPracticeConfig) {
        return super.exportXls(request, bestPracticeConfig, BestPracticeConfig.class, "最佳实践配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("practice:mes3_best_practice_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BestPracticeConfig.class);
    }

}
