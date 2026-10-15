package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiPlatoTwo;
import com.keytech.modules.kpi.service.IKpiPlatoTwoService;
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
 * @Description: 二级柏拉图（控制点）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Tag(name="二级柏拉图（控制点）")
@RestController
@RequestMapping("/kpi/kpiPlatoTwo")
@Slf4j
public class KpiPlatoTwoController extends BaseController<IKpiPlatoTwoService, KpiPlatoTwo> {
	@Autowired
	private IKpiPlatoTwoService kpiPlatoTwoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiPlatoTwo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="二级柏拉图（控制点）-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiPlatoTwo>> queryPageList(KpiPlatoTwo kpiPlatoTwo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiPlatoTwo> queryWrapper = QueryGenerator.initQueryWrapper(kpiPlatoTwo, req.getParameterMap());
		Page<KpiPlatoTwo> page = new Page<KpiPlatoTwo>(pageNo, pageSize);
		IPage<KpiPlatoTwo> pageList = kpiPlatoTwoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param kpiPlatoTwo
	 * @return
	 */
	@AutoLog(value = "二级柏拉图（控制点）-添加")
	@Operation(summary="二级柏拉图（控制点）-添加")
	@RequiresPermissions("kpi:mes3_kpi_plato_two:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody KpiPlatoTwo kpiPlatoTwo) {
		kpiPlatoTwoService.platoTwoSave(kpiPlatoTwo);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiPlatoTwo
	 * @return
	 */
	@AutoLog(value = "二级柏拉图（控制点）-编辑")
	@Operation(summary="二级柏拉图（控制点）-编辑")
	@RequiresPermissions("kpi:mes3_kpi_plato_two:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiPlatoTwo kpiPlatoTwo) {
		kpiPlatoTwoService.updateById(kpiPlatoTwo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "二级柏拉图（控制点）-通过id删除")
	@Operation(summary="二级柏拉图（控制点）-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_two:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiPlatoTwoService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "二级柏拉图（控制点）-批量删除")
	@Operation(summary="二级柏拉图（控制点）-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_two:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiPlatoTwoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="二级柏拉图（控制点）-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiPlatoTwo> queryById(@RequestParam(name="id",required=true) String id) {
		KpiPlatoTwo kpiPlatoTwo = kpiPlatoTwoService.getById(id);
		if(kpiPlatoTwo==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiPlatoTwo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiPlatoTwo
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_two:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiPlatoTwo kpiPlatoTwo) {
        return super.exportXls(request, kpiPlatoTwo, KpiPlatoTwo.class, "二级柏拉图（控制点）");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_two:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return kpiPlatoTwoService.platoTwoImportExcel(request, response, KpiPlatoTwo.class);
    }

}
