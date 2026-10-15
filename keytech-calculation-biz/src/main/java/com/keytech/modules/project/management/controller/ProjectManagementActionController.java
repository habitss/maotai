package com.keytech.modules.project.management.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementAction;
import com.keytech.modules.project.management.service.IProjectManagementActionService;
import com.keytech.modules.project.management.service.IProjectManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.DictAspect;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 项目行动计划
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Tag(name="项目行动计划")
@RestController
@RequestMapping("/project/management/projectManagementAction")
@Slf4j
public class ProjectManagementActionController extends BaseController<IProjectManagementActionService, ProjectManagementAction> {
	@Autowired
	private IProjectManagementActionService projectManagementActionService;

	@Autowired
	private IProjectManagementService projectManagementService;
	
	/**
	 * 分页列表查询
	 *
	 * @param projectManagementAction
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="项目行动计划-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ProjectManagementAction>> queryPageList(ProjectManagementAction projectManagementAction,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		IPage<ProjectManagementAction> pageList = new Page<ProjectManagementAction>();
		pageList.setRecords(projectManagementActionService.queryList(projectManagementAction));
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param list
	 * @return
	 */
	@AutoLog(value = "项目行动计划-添加")
	@Operation(summary="项目行动计划-添加")
	@RequiresPermissions("project.management:mes3_project_management_action:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody List<ProjectManagementAction> list) {
		projectManagementActionService.saveAction(list);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param projectManagementAction
	 * @return
	 */
	@AutoLog(value = "项目行动计划-编辑")
	@Operation(summary="项目行动计划-编辑")
	@RequiresPermissions("project.management:mes3_project_management_action:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ProjectManagementAction projectManagementAction) {
		projectManagementActionService.updateById(projectManagementAction);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目行动计划-通过id删除")
	@Operation(summary="项目行动计划-通过id删除")
	@RequiresPermissions("project.management:mes3_project_management_action:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id, @RequestParam(name="nodeId") String nodeId) {
		projectManagementActionService.deleteAction(id, nodeId);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目行动计划-批量删除")
	@Operation(summary="项目行动计划-批量删除")
	@RequiresPermissions("project.management:mes3_project_management_action:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.projectManagementActionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="项目行动计划-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ProjectManagementAction> queryById(@RequestParam(name="id",required=true) String id) {
		ProjectManagementAction projectManagementAction = projectManagementActionService.getById(id);
		if(projectManagementAction==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(projectManagementAction);
	}

    /**
    * 导出excel
    * @param projectManagementAction
    */
    @RequiresPermissions("project.management:mes3_project_management_action:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(ProjectManagementAction projectManagementAction) {
		List<ProjectManagement> projectManagementList = projectManagementService.list(
				new LambdaQueryWrapper<ProjectManagement>().eq(ProjectManagement::getId, projectManagementAction.getProjectId())
		);
		if(CollUtil.isEmpty(projectManagementList)){
			throw new JeecgBootException("项目不存在！");
		}
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		List<ProjectManagementAction> list = projectManagementActionService.queryList(projectManagementAction);
		String title = projectManagementList.get(0).getProjectName()+"项目行动计划";
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject("fileName", title);
		mv.addObject("entity", ProjectManagementAction.class);
		ExportParams exportParams = new ExportParams(title + "报表", "导出人:" + sysUser.getRealname(), title);
		mv.addObject("params", exportParams);
		mv.addObject("data", list);
        return mv;
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("project.management:mes3_project_management_action:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return projectManagementActionService.importExcel(request, response);
    }

}
