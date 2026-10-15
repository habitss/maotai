package com.keytech.modules.project.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.project.management.dto.ProjectManagementSummaryDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.service.IProjectManagementService;
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

import java.util.List;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Tag(name="项目管理")
@RestController
@RequestMapping("/project/management/projectManagement")
@Slf4j
public class ProjectManagementController extends BaseController<IProjectManagementService, ProjectManagement> {
	 @Autowired
	 private IProjectManagementService projectManagementService;
	
	/**
	 * 分页列表查询
	 *
	 * @param projectManagement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="项目管理-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ProjectManagement>> queryPageList(ProjectManagement projectManagement,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		IPage<ProjectManagement> pageList = new Page<>();
		pageList.setRecords(projectManagementService.queryAll(projectManagement, req));
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param projectManagement
	 * @return
	 */
	@AutoLog(value = "项目管理-添加")
	@Operation(summary="项目管理-添加")
	@RequiresPermissions("project.management:mes3_project_management:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ProjectManagement projectManagement) {
		projectManagementService.save(projectManagement);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param projectManagement
	 * @return
	 */
	@AutoLog(value = "项目管理-编辑")
	@Operation(summary="项目管理-编辑")
	@RequiresPermissions("project.management:mes3_project_management:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ProjectManagement projectManagement) {
		projectManagementService.updateById(projectManagement);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目管理-通过id删除")
	@Operation(summary="项目管理-通过id删除")
	@RequiresPermissions("project.management:mes3_project_management:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		projectManagementService.removeProjectByIds(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目管理-批量删除")
	@Operation(summary="项目管理-批量删除")
	@RequiresPermissions("project.management:mes3_project_management:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.projectManagementService.removeProjectByIds(ids);
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="项目管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ProjectManagement> queryById(@RequestParam(name="id",required=true) String id) {
		ProjectManagement projectManagement = projectManagementService.getById(id);
		if(projectManagement==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(projectManagement);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param projectManagement
    */
    @RequiresPermissions("project.management:mes3_project_management:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ProjectManagement projectManagement) {
        return super.exportXls(request, projectManagement, ProjectManagement.class, "项目管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("project.management:mes3_project_management:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ProjectManagement.class);
    }

	/**
	 * 项目个人激励-部门
	 *
	 * @param projectManagement
	 */
	@Operation(summary="项目个人激励-部门")
	@GetMapping(value = "/queryDepartSummary")
	public Result<List<ProjectManagementSummaryDto>> queryDepartSummary(ProjectManagement projectManagement) {
		return Result.OK(projectManagementService.getDepartmentalProjectSummary(projectManagement));
	}

	/**
	 * 项目个人激励-个人
	 *
	 * @param projectManagement
	 */
	@Operation(summary="项目个人激励-个人")
	@GetMapping(value = "/queryPersonalProjectSummary")
	public Result<List<ProjectManagementSummaryDto>> queryPersonalProjectSummary(ProjectManagement projectManagement) {
		return Result.OK(projectManagementService.getPersonalProjectSummary(projectManagement));
	}
}
