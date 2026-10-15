package com.keytech.modules.project.management.controller;

import com.keytech.modules.project.management.entity.ProjectManagementNode;
import com.keytech.modules.project.management.service.IProjectManagementNodeService;
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
 * @Description: 项目节点
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Tag(name="项目节点")
@RestController
@RequestMapping("/project/management/projectManagementNode")
@Slf4j
public class ProjectManagementNodeController extends BaseController<IProjectManagementNodeService, ProjectManagementNode> {
	@Autowired
	private IProjectManagementNodeService projectManagementNodeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param projectManagementNode
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="项目节点-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ProjectManagementNode>> queryPageList(ProjectManagementNode projectManagementNode,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ProjectManagementNode> queryWrapper = QueryGenerator.initQueryWrapper(projectManagementNode, req.getParameterMap());
		Page<ProjectManagementNode> page = new Page<ProjectManagementNode>(pageNo, pageSize);
		IPage<ProjectManagementNode> pageList = projectManagementNodeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param projectManagementNode
	 * @return
	 */
	@AutoLog(value = "项目节点-添加")
	@Operation(summary="项目节点-添加")
	@RequiresPermissions("project.management:mes3_project_management_node:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ProjectManagementNode projectManagementNode) {
		projectManagementNodeService.save(projectManagementNode);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param projectManagementNode
	 * @return
	 */
	@AutoLog(value = "项目节点-编辑")
	@Operation(summary="项目节点-编辑")
	@RequiresPermissions("project.management:mes3_project_management_node:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ProjectManagementNode projectManagementNode) {
		projectManagementNodeService.updateById(projectManagementNode);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目节点-通过id删除")
	@Operation(summary="项目节点-通过id删除")
	@RequiresPermissions("project.management:mes3_project_management_node:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		projectManagementNodeService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目节点-批量删除")
	@Operation(summary="项目节点-批量删除")
	@RequiresPermissions("project.management:mes3_project_management_node:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.projectManagementNodeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="项目节点-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ProjectManagementNode> queryById(@RequestParam(name="id",required=true) String id) {
		ProjectManagementNode projectManagementNode = projectManagementNodeService.getById(id);
		if(projectManagementNode==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(projectManagementNode);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param projectManagementNode
    */
    @RequiresPermissions("project.management:mes3_project_management_node:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ProjectManagementNode projectManagementNode) {
        return super.exportXls(request, projectManagementNode, ProjectManagementNode.class, "项目节点");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("project.management:mes3_project_management_node:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ProjectManagementNode.class);
    }

}
