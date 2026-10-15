package com.keytech.modules.plan.controller;

import com.keytech.modules.plan.entity.PlanPause;
import com.keytech.modules.plan.service.IPlanPauseService;
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
 * @Description: 计划暂停或恢复记录
 * @Author: jeecg-boot
 * @Date:   2025-07-02
 * @Version: V1.0
 */
@Tag(name="计划暂停或恢复记录")
@RestController
@RequestMapping("/plan/planPause")
@Slf4j
public class PlanPauseController extends BaseController<IPlanPauseService, PlanPause> {
	@Autowired
	private IPlanPauseService planPauseService;
	
	/**
	 * 分页列表查询
	 *
	 * @param planPause
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="计划暂停或恢复记录-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PlanPause>> queryPageList(PlanPause planPause,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PlanPause> queryWrapper = QueryGenerator.initQueryWrapper(planPause, req.getParameterMap());
		Page<PlanPause> page = new Page<PlanPause>(pageNo, pageSize);
		IPage<PlanPause> pageList = planPauseService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param planPause
	 * @return
	 */
	@AutoLog(value = "计划暂停或恢复记录-添加")
	@Operation(summary="计划暂停或恢复记录-添加")
	@RequiresPermissions("plan:mes3_plan_pause:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PlanPause planPause) {
		planPauseService.save(planPause);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param planPause
	 * @return
	 */
	@AutoLog(value = "计划暂停或恢复记录-编辑")
	@Operation(summary="计划暂停或恢复记录-编辑")
	@RequiresPermissions("plan:mes3_plan_pause:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PlanPause planPause) {
		planPauseService.updateById(planPause);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "计划暂停或恢复记录-通过id删除")
	@Operation(summary="计划暂停或恢复记录-通过id删除")
	@RequiresPermissions("plan:mes3_plan_pause:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		planPauseService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "计划暂停或恢复记录-批量删除")
	@Operation(summary="计划暂停或恢复记录-批量删除")
	@RequiresPermissions("plan:mes3_plan_pause:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.planPauseService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="计划暂停或恢复记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PlanPause> queryById(@RequestParam(name="id",required=true) String id) {
		PlanPause planPause = planPauseService.getById(id);
		if(planPause==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(planPause);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param planPause
    */
    @RequiresPermissions("plan:mes3_plan_pause:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PlanPause planPause) {
        return super.exportXls(request, planPause, PlanPause.class, "计划暂停或恢复记录");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("plan:mes3_plan_pause:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PlanPause.class);
    }

}
