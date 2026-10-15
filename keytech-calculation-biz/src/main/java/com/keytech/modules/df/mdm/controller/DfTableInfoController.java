package com.keytech.modules.df.mdm.controller;

import com.keytech.modules.df.mdm.entity.DfTableInfo;
import com.keytech.modules.df.mdm.service.IDfTableInfoService;
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
 * @Description: DF数据库表基础信息
 * @Author: jeecg-boot
 * @Date:   2026-01-14
 * @Version: V1.0
 */
@Tag(name="DF数据库表基础信息")
@RestController
@RequestMapping("/df/mdm/dfTableInfo")
@Slf4j
public class DfTableInfoController extends BaseController<IDfTableInfoService, DfTableInfo> {
	@Autowired
	private IDfTableInfoService dfTableInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dfTableInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF数据库表基础信息-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<DfTableInfo>> queryPageList(DfTableInfo dfTableInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<DfTableInfo> queryWrapper = QueryGenerator.initQueryWrapper(dfTableInfo, req.getParameterMap());
		Page<DfTableInfo> page = new Page<DfTableInfo>(pageNo, pageSize);
		IPage<DfTableInfo> pageList = dfTableInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dfTableInfo
	 * @return
	 */
	@AutoLog(value = "DF数据库表基础信息-添加")
	@Operation(summary="DF数据库表基础信息-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfTableInfo dfTableInfo) {
		dfTableInfoService.saveData(dfTableInfo);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dfTableInfo
	 * @return
	 */
	@AutoLog(value = "DF数据库表基础信息-编辑")
	@Operation(summary="DF数据库表基础信息-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfTableInfo dfTableInfo) {
		dfTableInfoService.updateById(dfTableInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF数据库表基础信息-通过id删除")
	@Operation(summary="DF数据库表基础信息-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		dfTableInfoService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF数据库表基础信息-批量删除")
	@Operation(summary="DF数据库表基础信息-批量删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_table_info:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dfTableInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF数据库表基础信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<DfTableInfo> queryById(@RequestParam(name="id",required=true) String id) {
		DfTableInfo dfTableInfo = dfTableInfoService.getById(id);
		if(dfTableInfo==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(dfTableInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param dfTableInfo
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_table_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfTableInfo dfTableInfo) {
        return super.exportXls(request, dfTableInfo, DfTableInfo.class, "DF数据库表基础信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_table_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfTableInfo.class);
    }

}
