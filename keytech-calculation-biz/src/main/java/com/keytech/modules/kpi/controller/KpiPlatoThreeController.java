package com.keytech.modules.kpi.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.kpi.entity.KpiPlatoThree;
import com.keytech.modules.kpi.service.IKpiPlatoThreeService;
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
 * @Description: 三级柏拉图(PI点)
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
@Tag(name="三级柏拉图(PI点)")
@RestController
@RequestMapping("/kpi/kpiPlatoThree")
@Slf4j
public class KpiPlatoThreeController extends BaseController<IKpiPlatoThreeService, KpiPlatoThree> {
	@Autowired
	private IKpiPlatoThreeService kpiPlatoThreeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiPlatoThree
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="三级柏拉图(PI点)-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiPlatoThree>> queryPageList(KpiPlatoThree kpiPlatoThree,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		if(StrUtil.isNotBlank(kpiPlatoThree.getControlActivity())){
			kpiPlatoThree.setControlActivity("*"+kpiPlatoThree.getControlActivity()+"*");
		}
        QueryWrapper<KpiPlatoThree> queryWrapper = QueryGenerator.initQueryWrapper(kpiPlatoThree, req.getParameterMap());
		Page<KpiPlatoThree> page = new Page<KpiPlatoThree>(pageNo, pageSize);
		IPage<KpiPlatoThree> pageList = kpiPlatoThreeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param kpiPlatoThree
	 * @return
	 */
	@AutoLog(value = "三级柏拉图(PI点)-添加")
	@Operation(summary="三级柏拉图(PI点)-添加")
	@RequiresPermissions("kpi:mes3_kpi_plato_three:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody KpiPlatoThree kpiPlatoThree) {
		kpiPlatoThreeService.addThree(kpiPlatoThree);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiPlatoThree
	 * @return
	 */
	@AutoLog(value = "三级柏拉图(PI点)-编辑")
	@Operation(summary="三级柏拉图(PI点)-编辑")
	@RequiresPermissions("kpi:mes3_kpi_plato_three:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiPlatoThree kpiPlatoThree) {
		kpiPlatoThreeService.updateById(kpiPlatoThree);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "三级柏拉图(PI点)-通过id删除")
	@Operation(summary="三级柏拉图(PI点)-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_three:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiPlatoThreeService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "三级柏拉图(PI点)-批量删除")
	@Operation(summary="三级柏拉图(PI点)-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_plato_three:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiPlatoThreeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="三级柏拉图(PI点)-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiPlatoThree> queryById(@RequestParam(name="id",required=true) String id) {
		KpiPlatoThree kpiPlatoThree = kpiPlatoThreeService.getById(id);
		if(kpiPlatoThree==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiPlatoThree);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiPlatoThree
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_three:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiPlatoThree kpiPlatoThree) {
        return super.exportXls(request, kpiPlatoThree, KpiPlatoThree.class, "三级柏拉图(PI点)");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_plato_three:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return kpiPlatoThreeService.platoThreeImportExcel(request, response, KpiPlatoThree.class);
    }

}
