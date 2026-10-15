package com.keytech.modules.cpk.controller;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.cpk.entity.CpkConfigCalc;
import com.keytech.modules.cpk.service.ICpkConfigCalcService;
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
import java.util.List;
import java.util.Map;

/**
 * @Description: CPK计算结果
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Tag(name="CPK计算结果")
@RestController
@RequestMapping("/cpk/cpkConfigCalc")
@Slf4j
public class CpkConfigCalcController extends BaseController<ICpkConfigCalcService, CpkConfigCalc> {
	@Autowired
	private ICpkConfigCalcService cpkConfigCalcService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cpkConfigCalc
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="CPK计算结果-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<CpkConfigCalc>> queryPageList(CpkConfigCalc cpkConfigCalc,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<CpkConfigCalc> queryWrapper = QueryGenerator.initQueryWrapper(cpkConfigCalc, req.getParameterMap());
		Page<CpkConfigCalc> page = new Page<CpkConfigCalc>(pageNo, pageSize);
		IPage<CpkConfigCalc> pageList = cpkConfigCalcService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cpkConfigCalc
	 * @return
	 */
	@AutoLog(value = "CPK计算结果-添加")
	@Operation(summary="CPK计算结果-添加")
	@RequiresPermissions("cpk:mes3_cpk_config_calc:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CpkConfigCalc cpkConfigCalc) {
		cpkConfigCalcService.save(cpkConfigCalc);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param cpkConfigCalc
	 * @return
	 */
	@AutoLog(value = "CPK计算结果-编辑")
	@Operation(summary="CPK计算结果-编辑")
	@RequiresPermissions("cpk:mes3_cpk_config_calc:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CpkConfigCalc cpkConfigCalc) {
		cpkConfigCalcService.updateById(cpkConfigCalc);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "CPK计算结果-通过id删除")
	@Operation(summary="CPK计算结果-通过id删除")
	@RequiresPermissions("cpk:mes3_cpk_config_calc:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cpkConfigCalcService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "CPK计算结果-批量删除")
	@Operation(summary="CPK计算结果-批量删除")
	@RequiresPermissions("cpk:mes3_cpk_config_calc:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cpkConfigCalcService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="CPK计算结果-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CpkConfigCalc> queryById(@RequestParam(name="id",required=true) String id) {
		CpkConfigCalc cpkConfigCalc = cpkConfigCalcService.getById(id);
		if(cpkConfigCalc==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(cpkConfigCalc);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cpkConfigCalc
    */
    @RequiresPermissions("cpk:mes3_cpk_config_calc:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CpkConfigCalc cpkConfigCalc) {
        return super.exportXls(request, cpkConfigCalc, CpkConfigCalc.class, "CPK计算结果");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cpk:mes3_cpk_config_calc:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CpkConfigCalc.class);
    }

	 /**
	  * 定时任务-计算cpk
	  */
	 @Operation(summary="定时任务")
	 @PostMapping(value = "/cpkCalcJob")
	 public Result<String> cpkCalcJob() {
		 cpkConfigCalcService.cpkCalcJob();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

	 /**
	  * CPK计算结果报表查询
	  * @param year 年份
	  * @param months 月份
	  * @param code 指标编码
	  * @param isAll 是否查询所有列数据1：是，0：否
	  */
	 @Operation(summary="CPK计算结果报表查询")
	 @GetMapping(value = "/queryCpkCalcReport")
	 public Result<List<Map<String, Object>>> queryCpkCalcReport(
			 @RequestParam(name="isAll", defaultValue="0") Integer isAll,
			 @RequestParam(name="code") String code,
			 @RequestParam(name="year") String year,
			 @RequestParam(name="months", required = false) String months
			 ) {
		 List<String> monthList = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
		 if(StrUtil.isNotBlank(months)){
			 monthList = Arrays.asList(months.split(","));
		 }
		 return Result.OK(cpkConfigCalcService.queryCpkCalcReport(year, monthList, code, isAll));
	 }

}
