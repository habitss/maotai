package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiCurvePartialStandard;
import com.keytech.modules.pi.entity.PiPost;
import com.keytech.modules.pi.service.IPiCurvePartialStandardService;
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

import java.io.IOException;
import java.util.Arrays;

 /**
 * @Description: 曲线局部标准配置
 * @Author: jeecg-boot
 * @Date:   2026-04-16
 * @Version: V1.0
 */
@Tag(name="曲线局部标准配置")
@RestController
@RequestMapping("/pi/curve/partialStandard")
@Slf4j
public class PiCurvePartialStandardController extends BaseController<IPiCurvePartialStandardService, PiCurvePartialStandard> {
	@Autowired
	private IPiCurvePartialStandardService piCurvePartialStandardService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piCurvePartialStandard
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="曲线局部标准配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiCurvePartialStandard>> queryPageList(PiCurvePartialStandard piCurvePartialStandard,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiCurvePartialStandard> queryWrapper = QueryGenerator.initQueryWrapper(piCurvePartialStandard, req.getParameterMap());
		Page<PiCurvePartialStandard> page = new Page<PiCurvePartialStandard>(pageNo, pageSize);
		IPage<PiCurvePartialStandard> pageList = piCurvePartialStandardService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piCurvePartialStandard
	 * @return
	 */
	@AutoLog(value = "曲线局部标准配置-添加")
	@Operation(summary="曲线局部标准配置-添加")
	@RequiresPermissions("pi:mes3_pi_curve_partial_standard:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiCurvePartialStandard piCurvePartialStandard) {
		piCurvePartialStandardService.save(piCurvePartialStandard);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piCurvePartialStandard
	 * @return
	 */
	@AutoLog(value = "曲线局部标准配置-编辑")
	@Operation(summary="曲线局部标准配置-编辑")
	@RequiresPermissions("pi:mes3_pi_curve_partial_standard:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiCurvePartialStandard piCurvePartialStandard) {
		piCurvePartialStandardService.updateById(piCurvePartialStandard);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "曲线局部标准配置-通过id删除")
	@Operation(summary="曲线局部标准配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_curve_partial_standard:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piCurvePartialStandardService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "曲线局部标准配置-批量删除")
	@Operation(summary="曲线局部标准配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_curve_partial_standard:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piCurvePartialStandardService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="曲线局部标准配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiCurvePartialStandard> queryById(@RequestParam(name="id",required=true) String id) {
		PiCurvePartialStandard piCurvePartialStandard = piCurvePartialStandardService.getById(id);
		if(piCurvePartialStandard==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piCurvePartialStandard);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piCurvePartialStandard
    */
    @RequiresPermissions("pi:mes3_pi_curve_partial_standard:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiCurvePartialStandard piCurvePartialStandard) {
        return super.exportXls(request, piCurvePartialStandard, PiCurvePartialStandard.class, "曲线局部标准配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_curve_partial_standard:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response,
								 @RequestParam(name = "dataSource", required = false) Integer dataSource) throws IOException {
		return piCurvePartialStandardService.importExcel(request, response, dataSource);
    }

}
