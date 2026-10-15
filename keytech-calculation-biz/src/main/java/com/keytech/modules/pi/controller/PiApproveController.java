package com.keytech.modules.pi.controller;

import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.common.QueryDeptScopeUtil;
import com.keytech.modules.pi.entity.PiApprove;
import com.keytech.modules.pi.service.IPiApproveService;
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
 * @Description: pi报表-报表审批
 * @Author: HQ
 * @Date:   2025-01-09
 * @Version: V1.0
 */


@Tag(name="pi报表-报表审批")
@RestController
@RequestMapping("/pi/piApprove")
@Slf4j
public class PiApproveController extends BaseController<IPiApproveService, PiApprove> {
	@Autowired
	private IPiApproveService piApproveService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piApprove
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="pi报表-报表审批-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiApprove>> queryPageList(PiApprove piApprove,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiApprove> queryWrapper = QueryGenerator.initQueryWrapper(piApprove, req.getParameterMap());
		//不查询管理员/和工厂管理员
		queryWrapper.ne("modify_by", "admin").ne("modify_by", "fadmin");
		QueryDeptScopeUtil.initDeptScope(queryWrapper, req.getParameterMap(), piApprove);
		Page<PiApprove> page = new Page<PiApprove>(pageNo, pageSize);
		IPage<PiApprove> pageList = piApproveService.page(page, queryWrapper);
		//匹配
		IPage<PiApprove> result = piApproveService.matchData(pageList);
		return Result.OK(result);
	}
	
	/**
	 *   添加
	 *
	 * @param piApprove
	 * @return
	 */
	@AutoLog(value = "pi报表-报表审批-添加")
	@Operation(summary="pi报表-报表审批-添加")
	//@RequiresPermissions("pi:mes3_pi_approve:add")
	@PostMapping(value = "/addData")
	public Result<?> addData(@RequestBody PiApprove piApprove) {
		return piApproveService.approveSave(piApprove);
	}
	
	/**
	 *  编辑
	 *
	 * @param piApprove
	 * @return
	 */
	@AutoLog(value = "pi报表-报表审批-编辑")
	@Operation(summary="pi报表-报表审批-编辑")
	@RequiresPermissions("pi:mes3_pi_approve:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiApprove piApprove) {
		piApproveService.updateById(piApprove);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pi报表-报表审批-通过id删除")
	@Operation(summary="pi报表-报表审批-通过id删除")
	@RequiresPermissions("pi:mes3_pi_approve:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piApproveService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pi报表-报表审批-批量删除")
	@Operation(summary="pi报表-报表审批-批量删除")
	@RequiresPermissions("pi:mes3_pi_approve:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piApproveService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="pi报表-报表审批-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiApprove> queryById(@RequestParam(name="id",required=true) String id) {
		PiApprove piApprove = piApproveService.getById(id);
		if(piApprove==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piApprove);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piApprove
    */
    @RequiresPermissions("pi:mes3_pi_approve:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiApprove piApprove) {
        return super.exportXls(request, piApprove, PiApprove.class, "pi报表-报表审批");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_approve:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiApprove.class);
    }

	 /**
	  * 报表审批-审批
	  *	1同意,2
	  * @param piApprove
	  * @return
	  */
	 @AutoLog(value = "pi报表-报表审批-审批")
	 @Operation(summary="pi报表-报表审批-审批")
	 //@RequiresPermissions("pi:mes3_pi_approve:add")
	 @PostMapping(value = "/approve")
	 public Result<String> approve(@RequestBody PiApprove piApprove) {
		 piApproveService.approve(piApprove);
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }
}
