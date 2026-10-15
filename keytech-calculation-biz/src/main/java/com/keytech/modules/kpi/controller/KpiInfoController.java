package com.keytech.modules.kpi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.service.IKpiInfoService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Tag(name="KPI清单")
@RestController
@RequestMapping("/kpi/kpiInfo")
@Slf4j
public class KpiInfoController extends BaseController<IKpiInfoService, KpiInfo> {
	@Autowired
	private IKpiInfoService kpiInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="KPI清单-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiInfo>> queryPageList(KpiInfo kpiInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiInfo> queryWrapper = QueryGenerator.initQueryWrapper(kpiInfo, req.getParameterMap());
		Page<KpiInfo> page = new Page<KpiInfo>(pageNo, pageSize);
		IPage<KpiInfo> pageList = kpiInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	 /**
	  * 清单树列表
	  * @param dto
	  * @return
	  */
	 @Operation(summary="KPI清单-列表")
	 @GetMapping(value = "/listTree")
	 public Result<?> listTree(KpiTypeDto dto) {
		 return Result.OK(kpiInfoService.listTree(dto));
	 }


	/**
	 *   添加
	 *
	 * @param kpiInfo
	 * @return
	 */
	@AutoLog(value = "KPI清单-添加")
	@Operation(summary="KPI清单-添加")
	@RequiresPermissions("kpi:mes3_kpi_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody KpiInfo kpiInfo) {
		//添加
		kpiInfoService.kpiSave(kpiInfo);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param kpiInfo
	 * @return
	 */
	@AutoLog(value = "KPI清单-编辑")
	@Operation(summary="KPI清单-编辑")
	@RequiresPermissions("kpi:mes3_kpi_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiInfo kpiInfo) {
		kpiInfoService.updateById(kpiInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "KPI清单-通过id删除")
	@Operation(summary="KPI清单-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiInfoService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "KPI清单-批量删除")
	@Operation(summary="KPI清单-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_info:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="KPI清单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiInfo> queryById(@RequestParam(name="id",required=true) String id) {
		KpiInfo kpiInfo = kpiInfoService.getById(id);
		if(kpiInfo==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param kpiInfo
    */
    @RequiresPermissions("kpi:mes3_kpi_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, KpiInfo kpiInfo) {
        return kpiInfoService.kpiInfoExportXls(request,kpiInfo);
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("kpi:mes3_kpi_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return kpiInfoService.kpiImportExcel(request, response,KpiInfo.class);
    }

	 /**
	  * 单位部门树
	  * @param
	  * isRoot 是否显示前两层
	  * factory 工厂code
	  * @return
	  */
	 @Operation(summary="单位树")
	 @GetMapping(value = "/queryDeptTreeList")
	 public Result<?> queryDeptTreeList(@RequestParam(name = "isRoot", required = false) String isRoot, @RequestParam(name = "factory", required = false) String factory) {
		 return Result.OK(kpiInfoService.queryDeptTreeList(isRoot,factory));
	 }


	 /**
	  * KPI树基础数据
	  */
	 @AutoLog(value = "KPI树基础数据")
	 @Operation(summary="KPI树基础数据")
	 @PostMapping(value = "/kpiControlConfiguration")
	 public Result<?> kpiControlConfiguration(@RequestBody KpiInfo kpiInfo) {
		 Result<?> re = kpiInfoService.kpiControlConfiguration(kpiInfo);
		 return re;
	 }

	 /**
	  * KPI清单-复制
	  * @param kpiInfo
	  * @return
	  */
	 @AutoLog(value = "KPI清单-复制")
	 @Operation(summary="KPI清单-复制")
//	 @RequiresPermissions("kpi:mes3_kpi_info:add")
	 @PostMapping(value = "/copyData")
	 public Result<String> copyData(@RequestBody KpiInfo kpiInfo) {
		 Map<String,String> map = kpiInfoService.copyData(kpiInfo);
		 if("0".equals(map.get("code"))){
			 return Result.OK("复制成功");
		 }else if("1".equals(map.get("code"))){
			 return Result.error("复制的年份无KPI数据");
		 }else if("2".equals(map.get("code"))){
			 return Result.error("两个年份KPI相同");
		 }else{
			 return Result.error("复制失败");
		 }
	 }

 }
