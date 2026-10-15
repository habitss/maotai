package com.keytech.modules.action.plan.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.util.TreeListUtil;
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
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 行动计划-问题分类
 * @Author: jeecg-boot
 * @Date:   2025-01-07
 * @Version: V1.0
 */
@Tag(name="行动计划-问题分类")
@RestController
@RequestMapping("/actionPlan/problemType")
@Slf4j
public class ActionPlanProblemTypeController extends BaseController<IActionPlanProblemTypeService, ActionPlanProblemType> {
	@Autowired
	private IActionPlanProblemTypeService actionPlanProblemTypeService;

	/**
	 * 分页列表查询
	 *
	 * @param actionPlanProblemType
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "行动计划-问题分类-分页列表查询")
	@Operation(summary="行动计划-问题分类-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<ActionPlanProblemType>> queryPageList(ActionPlanProblemType actionPlanProblemType,
															  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															  @RequestParam(name = "orderField",required = false) String orderField,
															  @RequestParam(name = "orderType",required = false) String orderType,
															  @RequestParam(name = "selectFields",required = false) String selectFields,
															  HttpServletRequest req) {
		QueryWrapper<ActionPlanProblemType> queryWrapper = QueryGenerator.initQueryWrapper(actionPlanProblemType, req.getParameterMap());
		String parentIds = req.getParameter("parentIds");
		if(StrUtil.isNotBlank(parentIds)){
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			queryWrapper.in("parent_id", parentIdList);
		}
		Page<ActionPlanProblemType> page = new Page<ActionPlanProblemType>(pageNo, pageSize);
		IPage<ActionPlanProblemType> pageList = actionPlanProblemTypeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 【vue3专用】加载节点的子数据
	 *
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
		Result<List<SelectTreeModel>> result = new Result<>();
		try {
			List<SelectTreeModel> ls = actionPlanProblemTypeService.queryListByPid(pid);
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * 【vue3专用】加载一级节点/如果是同步 则所有数据
	 *
	 * @param async
	 * @param pcode
	 * @return
	 */
	@RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pcode") String pcode) {
		Result<List<SelectTreeModel>> result = new Result<>();
		try {
			List<SelectTreeModel> ls =actionPlanProblemTypeService.queryListByPid(pcode);
			if (!async) {
				loadAllChildren(ls);
			}
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}

	/**
	 * 【vue3专用】递归求子节点 同步加载用到
	 *
	 * @param ls
	 */
	private void loadAllChildren(List<SelectTreeModel> ls) {
		for (SelectTreeModel tsm : ls) {
			List<SelectTreeModel> temp = actionPlanProblemTypeService.queryListByPid(tsm.getKey());
			if (temp != null && temp.size() > 0) {
				tsm.setChildren(temp);
				loadAllChildren(temp);
			}
		}
	}

	/**
	 *   添加
	 *
	 * @param actionPlanProblemType
	 * @return
	 */
	@AutoLog(value = "行动计划-问题分类-添加")
	@Operation(summary="行动计划-问题分类-添加")
	@RequiresPermissions("plan:mes3_action_plan_problem_type:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ActionPlanProblemType actionPlanProblemType) {
		actionPlanProblemTypeService.addActionPlanProblemType(actionPlanProblemType);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  编辑
	 *
	 * @param actionPlanProblemType
	 * @return
	 */
	@AutoLog(value = "行动计划-问题分类-编辑")
	@Operation(summary="行动计划-问题分类-编辑")
	@RequiresPermissions("plan:mes3_action_plan_problem_type:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ActionPlanProblemType actionPlanProblemType) {
		actionPlanProblemTypeService.updateActionPlanProblemType(actionPlanProblemType);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "行动计划-问题分类-通过id删除")
	@Operation(summary="行动计划-问题分类-通过id删除")
	@RequiresPermissions("plan:mes3_action_plan_problem_type:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		actionPlanProblemTypeService.deleteActionPlanProblemType(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "行动计划-问题分类-批量删除")
	@Operation(summary="行动计划-问题分类-批量删除")
	@RequiresPermissions("plan:mes3_action_plan_problem_type:deleteBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.actionPlanProblemTypeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "行动计划-问题分类-通过id查询")
	@Operation(summary="行动计划-问题分类-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ActionPlanProblemType> queryById(@RequestParam(name="id",required=true) String id) {
		ActionPlanProblemType actionPlanProblemType = actionPlanProblemTypeService.getById(id);
		if(actionPlanProblemType==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(actionPlanProblemType);
	}

	/**
	 * 行动计划-问题分类-获取所有数据
	 * @return
	 */
	//@AutoLog(value = "行动计划-问题分类-通过id查询")
	@Operation(summary="行动计划-问题分类-获取所有数据")
	@GetMapping(value = "/queryAllTreeData")
	public Result<List<ActionPlanProblemType>> queryAllTreeData(HttpServletRequest req, ActionPlanProblemType actionPlanProblemType){
		QueryWrapper<ActionPlanProblemType> queryWrapper = QueryGenerator.initQueryWrapper(actionPlanProblemType, req.getParameterMap());
		if(StrUtil.isNotBlank(actionPlanProblemType.getExcludeDisabled())){
			queryWrapper.select("id", "tenant_id", "factory_code", "dept_code", "line_code", "section", "problem_name", "parent_id", "has_child", "sort");
		}
		List<ActionPlanProblemType> list = actionPlanProblemTypeService.list(queryWrapper);
		for(ActionPlanProblemType a : list){
			if("0".equals(a.getParentId())){
				a.setParentId(null);
			}
		}
		return Result.OK(TreeListUtil.getTreeList(list, ActionPlanProblemType::getId, ActionPlanProblemType::getParentId, ActionPlanProblemType::getChildList).stream()
				.filter(M -> Objects.isNull(M.getParentId()))
				.collect(Collectors.toList()));
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param actionPlanProblemType
	 */
	@RequiresPermissions("plan:mes3_action_plan_problem_type:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, ActionPlanProblemType actionPlanProblemType) {
		actionPlanProblemType.setProblemName("*"+actionPlanProblemType.getProblemName()+"*");
		return super.exportXls(request, actionPlanProblemType, ActionPlanProblemType.class, "行动计划-问题分类");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("plan:mes3_action_plan_problem_type:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, ActionPlanProblemType.class);
	}

}
