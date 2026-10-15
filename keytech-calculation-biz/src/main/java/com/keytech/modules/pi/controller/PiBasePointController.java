package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiBasePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 基础点位维护
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
 @Tag(name="基础点位维护")
 @RestController
 @RequestMapping("/pi/piBasePoint")
 @Slf4j
public class PiBasePointController extends BaseController<IPiBasePointService, PiBasePoint> {
	@Autowired
	private IPiBasePointService piBasePointService;



	/**
	 * 分页列表查询
	 *
	 * @param piBasePoint
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "基础点位维护-分页列表查询")
	@Operation(summary = "基础点位维护-分页列表查询", description = "基础点位维护-分页列表查询")
	@GetMapping(value = "/listData")
	public Result<IPage<PiBasePoint>> queryPageList(PiBasePoint piBasePoint,
											   @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
											   @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
											   @RequestParam(name = "orderField",required = false) String orderField,
											   @RequestParam(name = "orderType",required = false) String orderType,
											   @RequestParam(name = "selectFields",required = false) String selectFields,
											   HttpServletRequest req) {

		if(StrUtil.isNotBlank(piBasePoint.getPointCode())){piBasePoint.setPointCode("*"+piBasePoint.getPointCode()+"*");}
		if(StrUtil.isNotBlank(piBasePoint.getPointName())){piBasePoint.setPointName("*"+piBasePoint.getPointName()+"*");}
		if(StrUtil.isNotBlank(piBasePoint.getQueryDept())){piBasePoint.setDeptCode(piBasePoint.getQueryDept());}
		Page<PiBasePoint> list = baseService.list(piBasePoint, pageNo, pageSize, orderField, orderType, null, req);
		//获取点位最新值
		if(null != piBasePoint.getIsNewData() && piBasePoint.getIsNewData()){
			piBasePointService.getPointData(list);
		}
		return Result.OK(list);
	}

	/**
	 *   添加
	 *
	 * @param piBasePoint
	 * @return
	 */
	@AutoLog(value = "基础点位维护-添加")
	@Operation(summary = "基础点位维护-添加", description = "基础点位维护-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiBasePoint piBasePoint) {
		piBasePointService.save(piBasePoint);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param piBasePoint
	 * @return
	 */
	@AutoLog(value = "基础点位维护-编辑")
	@Operation(summary = "基础点位维护-编辑", description = "基础点位维护-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiBasePoint piBasePoint) {
		piBasePointService.updateById(piBasePoint);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "基础点位维护-通过id删除")
	@Operation(summary = "基础点位维护-通过id删除", description = "基础点位维护-通过id删除")
	@DeleteMapping(value = "/deleteData")
	public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
		piBasePointService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "基础点位维护-批量删除")
	@Operation(summary = "基础点位维护-批量删除", description = "基础点位维护-批量删除")
	@DeleteMapping(value = "/deleteDataBatch")
	public Result<String> deleteDataBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piBasePointService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 分页列表查询
	 *
	 * @param piBasePoint
	 * @param req
	 * @return
	 */
	@AutoLog(value = "基础点位维护-分页列表查询")
	@AutoDict
	@Operation(summary = "基础点位维护-分页列表查询", description = "基础点位维护-分页列表查询")
	@GetMapping(value = "/queryExistsPiBasePoint")
	public Result<List<PiBasePoint>> queryExistsPiBasePoint(PiBasePoint piBasePoint,
													HttpServletRequest req) {
		QueryWrapper<PiBasePoint> queryWrapper = QueryGenerator.initQueryWrapper(piBasePoint, req.getParameterMap());
		queryWrapper.select("device_id");
		queryWrapper.groupBy("device_id");
		List<PiBasePoint> list = piBasePointService.list(queryWrapper);
		return Result.OK(list);
	}
//
//	/**
//	 * 通过id查询
//	 *
//	 * @param id
//	 * @return
//	 */
//	//@AutoLog(value = "基础点位维护-通过id查询")
//	@Operation(summary = "基础点位维护-通过id查询", description = "基础点位维护-通过id查询")
//	@GetMapping(value = "/queryById")
//	public Result<PiBasePoint> queryById(@RequestParam(name="id",required=true) String id) {
//		PiBasePoint piBasePoint = piBasePointService.getById(id);
//		if(piBasePoint==null) {
//			return Result.error("未找到对应数据");
//		}
//		return Result.OK(piBasePoint);
//	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param piBasePoint
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, PiBasePoint piBasePoint) {
		return exportXls(request, piBasePoint, PiBasePoint.class, "基础点位维护");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return importExcel(request, response, PiBasePoint.class);
	}

	 /**
	  * 点位查询
	  */
	 @Operation(summary = "点位查询", description = "点位查询")
	 @GetMapping(value = "/getPointData")
	 public Result<?> getPointData(PiBasePoint piBasePoint, HttpServletRequest req) {
		 LambdaQueryWrapper<PiBasePoint> queryWrapper = new LambdaQueryWrapper<>();
		 //queryWrapper.eq(PiBasePoint::getPointType,"1");
		 List<PiBasePoint> list = piBasePointService.list(queryWrapper);
		 return Result.OK(list);
	 }
 }
