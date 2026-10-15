package com.keytech.modules.df.mdm.controller;

import com.keytech.modules.df.mdm.entity.DfMdmDeptMapping;
import com.keytech.modules.df.mdm.service.IDfMdmDeptMappingService;
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
 * @Description: MDM部门与系统部门映射关系表
 * @Author: jeecg-boot
 * @Date:   2025-12-25
 * @Version: V1.0
 */
@Tag(name="MDM部门与系统部门映射关系表")
@RestController
@RequestMapping("/df/mdm/dfMdmDeptMapping")
@Slf4j
public class DfMdmDeptMappingController extends BaseController<IDfMdmDeptMappingService, DfMdmDeptMapping> {
	@Autowired
	private IDfMdmDeptMappingService dfMdmDeptMappingService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dfMdmDeptMapping
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="MDM部门与系统部门映射关系表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<DfMdmDeptMapping>> queryPageList(DfMdmDeptMapping dfMdmDeptMapping,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<DfMdmDeptMapping> queryWrapper = QueryGenerator.initQueryWrapper(dfMdmDeptMapping, req.getParameterMap());
		if(null != dfMdmDeptMapping.getIsMapping()){
			if(dfMdmDeptMapping.getIsMapping() == 0){
				queryWrapper.isNull("sys_dept_code");
			}else if(dfMdmDeptMapping.getIsMapping() == 1){
				queryWrapper.isNotNull("sys_dept_code");
			}
		}
		Page<DfMdmDeptMapping> page = new Page<DfMdmDeptMapping>(pageNo, pageSize);
		IPage<DfMdmDeptMapping> pageList = dfMdmDeptMappingService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dfMdmDeptMapping
	 * @return
	 */
	@AutoLog(value = "MDM部门与系统部门映射关系表-添加")
	@Operation(summary="MDM部门与系统部门映射关系表-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmDeptMapping dfMdmDeptMapping) {
		dfMdmDeptMappingService.save(dfMdmDeptMapping);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dfMdmDeptMapping
	 * @return
	 */
	@AutoLog(value = "MDM部门与系统部门映射关系表-编辑")
	@Operation(summary="MDM部门与系统部门映射关系表-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmDeptMapping dfMdmDeptMapping) {
		dfMdmDeptMappingService.updateById(dfMdmDeptMapping);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "MDM部门与系统部门映射关系表-通过id删除")
	@Operation(summary="MDM部门与系统部门映射关系表-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		dfMdmDeptMappingService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "MDM部门与系统部门映射关系表-批量删除")
	@Operation(summary="MDM部门与系统部门映射关系表-批量删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dfMdmDeptMappingService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="MDM部门与系统部门映射关系表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<DfMdmDeptMapping> queryById(@RequestParam(name="id",required=true) String id) {
		DfMdmDeptMapping dfMdmDeptMapping = dfMdmDeptMappingService.getById(id);
		if(dfMdmDeptMapping==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(dfMdmDeptMapping);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param dfMdmDeptMapping
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmDeptMapping dfMdmDeptMapping) {
        return super.exportXls(request, dfMdmDeptMapping, DfMdmDeptMapping.class, "MDM部门与系统部门映射关系表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_dept_mapping:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmDeptMapping.class);
    }

}
