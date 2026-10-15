package com.keytech.modules.plan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.enums.PlanStatusEnum;
import com.keytech.modules.plan.service.IPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;

 /**
 * @Description: 工单管理
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
@Tag(name="工单管理")
@RestController
@RequestMapping("/plan/plan")
@Slf4j
public class PlanController extends BaseController<IPlanService, Plan> {
	@Autowired
	private IPlanService planService;
	
	/**
	 * 分页列表查询
	 *
	 * @param plan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="工单管理-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<Plan>> queryPageList(Plan plan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		return planService.queryPageList(plan, pageNo, pageSize, orderField, orderType, selectFields, req);
	}
	
	/**
	 *   添加
	 *
	 * @param plan
	 * @return
	 */
	@AutoLog(value = "工单管理-添加")
	@Operation(summary="工单管理-添加")
	@RequiresPermissions("plan:mes3_plan:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody Plan plan) {
		return planService.savePlan(plan);
	}
	
	/**
	 *  编辑
	 *
	 * @param plan
	 * @return
	 */
	@AutoLog(value = "工单管理-编辑")
	@Operation(summary="工单管理-编辑")
	@RequiresPermissions("plan:mes3_plan:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody Plan plan) {
		planService.updateById(plan);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "工单管理-通过id删除")
	@Operation(summary="工单管理-通过id删除")
	@RequiresPermissions("plan:mes3_plan:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		planService.deleteData(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "工单管理-批量删除")
	@Operation(summary="工单管理-批量删除")
	@RequiresPermissions("plan:mes3_plan:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.planService.deleteData(ids);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="工单管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<Plan> queryById(@RequestParam(name="id",required=true) String id) {
		Plan plan = planService.getById(id);
		if(plan==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(plan);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param plan
    */
    @RequiresPermissions("plan:mes3_plan:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Plan plan) {
        return super.exportXls(request, plan, Plan.class, "工单管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("plan:mes3_plan:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return planService.importData(request, response);
    }

	 /**
	  *  开始计划
	  * @param plan
	  * @return
	  */
	 @AutoLog(value = "工单管理-开始计划")
	 @Operation(summary="工单管理-开始计划")
	 @RequestMapping(value = "/startPlan", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> startPlan(@RequestBody Plan plan) {
		 planService.startPlan(plan);
		 return Result.OK("计划开启成功!");
	 }

	 /**
	  *  暂停计划
	  * @param plan
	  * @return
	  */
	 @AutoLog(value = "工单管理-暂停计划")
	 @Operation(summary="工单管理-暂停计划")
	 @RequestMapping(value = "/pausePlan", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> pausePlan(@RequestBody Plan plan) {
		 planService.pauseOrRenew(plan);
		 return Result.OK("计划暂停成功!");
	 }

	 /**
	  *  恢复计划
	  * @param plan
	  * @return
	  */
	 @AutoLog(value = "工单管理-恢复计划")
	 @Operation(summary="工单管理-恢复计划")
	 @RequestMapping(value = "/renewPlan", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> renewPlan(@RequestBody Plan plan) {
		 planService.pauseOrRenew(plan);
		 return Result.OK("计划恢复成功!");
	 }

	 /**
	  * 取消计划
	  * @param plan
	  * @return
	  */
	 @AutoLog(value = "工单管理-取消计划")
	 @Operation(summary="工单管理-取消计划")
	 @RequestMapping(value = "/cancelPlan", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> cancelPlan(@RequestBody Plan plan) {
		 plan.setPlanStatus(PlanStatusEnum.CANCEL.code);
		 planService.updateById(plan);
		 return Result.OK("取消计划成功!");
	 }
}
