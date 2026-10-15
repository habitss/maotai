package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.controller.KpiThirdPlayDataController;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.dto.OrderPointDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.AdvancedDateTimeCalculator;
import com.keytech.modules.pi.util.MeasurementDataGenerator;
import com.keytech.modules.pi.vo.DevicePointVo;
import com.keytech.modules.pi.vo.PointDataVo;
import com.keytech.modules.pi.vo.StepDataVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: Adapter数据抽取-步骤
 * @Author: HQ
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Tag(name="Adapter数据抽取-步骤")
@RestController
@RequestMapping("/pi/adapter/piSistarRtSteps")
@Slf4j
public class PiSistarRtStepsController extends BaseController<IPiSistarRtStepsService, PiSistarRtSteps> {
	private static final Logger logger = LoggerFactory.getLogger(KpiThirdPlayDataController.class);
	@Autowired
	private IPiSistarRtStepsService piSistarRtStepsService;
	@Autowired
	private IPiSistarEngRecipesService piSistarEngRecipesService;
	@Autowired
	private IPiBasePointService piBasePointService;
	@Autowired
	private IMdmDeviceService mdmDeviceService;
	@Autowired
	private IPiComputationModelService iPiComputationModelService;
	@Autowired
	private IPiCurveStandardService iPiCurveStandardService;
	@Autowired
	private IPiCurveConfigService iPiCurveConfigService;
	/**
	 * 分页列表查询
	 *
	 * @param piSistarRtSteps
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Adapter数据抽取-步骤-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiSistarRtSteps>> queryPageList(PiSistarRtSteps piSistarRtSteps,
														@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														@RequestParam(name = "orderField",required = false) String orderField,
														@RequestParam(name = "orderType",required = false) String orderType,
														@RequestParam(name = "selectFields",required = false) String selectFields,
														HttpServletRequest req) {
		QueryWrapper<PiSistarRtSteps> queryWrapper = QueryGenerator.initQueryWrapper(piSistarRtSteps, req.getParameterMap());
		Page<PiSistarRtSteps> page = new Page<PiSistarRtSteps>(pageNo, pageSize);
		IPage<PiSistarRtSteps> pageList = piSistarRtStepsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 定时任务接口-步骤数据抽取
	 */
	@AutoLog(value = "定时任务接口-Adapter数据抽取")
	@Operation(summary = "定时任务接口-Adapter数据抽取", description = "定时任务接口-数据抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("Adapter数据抽取执行---ok");
		piSistarRtStepsService.extractData();
		logger.info("Adapter数据抽取---ok,");
		return Result.OK("调用成功！");
	}


	/**
	 * 定时任务接口-Adapter字典抽取
	 */
	@AutoLog(value = "定时任务接口-Adapter字典抽取")
	@Operation(summary = "定时任务接口-Adapter字典抽取", description = "定时任务接口-Adapter字典抽取")
	@PostMapping(value = "/dictionaryData")
	public Result<String> dictionaryData(@RequestBody Map map) {
		logger.info("Adapter数据抽取执行---ok");
		piSistarRtStepsService.dictionaryData();
		logger.info("Adapter数据抽取---ok,");
		return Result.OK("调用成功！");
	}

	/**
	 *  获取配方品种
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取配方品种")
	@Operation(summary = "获取配方品种", description = "获取配方品种")
	@PostMapping(value = "/getReccat")
	public Result<?> getReccat(@NotNull @RequestBody PiSistarRtSteps piSistarRtSteps) {
		String unitNames = piSistarRtSteps.getUnitNames();
		String pcuNos = piSistarRtSteps.getPcuNos();
		String[] pcuNoArr = null;
		if(pcuNos != null && pcuNos.contains(",")){
			pcuNoArr = pcuNos.split(",");
		}

		LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piSistarRtSteps.getAreaNo() != null, PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
		wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEventTime());
		wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
		wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
		wrapper.notLike(PiSistarRtSteps::getReccatName,"CIP");
		if(pcuNoArr != null && pcuNoArr.length > 0){
			wrapper.in(PiSistarRtSteps::getPcuNo, pcuNoArr);
		}else{
			wrapper.eq(StrUtil.isNotBlank(pcuNos), PiSistarRtSteps::getPcuNo, pcuNos);
		}
		wrapper.eq(Objects.nonNull(piSistarRtSteps.getUnitNo()), PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitNo());
		wrapper.orderByAsc(PiSistarRtSteps::getRecipeNo);

		//设备-查询条件
		if(StrUtil.isNotBlank(unitNames)){
			String [] unitName = unitNames.split(",");
			if (unitName!=null && unitName.length > 0) {
				wrapper.and(w -> {
					for (String name : unitName) {
						w.or().like(PiSistarRtSteps::getUnitName, name);
					}
				});
			}
		}
		//查看配置是否存在(????)
		List<PiCurveConfig> piCurveConfigList = iPiCurveConfigService.getCurveConfigByArea(piSistarRtSteps.getPcuNos());

		//adapter
		List<PiSistarRtSteps> list = piSistarRtStepsService.list(wrapper);
		if(list != null && list.size() == 0 || (piCurveConfigList != null && !piCurveConfigList.isEmpty())){
			// pi
			list = new ArrayList<>();
		}

		//去重
		List<PiSistarRtSteps> distinctListNew = list.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(
						step -> step.getReccatNo() + "_" + step.getReccatName(), // 使用字符串拼接作为键
						step -> step,
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.collect(Collectors.toList());

		return Result.OK(distinctListNew);
	}

	/**
	 *  获取配方
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取配方")
	@Operation(summary = "获取配方", description = "获取配方")
	@PostMapping(value = "/getRecipe")
	public Result<?> getRecipe(@NotNull @RequestBody PiSistarRtSteps piSistarRtSteps) {
		String unitNames = piSistarRtSteps.getUnitNames();

		String pcuNos = piSistarRtSteps.getPcuNos();
		String[] pcuNoArr = null;
		if(pcuNos != null && pcuNos.contains(",")){
			pcuNoArr = pcuNos.split(",");
		}

		LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piSistarRtSteps.getAreaNo() != null, PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
		wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEventTime());
		wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
		wrapper.eq(piSistarRtSteps.getPcuNo() != null, PiSistarRtSteps::getPcuNo, piSistarRtSteps.getPcuNo());
		wrapper.eq(piSistarRtSteps.getReccatNo() != null,PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
		wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");

		if(pcuNoArr != null && pcuNoArr.length > 0){
			wrapper.in(PiSistarRtSteps::getPcuNo, pcuNoArr);
		}else{
			if(piSistarRtSteps.getPcuNo() == null){
				wrapper.eq(StrUtil.isNotBlank(pcuNos), PiSistarRtSteps::getPcuNo, pcuNos);
			}
		}

		wrapper.eq(Objects.nonNull(piSistarRtSteps.getUnitNo()), PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitNo());
		wrapper.orderByAsc(PiSistarRtSteps::getRecipeNo);

		//设备-查询条件
		if(StrUtil.isNotBlank(unitNames)){
			String [] unitName = unitNames.split(",");
			if (unitName!=null && unitName.length > 0) {
				wrapper.and(w -> {
					for (String name : unitName) {
						w.or().like(PiSistarRtSteps::getUnitName, name);
					}
				});
			}
		}

		List<PiSistarRtSteps> list = piSistarRtStepsService.list(wrapper);
		//去重
		List<PiSistarRtSteps> distinctListNew = list.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(
						step -> step.getAreaNo() + "_" + step.getReccatNo()+ "_" + step.getRecipeNo(), // 使用字符串拼接作为键
						step -> step,
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.collect(Collectors.toList());

		return Result.OK(distinctListNew);
	}

	/**
	 *  获取订单
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取订单")
	@Operation(summary = "获取订单", description = "获取订单")
	@PostMapping(value = "/getOrder")
	public Result<?> getOrder(@NotNull @RequestBody PiSistarRtSteps piSistarRtSteps) {
		LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
		wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getStartTime());
		wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
		wrapper.eq(PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
		wrapper.eq(PiSistarRtSteps::getRecipeNo, piSistarRtSteps.getRecipeNo());
		wrapper.eq(PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitNo());
		wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
		wrapper.orderByAsc(PiSistarRtSteps::getOrderNo);
		List<PiSistarRtSteps> list = piSistarRtStepsService.list(wrapper);
		//去除重复订单号
		List<PiSistarRtSteps> distinctAndSortedList = list.stream()
				.collect(Collectors.toMap(
						PiSistarRtSteps::getOrderNo,
						Function.identity(),
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.sorted(Comparator.comparing(PiSistarRtSteps::getOrderNo))
				.collect(Collectors.toList());
		return Result.OK(distinctAndSortedList);
	}

	/**
	 *  获取批次
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getBatch")
	public Result<?> getBatch(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		//String pcuNos = piSistarRtSteps.getPcuNos();
		String[] pcuNoArr = null;
//		if(pcuNos != null && pcuNos.contains(",")){
//			pcuNoArr = pcuNos.split(",");
//		}

		LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piSistarRtSteps.getAreaNo() != null, PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
		wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getStartTime());
		wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
		wrapper.eq(piSistarRtSteps.getReccatNo() != null, PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
		wrapper.eq(piSistarRtSteps.getRecipeNo() != null, PiSistarRtSteps::getRecipeNo, piSistarRtSteps.getRecipeNo());
		wrapper.eq(piSistarRtSteps.getOrderNo() != null, PiSistarRtSteps::getOrderNo, piSistarRtSteps.getOrderNo());
		wrapper.eq(piSistarRtSteps.getUnitNo() != null, PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitNo());
		wrapper.eq(PiSistarRtSteps::getPcuNo, piSistarRtSteps.getPcuNo());
//		if(pcuNoArr != null && pcuNoArr.length > 0){
//			wrapper.in(PiSistarRtSteps::getPcuNo, pcuNoArr);
//		}else{
//			wrapper.eq(StrUtil.isNotBlank(pcuNos), PiSistarRtSteps::getPcuNo, pcuNos);
//		}
		wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
		//wrapper.notLike(PiSistarRtSteps::getReccatName,"CIP");
		wrapper.orderByAsc(PiSistarRtSteps::getBatchNo);
		List<PiSistarRtSteps> list = piSistarRtStepsService.list(wrapper);
		//去除重复批次号
		List<PiSistarRtSteps> distinctAndSortedList = list.stream()
				.collect(Collectors.toMap(
						PiSistarRtSteps::getBatchNo,
						Function.identity(),
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				//.sorted(Comparator.comparing(PiSistarRtSteps::getBatchNo))
				.sorted(Comparator.comparing(step -> {
					try {
						return Integer.parseInt(step.getBatchNo());
					} catch (NumberFormatException e) {
						return Integer.MAX_VALUE;  // 非数字放到最后
					}
				}))
				.collect(Collectors.toList());
		return Result.OK(distinctAndSortedList);
	}


	/**
	 *  获取设备和点位
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/unitAndPoint")
	public Result<?> unitAndPoint(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		return Result.OK(baseService.unitAndPoint(piSistarRtSteps));
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
		//点位对应数据
		Map<String, List<MeasurementData>> pointMap = new HashMap<>();
		String pointJson = piSistarRtSteps.getPoints();

		List<OrderPointDto> jsonArray = JSONUtil.toList(pointJson, OrderPointDto.class);

		//List<String> pointList = Arrays.asList(points.split(","));
		//Date转 LocalDateTime
//		LocalDateTime startTime = timestampToLocalDate(piSistarRtSteps.getStartTime());
//		LocalDateTime endTime = timestampToLocalDate(piSistarRtSteps.getEndTime());

		//点位取值间隔(秒)
		int interval = 60;
		//时间轴取值间隔(分钟)
		int timeAxisInterval = 1;
		if(piSistarRtSteps.getIsFermentation()){
			// 取小时(发酵)
			interval = 3600; // 1小时
			timeAxisInterval = 60; // 1小时
		}

		Date minStartTime = null;
		Date maxEndTime = null;
		OrderPointDto maxDurationRecord = null;
		//标准曲线-点位集合
		List<OrderPointDto> standardtList = new ArrayList<>();
		//获取时间跨度最大点位
		long maxDuration = -1;
		for (OrderPointDto points : jsonArray) {
			Date startTime = points.getStartDate();
			Date endTime = points.getEndDate();
			if(startTime != null && endTime != null) {
				// 计算当前记录的时间跨度（毫秒）
				long duration = endTime.getTime() - startTime.getTime();
				// 更新最大时间跨度记录
				if (maxDurationRecord == null || duration > maxDuration) {
					maxDuration = duration;
					maxDurationRecord = points;
				}
			}
			if (points.getPointCode().contains("point_")){
				standardtList.add(points);
			}
		}

		// 需要获取标准曲线的点位
//		List<String> standardCurvePointList = new ArrayList<>();
		for (OrderPointDto points : jsonArray) {
			LocalDateTime startLocalTime = null;
			LocalDateTime endLocalTime = null;
			if(points.getStartDate() != null && points.getEndDate() != null){
				startLocalTime = timestampToLocalDate(points.getStartDate());
				endLocalTime = timestampToLocalDate(points.getEndDate());
			}else{
				if (maxDurationRecord != null) {
					startLocalTime = timestampToLocalDate(maxDurationRecord.getStartDate());
					endLocalTime = timestampToLocalDate(maxDurationRecord.getEndDate());
					points.setStartDate(maxDurationRecord.getStartDate());
					points.setEndDate(maxDurationRecord.getEndDate());
				}
			}
			if(startLocalTime == null || endLocalTime == null){
				continue;
			}
			List<MeasurementData> pointDataList = new ArrayList<>();
			List<String> pointList = new ArrayList<>();
			pointList.add(points.getPointCode());
//			if(points.getPointCode().contains("point_")){
//				//生产配置的标准曲线
//				standardCurvePointList.add(points.getPointCode());
//			}else{
//
//			}
			pointDataList = iPiComputationModelService.getMeasurementDataList(startLocalTime, endLocalTime ,pointList ,interval);

			//测试
//			List<MeasurementData> measurementDataList = new ArrayList<>();
//			Date startTime = points.getStartDate();
//			Date endTime = points.getEndDate();
//			List<MeasurementData> pointDataList = new ArrayList<>();
//			for (String point : pointList) {
//				pointDataList = MeasurementDataGenerator.generateMeasurementData(startTime,
//						endTime, point);
//				measurementDataList.addAll(pointDataList);
//			}

			//measurementDataList.addAll(pointDataList);
			if (StrUtil.isNotBlank(points.getOrderBatch())){
				pointMap.put(points.getOrderBatch()+"-"+points.getPointCode(), pointDataList);
			}else{
				pointMap.put(points.getPointCode(), pointDataList);
			}
		}

		// 生成标准曲线
		if(standardtList != null && standardtList.size() > 0 && maxDurationRecord != null){
			//生成标准曲线
			Map<String, List<MeasurementData>> standardCurvepointMap = generateStandardCurveMap(standardtList,
					maxDurationRecord.getStartDate(), timeAxisInterval);
			pointMap.putAll(standardCurvepointMap);
		}

		//获取时间范围
		List<Integer> intervals = new ArrayList<>();
		if(maxDurationRecord != null){
			minStartTime = maxDurationRecord.getStartDate();
			maxEndTime = maxDurationRecord.getEndDate();
			intervals = AdvancedDateTimeCalculator.calculateDynamicIntervals(minStartTime, maxEndTime, timeAxisInterval);
		}

		PointDataVo pointDataVO = new PointDataVo();
		pointDataVO.setPointData(pointMap);
		pointDataVO.setIntervals(intervals);
		return Result.OK(pointDataVO);
	}


	/**
	 * 生成标准曲线集合
	 * @param standardtList 标准曲线点位集合
	 * @param startTime 开始时间
	 * @param timeInterval 时间间隔(分钟)
	 * @return
	 */
	private Map<String, List<MeasurementData>> generateStandardCurveMap(List<OrderPointDto> standardtList, Date startTime
			,int timeInterval) {
		LocalDateTime localDateTime = startTime.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		Map<String, List<MeasurementData>> pointMap = new HashMap<>();
		for (OrderPointDto orderPointDto : standardtList) {
			String standardids = orderPointDto.getStandardids();
			String pointCode = orderPointDto.getPointCode();
			if(StrUtil.isBlank(standardids)){
				continue;
			}
			//获取曲线标准配置
			LambdaQueryWrapper<PiCurveStandard> wrapper = new LambdaQueryWrapper<>();
			wrapper.in(PiCurveStandard::getId, standardids.split( ","));
			wrapper.orderByAsc(PiCurveStandard::getSort);
			List<PiCurveStandard> piCurveStandardList = iPiCurveStandardService.list(wrapper);
			// 获取标准曲线
			List<Object[]> allPoints = generateStandardCurve(piCurveStandardList, localDateTime, timeInterval);
			List<MeasurementData> pointDataList = new ArrayList<>();
			for (Object[] allPoint : allPoints) {
				//对象转时间
//				Date date = null ;
					//date = DateUtils.parseDateTime(String.valueOf(allPoint[0]));
//				LocalDateTime localDate = (LocalDateTime) allPoint[0];
//				Instant instant = localDate.atZone(ZoneId.of("UTC")).toInstant();

				// point[0] 是 LocalDateTime 类型
				LocalDateTime baseTime = (LocalDateTime) allPoint[0];
				// 转换为 Instant
				Instant instant = baseTime.atZone(ZoneId.of("UTC")).toInstant();
				//date转Instant
				//Instant instant = date.toInstant();
				MeasurementData dataPoint = new MeasurementData(instant, pointCode, (Double) allPoint[1]);
				pointDataList.add(dataPoint);
			}
			pointMap.put(pointCode, pointDataList);
			//System.out.println(allPoints);
		}
		return pointMap;
	}

	/**
	 * 获取标准曲线
	 * @param piCurveStandardList 标准曲线集合
	 * @param startTime 开始时间
	 * @param timeInterval 时间间隔(分钟)
	 * @return
	 */
	private List<Object[]> generateStandardCurve(List<PiCurveStandard> piCurveStandardList, LocalDateTime startTime
			,int timeInterval) {
		List<Object[]> allPoints = new ArrayList<>();
		//参数名称
		String param = "";
		String unit = "";
		//Double lastEndValue = null;
		LocalDateTime currentStartTime = startTime;
		for (PiCurveStandard standard : piCurveStandardList) {
			if(StrUtil.isBlank(param)){
				param = standard.getParam();
			}
			if(StrUtil.isBlank(unit)){
				unit = standard.getUnit();
			}
			//标准起始值
			Double init = safeParseDouble(standard.getInitialValue());
			//标准结束值
			Double end = safeParseDouble(standard.getEndValue());
			//标准时长(小时/分钟) 将duration转换成小时
			Double duration = safeParseDouble(standard.getDuration());
			//标准类型
			Integer durationType = standard.getDurationType();
			if (durationType != null && durationType == 1) {
				duration = duration / 60.0;
			}
			if (StrUtil.isBlank(standard.getInitialValue())
					|| StrUtil.isBlank(standard.getEndValue())
					|| StrUtil.isBlank(standard.getDuration())
					|| standard.getDurationType() == null) {
				continue;
			}
			//上一步结束值设置为起点
			//double stepStartValue = (lastEndValue == null) ? init : lastEndValue;
			//标中配置的起始值
			double stepStartValue = init;

			// 生成这一段曲线
			List<Object[]> segment = generateStandardCurve(
					currentStartTime,
					stepStartValue,
					end,
					duration,
					timeInterval
			);
			allPoints.addAll(segment);
			// 去掉首点，防止与上一段重复
			if (!allPoints.isEmpty() && !segment.isEmpty()) {
				segment.remove(0);
			}

			// 更新游标
			currentStartTime = currentStartTime.plusMinutes((long) (duration * 60));
			// lastEndValue = end;
		}
		return allPoints;
	}





	//timestampToLocalDate Date转 LocalDateTime 方法
	private LocalDateTime timestampToLocalDate(Date timestampString) {
		try {
			LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampString.getTime()), ZoneId.systemDefault());
			return dateTime;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取数据
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取数据")
	@Operation(summary = "获取数据", description = "获取数据")
	@PostMapping(value = "/getData")
	public Result<?> getData(@NotNull @RequestBody PiSistarRtSteps piSistarRtSteps) {
		return Result.OK(baseService.getData(piSistarRtSteps));
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

		//读取标准曲线
		Map<String, List<PiCurveStandard>> standardMap = new HashMap<>();
		//步骤集合
		List<PiSistarRtSteps> stepList = piSistarRtSteps.getStepList();


		PointDataVo pointDataVO = new PointDataVo();
		//点位对应数据
		Map<String, List<Object[]>> pointMap = new HashMap<>();
		Map<String, String> pointNameMap = new HashMap<>();
		Map<String, String> pointUnitMap = new HashMap<>();
		String points = piSistarRtSteps.getPoints();
		List<String> pointList = Arrays.asList(points.split(","));
		//Date转 LocalDateTime 开始时间
		LocalDateTime startTime = timestampToLocalDate(piSistarRtSteps.getStartTime());
		LocalDateTime endTime = timestampToLocalDate(piSistarRtSteps.getEndTime());

		//获取点位
		LambdaQueryWrapper<PiBasePoint> basePointwrapper = new LambdaQueryWrapper<>();
		basePointwrapper.in(PiBasePoint::getPointCode, pointList);
		basePointwrapper.orderByAsc(PiBasePoint::getSort);
		List<PiBasePoint> basePointList = piBasePointService.list(basePointwrapper);

		//得到设备code和点位code map集合 key为设备code, value为点位code
		List<Integer> deviceIds = basePointList.stream()
				.map(PiBasePoint::getDeviceId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		//获取设备
		Map<Integer, String> deviceIdToCodeMap = new HashMap<>();
		if (deviceIds != null && !deviceIds.isEmpty()) {
			LambdaQueryWrapper<MdmDevice> deviceWrapper = new LambdaQueryWrapper<>();
			deviceWrapper.in(MdmDevice::getId, deviceIds);
			List<MdmDevice> deviceList = mdmDeviceService.list(deviceWrapper);
			deviceIdToCodeMap = deviceList.stream()
					.collect(Collectors.toMap(
							MdmDevice::getId,
							MdmDevice::getDeviceCode,
							(existing, replacement) -> existing
					));
		}
		//设备-点位对照
		Map<String, String> devicePointMap = new HashMap<>();
		for (PiBasePoint point : basePointList) {
			if (point.getDeviceId() != null && point.getPointCode() != null) {
				String deviceCode = deviceIdToCodeMap.get(point.getDeviceId());
				if (deviceCode != null) {
					// 设备编码去掉区域 如2-28-7  区域只保留 28-7
					int firstHyphen = deviceCode.indexOf("-");
					if (firstHyphen > 0) {
						deviceCode = deviceCode.substring(firstHyphen + 1);
					}
					devicePointMap.putIfAbsent(deviceCode, point.getPointCode());
				}
			}
		}

		if (stepList != null && stepList.size() > 0){
			PiSistarRtSteps firstStep = stepList.get(0);
			//获取标准曲线
			LambdaQueryWrapper<PiCurveStandard> standardwrapper = new LambdaQueryWrapper<>();
			standardwrapper.eq(PiCurveStandard::getAreaNo, firstStep.getPcuNo());
			standardwrapper.eq(PiCurveStandard::getUnitNo, firstStep.getUnitNo());
			standardwrapper.eq(PiCurveStandard::getRecipeNo, firstStep.getRecipeNo());
			//点位
			//String pointsStr = piSistarRtSteps.getPoints();
//			if (StrUtil.isNotBlank(pointsStr)) {
//				List<String> pointList = Arrays.asList(pointsStr.split(","));
//				standardwrapper.in(PiCurveStandard::getPointCode, pointList);
//			}

			//步骤和ropId 条件
			standardwrapper.and(w -> {
				for (PiSistarRtSteps step : stepList) {
					w.or(or -> or
							.eq(PiCurveStandard::getEopNo, step.getEopNo())
							.eq(PiCurveStandard::getRopId, step.getRopId())
					);
				}
			});

			standardwrapper.orderByAsc(PiCurveStandard::getParam).orderByAsc(PiCurveStandard::getSort);
			List<PiCurveStandard> list = iPiCurveStandardService.list(standardwrapper);
			// 排除list中设备编码在devicePointMap中匹配不上的, 匹配上的把pointCode同步上
			list = list.stream()
					.filter(standard -> StrUtil.isNotBlank(standard.getUnitNo()) &&
							StrUtil.isNotBlank(standard.getAreaNo()) && devicePointMap.containsKey(standard.getUnitNo()+"-"+standard.getAreaNo()))
					.peek(standard -> standard.setPointCode(devicePointMap.get(standard.getUnitNo()+"-"+standard.getAreaNo())))
					.collect(Collectors.toList());


			//通过参数分类
			standardMap = list.stream().collect(Collectors.groupingBy(PiCurveStandard::getParam));
		}

		//获取目标bindStandard 值集合(可能有多标准 需要拆分a,b 得到 a和b)
//		List<String> bindStandardList = basePointList.stream().map(PiBasePoint::getBindStandard).collect(Collectors.toList());
		List<String> bindStandardList = basePointList.stream()
				.map(PiBasePoint::getBindStandard)
				.filter(Objects::nonNull)
				.flatMap(bindStandard -> Arrays.stream(bindStandard.split(",")))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.distinct()  // 去重
				.collect(Collectors.toList());
		basePointList.forEach(point -> pointNameMap.put(point.getPointCode(), point.getPointName()));

		if (bindStandardList != null && !bindStandardList.isEmpty()){
			basePointwrapper = new LambdaQueryWrapper<>();
			basePointwrapper.in(PiBasePoint::getId, bindStandardList);
			basePointList = piBasePointService.list(basePointwrapper);
		}else{
			basePointList = new ArrayList<>();
		}

		pointList = new ArrayList<>(pointList);
		for (PiBasePoint piBasePoint : basePointList) {
			pointList.add(piBasePoint.getPointCode());
			pointNameMap.put(piBasePoint.getPointCode(), piBasePoint.getPointName());
		}
		//获取前一分钟数据
		LocalDateTime previousMinute = startTime.minusMinutes(1);
		List<MeasurementData> measurementDataList = iPiComputationModelService.getMeasurementDataList(previousMinute, endTime ,pointList ,60);

		//测试数据
//		List<MeasurementData> measurementDataList  = new ArrayList<>();
//		//遍历pointList
//		for (String point : pointList) {
//			List<MeasurementData> pointDataList = MeasurementDataGenerator.generateMeasurementData(piSistarRtSteps.getStartTime(),
//					piSistarRtSteps.getEndTime(), point);
//			measurementDataList.addAll(pointDataList);
//		}

		//循环点位
		for (String point : pointList) {
			List<Object[]> pointDataList = new ArrayList<>();
			boolean isFirst = true;
			for (MeasurementData measurementData : measurementDataList) {
				if(point.equals(measurementData.getCnlNum())){
					if(isFirst){
						//第一个点设置成开始时间
						measurementData.setTime(startTime.atZone(ZoneId.systemDefault()).toInstant());
						isFirst = false;
					}
					Object[] dataPoint = new Object[2];
					String timeStr = formatInstantToString(measurementData.getTime());
					dataPoint[0] = timeStr;
					dataPoint[1] = measurementData.getValue();
					pointDataList.add(dataPoint);
				}

			}

			pointMap.put(point, pointDataList);
		}

		//获取时间范围
//		List<String> timeRange = TimeRangeUtils.getMinuteTimeRange(startTime, endTime);

		//遍历standardMap
		//int num = 1;
		for (Map.Entry<String, List<PiCurveStandard>> entry : standardMap.entrySet()) {
			//String point = "point_"+ num;
			List<Object[]> allPoints = new ArrayList<>();
			//参数名称
			String param = "";
			String unit = "";
			String point = "";
			//Double lastEndValue = null;
			LocalDateTime currentStartTime = startTime;
			for (PiCurveStandard standard : entry.getValue()) {
				if(StrUtil.isBlank(param)){
					param = standard.getParam();
				}
				if(StrUtil.isBlank(unit)){
					unit = standard.getUnit();
				}
				if (StrUtil.isBlank(point)) {
					point = "point_"+standard.getPointCode();
				}
				//标准起始值
				Double init = safeParseDouble(standard.getInitialValue());
				//标准结束值
				Double end = safeParseDouble(standard.getEndValue());
				//标准时长(小时/分钟)
				Double duration = safeParseDouble(standard.getDuration());
				//标准时长类型(1分钟/2小时) 将duration转换成小时
				Integer durationType = standard.getDurationType();
				if (durationType != null && durationType == 1) {
					duration = duration / 60.0;
				}
				if (StrUtil.isBlank(standard.getInitialValue())
						|| StrUtil.isBlank(standard.getEndValue())
						|| StrUtil.isBlank(standard.getDuration())
						|| standard.getDurationType() == null) {
					continue;
				}
				//上一步结束值设置为起点
				//double stepStartValue = (lastEndValue == null) ? init : lastEndValue;
				//标中配置的起始值
				double stepStartValue = init;

				// 生成这一段曲线
				List<Object[]> segment = generateStandardCurve(
						currentStartTime,
						stepStartValue,
						end,
						duration,
						1
				);
				allPoints.addAll(segment);
				// 去掉首点，防止与上一段重复
				if (!allPoints.isEmpty() && !segment.isEmpty()) {
					segment.remove(0);
				}

				// 更新游标
				currentStartTime = currentStartTime.plusMinutes((long) (duration * 60));
				//lastEndValue = end;
			}

			//标准曲线
			pointMap.put(point, allPoints);
			//num++;
			//点位名称
			pointNameMap.put(point, param);
			//单位
			pointUnitMap.put(param, unit);
		}

		pointDataVO.setPointTimeData(pointMap);
		pointDataVO.setPointName(pointNameMap);
		pointDataVO.setPointUnit(pointUnitMap);
		return Result.OK(pointDataVO);
	}

	//计算标准曲线
	public static List<Object[]> generateStandardCurve(
			LocalDateTime startTime,
			double initialValue,
			double endValue,
			double durationHours,
			int timeInterval
	) {
		List<Object[]> result = new ArrayList<>();

		// 总分钟数
		int totalMinutes = (int) (durationHours * 60);
		if (totalMinutes <= 0 || timeInterval <= 0) {
			return result;
		}
		boolean wholeHour = isWholeHour(durationHours, timeInterval);
		LocalDateTime baseTime =
				wholeHour ? alignToHour(startTime) : startTime;

//		DateTimeFormatter formatter =
//				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// 总步数（间隔点数）
		int steps = totalMinutes / timeInterval;
		if (steps <= 0) {
			return result;
		}
		// 每一步的数值增量
		double stepValue = (endValue - initialValue) / steps;

		// 第一个点（起点）
		result.add(new Object[]{
				baseTime,
				round(initialValue, 1)
		});

		// 后续点
		for (int i = 1; i <= steps; i++) {
//			String utcTime = baseTime
//					.atZone(ZoneId.systemDefault())
//					.withZoneSameInstant(ZoneOffset.UTC)
//					.toInstant()
//					.toString();
			LocalDateTime time = baseTime
					.plusMinutes((long) i * timeInterval)
					.withSecond(0)
					.withNano(0);

			double value = initialValue + stepValue * i;

			result.add(new Object[]{
					time,
					round(value, 1)
			});
		}
		return result;
	}

	/**
	 * 将时间对齐到整小时
	 */
	private static LocalDateTime alignToHour(LocalDateTime time) {
		if (time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0) {
			return time;
		}
		return time
				.withMinute(0)
				.withSecond(0)
				.withNano(0)
				.plusHours(1);
	}

	/**
	 * 判断是否整小时
	 * @param durationHours
	 * @param timeInterval
	 * @return
	 */
	private static boolean isWholeHour(double durationHours, int timeInterval) {
		return timeInterval == 60 && durationHours % 1 == 0;
	}

	//保留小数
	private static double round(double value, int scale) {
		return BigDecimal.valueOf(value)
				.setScale(scale, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private static Double safeParseDouble(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}





	/**
	 * 将Instant格式化为字符串 '2025-08-16 02:25:00'
	 */
	private String formatInstantToString(Instant instant) {
		return instant.atZone(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 *  获取设备
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取设备")
	@Operation(summary = "获取设备", description = "获取设备")
	@PostMapping(value = "/getDevice")
	public Result<?> getDevice(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		return Result.OK(baseService.getDevice(piSistarRtSteps));
	}

	/**
	 *  获取糖化锅最新批次数据
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取糖化锅最新批次数据")
	@Operation(summary = "获取糖化锅最新批次数据", description = "获取糖化锅最新批次数据")
	@PostMapping(value = "/getMashingData")
	public Result<?> getMashingData(@RequestBody PiRaStep piRaStep) {
		Optional<PiSistarRtSteps> optional = piSistarRtStepsService.lambdaQuery()
				.like(PiSistarRtSteps::getUnitName, piRaStep.getUnitName())
		        .notLike(PiSistarRtSteps::getRecipeName,"CIP")
				.orderByDesc(PiSistarRtSteps::getEventTime)
				.last("LIMIT 1")
				.oneOpt();
		PiSistarRtSteps piSistarRtStep = optional.orElse(null);

		//获取上一批次
		PiSistarRtSteps lastPiSistarRtStep = piSistarRtStepsService.lambdaQuery()
				.le(PiSistarRtSteps::getStartTime, piSistarRtStep.getStartTime())
				.like(PiSistarRtSteps::getUnitName, piRaStep.getUnitName())
		        .notLike(PiSistarRtSteps::getRecipeName,"CIP")
				.ne(PiSistarRtSteps::getBatchNo, piSistarRtStep.getBatchNo())
				.orderByDesc(PiSistarRtSteps::getEventTime)
				.last("LIMIT 1")
				.oneOpt()
				.orElse(null);


		// 获取设备id
		LambdaQueryWrapper<MdmDevice> wrapperDevice = new LambdaQueryWrapper<>();
		wrapperDevice.like(MdmDevice::getDeviceCode, "-");
		List<MdmDevice> deviceList = mdmDeviceService.list(wrapperDevice);
		//获取设备code 和 设备对象 对照map
		Map<String, MdmDevice> deviceMap = deviceList.stream()
			.collect(Collectors.toMap(
					MdmDevice::getDeviceCode,  // key mapper
					device -> device,          // 使用设备对象本身作为value
					(existing, replacement) -> existing  // 保留已有值，忽略新值
			));
		String key = lastPiSistarRtStep.getAreaNo() + "-" + lastPiSistarRtStep.getUnitNo() + "-" + lastPiSistarRtStep.getPcuNo();
		if(lastPiSistarRtStep != null && deviceMap.containsKey(key)){
			MdmDevice mdmDevice = (deviceMap.get(key));
			lastPiSistarRtStep.setUnitNames(mdmDevice.getId().toString());
			lastPiSistarRtStep.setLineCode(mdmDevice.getLineCode());
		}
		return Result.OK(lastPiSistarRtStep);
	}


	@AutoLog(value = "保存步骤配置")
	@Operation(summary = "保存步骤配置", description = "保存步骤配置")
	@PostMapping(value = "/stepsSave")
	public Result<?> stepsSave(@RequestBody PiCurveStep piCurveStep) {
		baseService.stepsSave(piCurveStep);
		return Result.OK("保存成功!");
	}
}
