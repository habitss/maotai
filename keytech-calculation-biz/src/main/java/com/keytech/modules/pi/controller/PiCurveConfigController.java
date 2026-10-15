package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.dto.OrderPointDto;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.IPiCurveConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.util.AdvancedDateTimeCalculator;
import com.keytech.modules.pi.util.MeasurementDataGenerator;
import com.keytech.modules.pi.vo.DevicePointVo;
import com.keytech.modules.pi.vo.PiCurveStepVo;
import com.keytech.modules.pi.vo.PointDataVo;
import com.keytech.modules.pi.vo.StepDataVo;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: PI曲线配置
 * @Author: jeecg-boot
 * @Date:   2025-12-09
 * @Version: V1.0
 */
@Tag(name="PI曲线配置")
@RestController
@RequestMapping("/pi/piCurveConfig")
@Slf4j
public class PiCurveConfigController extends BaseController<IPiCurveConfigService, PiCurveConfig> {
	@Autowired
	private IPiCurveConfigService piCurveConfigService;

	/**
	 * 分页列表查询
	 *
	 * @param piCurveConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI曲线配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiCurveConfig>> queryPageList(PiCurveConfig piCurveConfig,
														@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														@RequestParam(name = "orderField",required = false) String orderField,
														@RequestParam(name = "orderType",required = false) String orderType,
														@RequestParam(name = "selectFields",required = false) String selectFields,
														HttpServletRequest req) {
		QueryWrapper<PiCurveConfig> queryWrapper = QueryGenerator.initQueryWrapper(piCurveConfig, req.getParameterMap());
		Page<PiCurveConfig> page = new Page<PiCurveConfig>(pageNo, pageSize);
		IPage<PiCurveConfig> pageList = piCurveConfigService.page(page, queryWrapper);
		if(pageList.getRecords() != null && pageList.getRecords().size() > 0){
			//参数转换
			baseService.convert(pageList);
		}
		return Result.OK(pageList);
	}

	/**
	 *  编辑
	 *
	 * @param piCurveConfig
	 * @return
	 */
	@AutoLog(value = "PI曲线配置-编辑")
	@Operation(summary="PI曲线配置-编辑")
	@RequiresPermissions("pi:mes3_pi_curve_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiCurveConfig piCurveConfig) {
		baseService.editData(piCurveConfig);
		return Result.OK(StrUtil.isNotBlank(piCurveConfig.getId()) ? "编辑成功!" : "新增成功!");
	}
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI-结果转换模型配置-主表-通过id删除")
	@Operation(summary="PI-结果转换模型配置-主表-通过id删除")
	@RequiresPermissions("pi:mes3_pi_result_transformation_model:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piCurveConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	/**
	 *  获取设备
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getDevice")
	public Result<?> getDevice(@RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getDevice(piCurveStepDto));
	}


	/**
	 *  获取配方
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取配方")
	@Operation(summary = "获取配方", description = "获取配方")
	@PostMapping(value = "/getRecipe")
	public Result<?> getRecipe(@NotNull @RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getRecipe(piCurveStepDto));

	}

	/**
	 *  获取订单
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取订单")
	@Operation(summary = "获取订单", description = "获取订单")
	@PostMapping(value = "/getOrder")
	public Result<?> getOrder(@NotNull @RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getOrder(piCurveStepDto));


	}

	/**
	 *  获取批次
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getBatch")
	public Result<?> getBatch(@RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getBatch(piCurveStepDto));
	}


	/**
	 * 获取数据(步骤时间)
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取数据")
	@Operation(summary = "获取数据", description = "获取数据")
	@PostMapping(value = "/getData")
	public Result<?> getData(@NotNull @RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getData(piCurveStepDto));
	}


	/**
	 *  获取设备和点位
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/pointData")
	public Result<?> pointData(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		return Result.OK(baseService.pointData(piSistarRtSteps));
	}


	/**
	 *  获取批次
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getRtBatches")
	public Result<?> getRtBatches(@RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.getRtBatches(piCurveStepDto));
	}


	/**
	 *  获取设备和点位
	 * @param piCurveStepDto
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/unitAndPoint")
	public Result<?> unitAndPoint(@RequestBody PiCurveStepDto piCurveStepDto) {
		return Result.OK(baseService.unitAndPoint(piCurveStepDto));
	}


	/**
	 *  获取设备和点位
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/getPointData")
	public Result<?> getPointData(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		return Result.OK(baseService.getPointData(piSistarRtSteps));
	}
}
