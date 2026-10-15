package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.internal.NonNullElementWrapperList;
import com.keytech.modules.kpi.controller.KpiThirdPlayDataController;
import com.keytech.modules.pi.dto.AdapterAlgorithmDataDto;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Adapter-算法配置
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Tag(name="Adapter-算法配置")
@RestController
@RequestMapping("/pi/adapter/algorithm")
@Slf4j
public class PiAdapterAlgorithmController extends BaseController<IPiAdapterAlgorithmService, PiAdapterAlgorithm> {
	private static final Logger logger = LoggerFactory.getLogger(KpiThirdPlayDataController.class);
	@Autowired
	private IPiAdapterAlgorithmService piAdapterAlgorithmService;
	@Autowired
	private IPiAdapterPcuService iPiAdapterPcuService;
	@Autowired
	private IPiAdapterUnitService piAdapterUnitService;
	@Autowired
	private IPiAdapterUnitEopService piAdapterUnitEopService;
	@Autowired
	private IPiAdapterEopParamService piAdapterEopParamService;
	@Autowired
	private IPiSistarRtStepsService piSistarRtStepsService;

	/**
	 * 分页列表查询
	 *
	 * @param piAdapterAlgorithm
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Adapter-算法配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiAdapterAlgorithm>> queryPageList(PiAdapterAlgorithm piAdapterAlgorithm,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiAdapterAlgorithm> queryWrapper = QueryGenerator.initQueryWrapper(piAdapterAlgorithm, req.getParameterMap());
		Page<PiAdapterAlgorithm> page = new Page<PiAdapterAlgorithm>(pageNo, pageSize);
		IPage<PiAdapterAlgorithm> pageList = piAdapterAlgorithmService.page(page, queryWrapper);
		//添加信息
		pageList = piAdapterAlgorithmService.addInformation(pageList);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param adapterAlgorithmDataDto
	 * @return
	 */
	@AutoLog(value = "Adapter-算法配置-添加")
	@Operation(summary="Adapter-算法配置-添加")
	@PostMapping(value = "/addAlgorithm")
	public Result<String> addAlgorithm(@RequestBody AdapterAlgorithmDataDto adapterAlgorithmDataDto) {
		piAdapterAlgorithmService.addAlgorithm(adapterAlgorithmDataDto);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAdapterAlgorithm
	 * @return
	 */
	@AutoLog(value = "Adapter-算法配置-编辑")
	@Operation(summary="Adapter-算法配置-编辑")
	@RequiresPermissions("pi:mes3_pi_adapter_algorithm:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAdapterAlgorithm piAdapterAlgorithm) {
		piAdapterAlgorithmService.updateById(piAdapterAlgorithm);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Adapter-算法配置-通过id删除")
	@Operation(summary="Adapter-算法配置-通过id删除")
//	@RequiresPermissions("pi:mes3_pi_adapter_algorithm:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		//list
		List<String> ids = List.of(id);
		piAdapterAlgorithmService.deleteDataBatch(ids);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Adapter-算法配置-批量删除")
	@Operation(summary="Adapter-算法配置-批量删除")
//	@RequiresPermissions("pi:mes3_pi_adapter_algorithm:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		piAdapterAlgorithmService.deleteDataBatch(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Adapter-算法配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAdapterAlgorithm> queryById(@RequestParam(name="id",required=true) String id) {
		PiAdapterAlgorithm piAdapterAlgorithm = piAdapterAlgorithmService.getById(id);
		if(piAdapterAlgorithm==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAdapterAlgorithm);
	}
	/**
	 * 获取PCU
	 */
	@Operation(summary = "-算法配置-获取PCU", description = "算法配置-获取PCU")
	@GetMapping(value = "/getPcu")
	public Result<?> getPcu(@RequestParam(name="areaNo",required=true) String areaNo) {
		LambdaQueryWrapper<PiAdapterPcu> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAdapterPcu::getAreaNo,areaNo);
		List<PiAdapterPcu> list = iPiAdapterPcuService.list(queryWrapper);
		return Result.OK(list);
	}
	 /**
	  * 获取设备
	  */
	 @Operation(summary = "-算法配置-获取设备", description = "算法配置-获取设备")
	 @GetMapping(value = "/getUnit")
	 public Result<?> getArea(@RequestParam(name="areaNo",required=true) String areaNo,
							  @RequestParam(name="pcuNo",required=true) String pcuNo) {
		 LambdaQueryWrapper<PiAdapterUnit> queryWrapper = new LambdaQueryWrapper<>();
		 queryWrapper.eq(PiAdapterUnit::getAreaNo,areaNo);
		 queryWrapper.eq(PiAdapterUnit::getPcuNo,pcuNo);
		 List<PiAdapterUnit> list = piAdapterUnitService.list(queryWrapper);
		 return Result.OK(list);
	 }

	/**
	 * 获取EOP
	 */
	@Operation(summary = "-算法配置-获取EOP", description = "算法配置-获取EOP")
	@GetMapping(value = "/getEop")
	public Result<?> getEop(@RequestParam(name="areaNo",required=true) String areaNo,
							@RequestParam(name="pcuNo",required=true) String pcuNo,
							@RequestParam(name="unitNo",required=true) String unitNo) {
		LambdaQueryWrapper<PiAdapterUnitEop> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAdapterUnitEop::getAreaNo,areaNo);
		queryWrapper.eq(PiAdapterUnitEop::getPcuNo,pcuNo);
		queryWrapper.eq(PiAdapterUnitEop::getUnitNo,unitNo);
		List<PiAdapterUnitEop> list = piAdapterUnitEopService.list(queryWrapper);
		return Result.OK(list);
	}

	/**
	 * 获取EOP参数
	 */
	@Operation(summary = "-算法配置-获取EOP参数", description = "算法配置-获取EOP参数")
	@GetMapping(value = "/getEopParam")
	public Result<?> getEopParam(@RequestParam(name="areaNo",required=true) String areaNo,
							@RequestParam(name="pcuNo",required=true) String pcuNo,
							@RequestParam(name="unitNo",required=true) String unitNo,
							@RequestParam(name="eopNo",required=true) String eopNo) {
		LambdaQueryWrapper<PiAdapterEopParam> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PiAdapterEopParam::getAreaNo,areaNo);
		queryWrapper.eq(PiAdapterEopParam::getPcuNo,pcuNo);
		queryWrapper.eq(PiAdapterEopParam::getUnitNo,unitNo);
		queryWrapper.eq(PiAdapterEopParam::getEopNo,eopNo);
		List<PiAdapterEopParam> list = piAdapterEopParamService.list(queryWrapper);
		return Result.OK(list);
	}


	/**
	 * 获取算法数据
	 */
	@Operation(summary = "算法数据查询", description = "算法数据查询")
	@GetMapping(value = "/adapterData")
	public Result<AdapterAlgorithmDataDto> adapterData(PiAdapterAlgorithm piAdapterAlgorithm) {
		AdapterAlgorithmDataDto adapterAlgorithmDataDto = piAdapterAlgorithmService.adapterData(piAdapterAlgorithm);
		return Result.OK(adapterAlgorithmDataDto);
	}

	/**
	 * Adapter计算[调试接口]
	 */
	@AutoLog(value = "Adapter计算")
	@Operation(summary = "Adapter计算", description = "Adapter计算")
	@PostMapping(value = "/calculate")
	public Result<String> calculate(@RequestBody Map map) {
		logger.info("Adapter计算执行---ok");
		List<PiSistarRtSteps> list = new ArrayList<>();
		PiSistarRtSteps piSistarRtSteps = piSistarRtStepsService.getById("1964879206200328195");
		list.add(piSistarRtSteps);
		piAdapterAlgorithmService.calculate(list, null);
		logger.info("Adapter计算完成---ok,");
		return Result.OK("调用成功！");
	}

}
