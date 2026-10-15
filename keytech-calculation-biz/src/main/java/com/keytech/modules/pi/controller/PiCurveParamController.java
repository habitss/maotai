package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiCurveParam;
import com.keytech.modules.pi.service.IPiCurveParamService;
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
 * @Description: PI曲线参数配置
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Tag(name="PI曲线参数配置")
@RestController
@RequestMapping("/pi/piCurveParam")
@Slf4j
public class PiCurveParamController extends BaseController<IPiCurveParamService, PiCurveParam> {
	@Autowired
	private IPiCurveParamService piCurveParamService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piCurveParam
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI曲线参数配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiCurveParam>> queryPageList(PiCurveParam piCurveParam,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiCurveParam> queryWrapper = QueryGenerator.initQueryWrapper(piCurveParam, req.getParameterMap());
		Page<PiCurveParam> page = new Page<PiCurveParam>(pageNo, pageSize);
		IPage<PiCurveParam> pageList = piCurveParamService.page(page, queryWrapper);

		if(pageList.getRecords() != null && pageList.getRecords().size() > 0){
			//参数转换
			piCurveParamService.convert(pageList);
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piCurveParam
	 * @return
	 */
	@AutoLog(value = "PI曲线参数配置-添加")
	@Operation(summary="PI曲线参数配置-添加")
	@RequiresPermissions("pi:mes3_pi_curve_param:add")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody PiCurveParam piCurveParam) {
		return piCurveParamService.saveDate(piCurveParam);
	}
	
	/**
	 *  编辑
	 *
	 * @param piCurveParam
	 * @return
	 */
	@AutoLog(value = "PI曲线参数配置-编辑")
	@Operation(summary="PI曲线参数配置-编辑")
	@RequiresPermissions("pi:mes3_pi_curve_param:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody PiCurveParam piCurveParam) {
		return piCurveParamService.saveDate(piCurveParam);
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI曲线参数配置-通过id删除")
	@Operation(summary="PI曲线参数配置-通过id删除")
	@RequiresPermissions("pi:mes3_pi_curve_param:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piCurveParamService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI曲线参数配置-批量删除")
	@Operation(summary="PI曲线参数配置-批量删除")
	@RequiresPermissions("pi:mes3_pi_curve_param:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piCurveParamService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI曲线参数配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiCurveParam> queryById(@RequestParam(name="id",required=true) String id) {
		PiCurveParam piCurveParam = piCurveParamService.getById(id);
		if(piCurveParam==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piCurveParam);
	}

}
