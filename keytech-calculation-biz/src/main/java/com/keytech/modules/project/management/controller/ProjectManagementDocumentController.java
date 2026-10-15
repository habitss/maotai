package com.keytech.modules.project.management.controller;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.project.management.dto.ProjectManagementDocumentDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementDocument;
import com.keytech.modules.project.management.service.IProjectManagementDocumentService;
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
import java.util.List;

/**
 * @Description: 项目清单
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Tag(name="项目清单")
@RestController
@RequestMapping("/project/management/projectManagementDocument")
@Slf4j
public class ProjectManagementDocumentController extends BaseController<IProjectManagementDocumentService, ProjectManagementDocument> {
	@Autowired
	private IProjectManagementDocumentService projectManagementDocumentService;
	
	/**
	 * 分页列表查询
	 *
	 * @param projectManagementDocument
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="项目清单-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ProjectManagementDocument>> queryPageList(ProjectManagementDocument projectManagementDocument,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<ProjectManagementDocument> queryWrapper = QueryGenerator.initQueryWrapper(projectManagementDocument, req.getParameterMap());
		Page<ProjectManagementDocument> page = new Page<ProjectManagementDocument>(pageNo, pageSize);
		IPage<ProjectManagementDocument> pageList = projectManagementDocumentService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 @Operation(summary="项目清单-分页列表查询")
	 @GetMapping(value = "/queryAllList")
	 public Result<List<JSONObject>> queryAllList(ProjectManagement projectManagement, HttpServletRequest req) throws NoSuchFieldException, IllegalAccessException {
		 return Result.OK(projectManagementDocumentService.queryAllList(projectManagement, req));
	 }
	
	/**
	 *  编辑
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "项目清单-编辑")
	@Operation(summary="项目清单-编辑")
	@RequiresPermissions("project.management:mes3_project_management_document:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ProjectManagementDocumentDto dto) {
		projectManagementDocumentService.saveOrUpdateData(dto);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目清单-通过id删除")
	@Operation(summary="项目清单-通过id删除")
	@RequiresPermissions("project.management:mes3_project_management_document:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		projectManagementDocumentService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目清单-批量删除")
	@Operation(summary="项目清单-批量删除")
	@RequiresPermissions("project.management:mes3_project_management_document:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.projectManagementDocumentService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="项目清单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ProjectManagementDocument> queryById(@RequestParam(name="id",required=true) String id) {
		ProjectManagementDocument projectManagementDocument = projectManagementDocumentService.getById(id);
		if(projectManagementDocument==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(projectManagementDocument);
	}

    /**
    * 导出excel
    * @param request
    * @param projectManagement
    */
    @RequiresPermissions("project.management:mes3_project_management_document:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ProjectManagement projectManagement) throws NoSuchFieldException, IllegalAccessException {
        return projectManagementDocumentService.exportXlsBy(request, projectManagement);
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("project.management:mes3_project_management_document:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ProjectManagementDocument.class);
    }

}
