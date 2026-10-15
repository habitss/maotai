package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.modules.kpi.service.IKpiPlanDataService;
import com.keytech.modules.kpi.service.IKpiPlatoOneService;
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
 * @Description: 一级柏拉图（生产流程）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Tag(name="一级柏拉图（生产流程）")
@RestController
@RequestMapping("/kpi/kpiPlatoOne")
@Slf4j
public class KpiPlatoOneController extends BaseController<IKpiPlatoOneService, KpiPlatoOne> {
	@Autowired
	private IKpiPlatoOneService kpiPlatoOneService;
	@Autowired
	private IKpiPlanDataService kpiPlanDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiPlatoOne
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="一级柏拉图（生产流程）-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiPlatoOne>> queryPageList(KpiPlatoOne kpiPlatoOne,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiPlatoOne> queryWrapper = QueryGenerator.initQueryWrapper(kpiPlatoOne, req.getParameterMap());
		Page<KpiPlatoOne> page = new Page<KpiPlatoOne>(pageNo, pageSize);
		IPage<KpiPlatoOne> pageList = kpiPlatoOneService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param kpiPlatoOne
	 * @return
	 */
	@AutoLog(value = "一级柏拉图（生产流程）-添加")
	@Operation(summary="一级柏拉图（生产流程）-添加")
	@RequiresPermissions("kpi:mes3_kpi_plato_one:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody KpiPlatoOne kpiPlatoOne) {
		kpiPlatoOneService.platoOneSave(kpiPlatoOne);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiPlatoOne
	 * @return
	 */
	@AutoLog(value = "一级柏拉图（生产流程）-编辑")
	@Operation(summary="一级柏拉图（生产流程）-编辑")
	@RequiresPermissions("kpi:mes3_kpi_plato_one:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiPlatoOne kpiPlatoOne) {
		kpiPlatoOneService.updateById(kpiPlatoOne);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *  通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "一级柏拉图（生产流程）-通过id删除")
	@Operation(summary="一级柏拉图（生产流程）-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_one:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiPlatoOneService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "一级柏拉图（生产流程）-批量删除")
	@Operation(summary="一级柏拉图（生产流程）-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_one:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiPlatoOneService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="一级柏拉图（生产流程）-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiPlatoOne> queryById(@RequestParam(name="id",required=true) String id) {
		KpiPlatoOne kpiPlatoOne = kpiPlatoOneService.getById(id);
		if(kpiPlatoOne==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiPlatoOne);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiPlatoOne
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_one:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiPlatoOne kpiPlatoOne) {
        return super.exportXls(request, kpiPlatoOne, KpiPlatoOne.class, "一级柏拉图（生产流程）");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_one:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return kpiPlatoOneService.platoOneImportExcel(request, response, KpiPlatoOne.class);
    }

	 @AutoLog(value = "一级柏拉图（生产流程）-查询KPI柏拉图")
	 @Operation(summary="一级柏拉图（生产流程）-查询KPI柏拉图")
	 @PostMapping(value = "/listPlatoTree")
	 public Result<?> listPlatoTree(@RequestBody KpiInfo kpiInfo) {
		 return Result.OK(kpiPlanDataService.treeData(kpiInfo.getMonth(), kpiInfo.getKpiCode()));
	 }


}
