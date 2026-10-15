package com.keytech.modules.df.mdm.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.modules.df.mdm.entity.DfMdmIndStand;
import com.keytech.modules.df.mdm.job.SyncDfMdmDataJob;
import com.keytech.modules.df.mdm.service.IDfMdmIndStandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.biz.model.ReturnT;
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
 * @Description: DF主数据-指标标准
 * @Author: jeecg-boot
 * @Date:   2026-01-26
 * @Version: V1.0
 */
@Tag(name="DF主数据-指标标准")
@RestController
@RequestMapping("/df/mdm/dfMdmIndStand")
@Slf4j
public class DfMdmIndStandController extends BaseController<IDfMdmIndStandService, DfMdmIndStand> {
	@Autowired
	private IDfMdmIndStandService dfMdmIndStandService;
	 @Autowired
	 private SyncDfMdmDataJob syncDfMdmDataJob;
	
	/**
	 * 分页列表查询
	 *
	 * @param dfMdmIndStand
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="DF主数据-指标标准-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<DfMdmIndStand>> queryPageList(DfMdmIndStand dfMdmIndStand,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<DfMdmIndStand> queryWrapper = QueryGenerator.initQueryWrapper(dfMdmIndStand, req.getParameterMap());
		Page<DfMdmIndStand> page = new Page<DfMdmIndStand>(pageNo, pageSize);
		IPage<DfMdmIndStand> pageList = dfMdmIndStandService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param dfMdmIndStand
	 * @return
	 */
	@AutoLog(value = "DF主数据-指标标准-添加")
	@Operation(summary="DF主数据-指标标准-添加")
	@RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody DfMdmIndStand dfMdmIndStand) {
		dfMdmIndStandService.save(dfMdmIndStand);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param dfMdmIndStand
	 * @return
	 */
	@AutoLog(value = "DF主数据-指标标准-编辑")
	@Operation(summary="DF主数据-指标标准-编辑")
	@RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody DfMdmIndStand dfMdmIndStand) {
		dfMdmIndStandService.updateById(dfMdmIndStand);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DF主数据-指标标准-通过id删除")
	@Operation(summary="DF主数据-指标标准-通过id删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		dfMdmIndStandService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DF主数据-指标标准-批量删除")
	@Operation(summary="DF主数据-指标标准-批量删除")
	@RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dfMdmIndStandService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="DF主数据-指标标准-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<DfMdmIndStand> queryById(@RequestParam(name="id",required=true) String id) {
		DfMdmIndStand dfMdmIndStand = dfMdmIndStandService.getById(id);
		if(dfMdmIndStand==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(dfMdmIndStand);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param dfMdmIndStand
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DfMdmIndStand dfMdmIndStand) {
        return super.exportXls(request, dfMdmIndStand, DfMdmIndStand.class, "DF主数据-指标标准");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("df.mdm:mes3_mdm_df_ind_stand:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DfMdmIndStand.class);
    }

}
