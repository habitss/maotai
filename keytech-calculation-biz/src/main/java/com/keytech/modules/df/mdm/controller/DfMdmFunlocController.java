package com.keytech.modules.df.mdm.controller;

import com.keytech.modules.df.mdm.entity.DfMdmFunloc;
import com.keytech.modules.df.mdm.service.IDfMdmFunlocService;
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
 * @Description: DF主数据-功能位置
 * @Author: jeecg-boot
 * @Date:   2026-01-28
 * @Version: V1.0
 */
@Tag(name="DF主数据-功能位置")
@RestController
@RequestMapping("/df/mdm/dfMdmFunloc")
@Slf4j
public class DfMdmFunlocController extends BaseController<IDfMdmFunlocService, DfMdmFunloc> {
	@Autowired
	private IDfMdmFunlocService dfMdmFunlocService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dfMdmFunloc
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF主数据-功能位置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<DfMdmFunloc>> queryPageList(DfMdmFunloc dfMdmFunloc,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<DfMdmFunloc> queryWrapper = QueryGenerator.initQueryWrapper(dfMdmFunloc, req.getParameterMap());
		Page<DfMdmFunloc> page = new Page<DfMdmFunloc>(pageNo, pageSize);
		IPage<DfMdmFunloc> pageList = dfMdmFunlocService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dfMdmFunloc
	 * @return
	 */
	@AutoLog(value = "DF主数据-功能位置-添加")
	@Operation(summary="DF主数据-功能位置-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_funloc:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmFunloc dfMdmFunloc) {
		dfMdmFunlocService.save(dfMdmFunloc);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dfMdmFunloc
	 * @return
	 */
	@AutoLog(value = "DF主数据-功能位置-编辑")
	@Operation(summary="DF主数据-功能位置-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_funloc:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmFunloc dfMdmFunloc) {
		dfMdmFunlocService.updateById(dfMdmFunloc);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF主数据-功能位置-通过id删除")
	@Operation(summary="DF主数据-功能位置-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_funloc:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		dfMdmFunlocService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF主数据-功能位置-批量删除")
	@Operation(summary="DF主数据-功能位置-批量删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_funloc:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dfMdmFunlocService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF主数据-功能位置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<DfMdmFunloc> queryById(@RequestParam(name="id",required=true) String id) {
		DfMdmFunloc dfMdmFunloc = dfMdmFunlocService.getById(id);
		if(dfMdmFunloc==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(dfMdmFunloc);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param dfMdmFunloc
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_funloc:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmFunloc dfMdmFunloc) {
        return super.exportXls(request, dfMdmFunloc, DfMdmFunloc.class, "DF主数据-功能位置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_funloc:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmFunloc.class);
    }

}
