package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.dto.OrderPointDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.AdvancedDateTimeCalculator;
import com.keytech.modules.pi.util.DateConvertUtil;
import com.keytech.modules.pi.util.MeasurementDataGenerator;
import com.keytech.modules.pi.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: RA-步骤
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Tag(name="RA-步骤")
@RestController
@RequestMapping("/pi/ra/piRaStep")
@Slf4j
public class PiRaStepController extends BaseController<IPiRaStepService, PiRaStep> {
	private static final Logger logger = LoggerFactory.getLogger(PiRaStepController.class);
	@Autowired
	private IPiRaStepService ipiRaStepService;
	@Autowired
	private IPiRaProcedureService iPiRaProcedureService;
	@Autowired
	private IMdmDeviceService mdmDeviceService;
	@Autowired
	private IPiBasePointService piBasePointService;
	@Autowired
	private IPiComputationModelService iPiComputationModelService;
	@Autowired
	private IPiRaUnitService iPiRaUnitService;
	@Autowired
	private IPiRaBopService iPiRaBopService;
	@Autowired
	private IPiRaStepParamService iPiRaStepParamService;
	@Autowired
	private IPiCurveStandardService iPiCurveStandardService;

	/**
	 * 定时任务接口-RA步骤抽取数据抽取
	 */
	@AutoLog(value = "定时任务接口-RA步骤抽取")
	@Operation(summary = "定时任务接口-RA步骤抽取", description = "定时任务接口-RA步骤抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("RA步骤数据抽取执行---ok");
		ipiRaStepService.extractData();
		logger.info("RA步骤数据抽取---ok,");
		return Result.OK("调用成功！");
	}

	/**
	 *  定时任务接口-RA步骤数据补抽(结束时间-通过参数获取)
	 * @param map
	 * @return
	 */
	@AutoLog(value = "定时任务接口-RA步骤数据补抽")
	@Operation(summary = "定时任务接口-RA步骤数据补抽", description = "定时任务接口-RA步骤数据补抽")
	@PostMapping(value = "/retrieveData")
	public Result<String> retrieveData(@RequestBody Map map) {
		logger.info("RA步骤数据补抽执行---ok");
		//通过参数获取结束时间
		ipiRaStepService.retrieveData(map);
		//通过上一步获取结束时间
		ipiRaStepService.retrieveEndTime(map);
		logger.info("RA步骤数据补抽---ok,");
		return Result.OK("调用成功！");
	}

	/**
	 *  定时任务接口-RA步骤数据补抽结束时间(按下一次开始时间)
	 * @param map
	 * @return
	 */
	@AutoLog(value = "定时任务接口-RA步骤数据补抽结束时间")
	@Operation(summary = "定时任务接口-RA步骤数据补抽结束时间", description = "定时任务接口-RA步骤数据补抽结束时间")
	@PostMapping(value = "/retrieveEndTime")
	public Result<String> retrieveEndTime(@RequestBody Map map) {
		logger.info("RA步骤数据补抽结束时间执行---ok");
		ipiRaStepService.retrieveEndTime(map);
		logger.info("RA步骤数据补抽结束时间---ok,");
		return Result.OK("调用成功！");
	}

	/**
	 *  获取配方
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取配方")
	@Operation(summary = "获取配方", description = "获取配方")
	@PostMapping(value = "/getRecipe")
	public Result<?> getRecipe(@NotNull @RequestBody PiRaStep piRaStep) {
		LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piRaStep.getAreaId() != null, PiRaStep::getAreaId, piRaStep.getAreaId());
		wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
		wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
		wrapper.eq(Objects.nonNull(piRaStep.getUnitId()), PiRaStep::getUnitId, piRaStep.getUnitId());
		wrapper.orderByAsc(PiRaStep::getRecipeId);
		List<PiRaStep> list = ipiRaStepService.list(wrapper);
		//去重
		List<PiRaStep> distinctListNew = list.stream()
				.filter(Objects::nonNull)
				.map(step -> {
					// 处理 recipeName 去掉/后内容
					if (step.getRecipeName() != null && step.getRecipeName().contains("/")) {
						step.setRecipeName(
								step.getRecipeName().substring(0, step.getRecipeName().indexOf("/"))
						);
					}
					return step;
				})
				.collect(Collectors.toMap(
						step -> step.getAreaId() + "_" + step.getRecipeId(),
						Function.identity(),
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.collect(Collectors.toList());

		return Result.OK(distinctListNew);
	}


	/**
	 *  获取订单
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取订单")
	@Operation(summary = "获取订单", description = "获取订单")
	@PostMapping(value = "/getOrder")
	public Result<?> getOrder(@NotNull @RequestBody PiRaStep piRaStep) {
		LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piRaStep.getAreaId() != null, PiRaStep::getAreaId, piRaStep.getAreaId());
		wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
		wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
		wrapper.eq(piRaStep.getRecipeId() != null, PiRaStep::getRecipeId, piRaStep.getRecipeId());
		wrapper.eq(Objects.nonNull(piRaStep.getUnitId()), PiRaStep::getUnitId, piRaStep.getUnitId());
		wrapper.orderByAsc(PiRaStep::getOrderId);
		List<PiRaStep> list = ipiRaStepService.list(wrapper);
		//去除重复订单号
		List<PiRaStep> distinctAndSortedList = list.stream()
				.collect(Collectors.toMap(
						PiRaStep::getOrderId,
						Function.identity(),
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.sorted(Comparator.comparing(PiRaStep::getOrderId))
				.collect(Collectors.toList());
		return Result.OK(distinctAndSortedList);
	}

	/**
	 *  获取批次
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getBatch")
	public Result<?> getBatch(@RequestBody PiRaStep piRaStep) {
		LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(piRaStep.getAreaId() != null,PiRaStep::getAreaId, piRaStep.getAreaId());
		wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
		wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
		wrapper.eq(piRaStep.getRecipeId() != null, PiRaStep::getRecipeId, piRaStep.getRecipeId());
		//wrapper.eq(PiRaStep::getOrderId, piRaStep.getOrderId());
		wrapper.eq(Objects.nonNull(piRaStep.getUnitId()), PiRaStep::getUnitId, piRaStep.getUnitId());
		wrapper.orderByAsc(PiRaStep::getBatchNr);

		List<PiRaStep> list = ipiRaStepService.list(wrapper);
		//去除重复批次号
		List<PiRaStep> distinctAndSortedList = list.stream()
				.collect(Collectors.toMap(
						PiRaStep::getBatchNr,
						Function.identity(),
						(existing, replacement) -> existing
				))
				.values()
				.stream()
				.sorted(Comparator.comparing(PiRaStep::getBatchNr))
				.collect(Collectors.toList());
		return Result.OK(distinctAndSortedList);
	}


	/**
	 * 获取数据
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取数据")
	@Operation(summary = "获取数据", description = "获取数据")
	@PostMapping(value = "/getData")
	public Result<?> getData(@NotNull @RequestBody PiRaStep piRaStep) {
		return Result.OK(ipiRaStepService.getData(piRaStep));

	}


	/**
	 *  获取设备和点位
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/pointData")
	public Result<?> pointData(@RequestBody PiRaStep piRaStep) {

		//读取标准曲线
		Map<String, List<PiCurveStandard>> standardMap = new HashMap<>();
		//步骤集合 ******[新增字段]
		List<PiRaStep> stepList = piRaStep.getStepList();


		PointDataVo pointDataVO = new PointDataVo();
		//点位对应数据
		Map<String, List<Object[]>> pointMap = new HashMap<>();
		Map<String, String> pointNameMap = new HashMap<>();
		Map<String, String> pointUnitMap = new HashMap<>();
		String points = piRaStep.getPoints();
		List<String> pointList = Arrays.asList(points.split(","));
		//Date转 LocalDateTime
//		LocalDateTime startTime = DateConvertUtil.timestampToLocalDate(piRaStep.getEventTime());
//		LocalDateTime endTime = DateConvertUtil.timestampToLocalDate(piRaStep.getEndTime());

		LocalDateTime startTime = DateConvertUtil.timestampToLocalDate(piRaStep.getStartTime());
		LocalDateTime endTime = DateConvertUtil.timestampToLocalDate(piRaStep.getEndTime());

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
					// 设备编码 区域-设备编码
//					int firstHyphen = deviceCode.indexOf("-");
//					if (firstHyphen > 0) {
//						deviceCode = deviceCode.substring(firstHyphen + 1);
//					}
					devicePointMap.putIfAbsent(deviceCode, point.getPointCode());
				}
			}
		}


		if (stepList != null && stepList.size() > 0){
			PiRaStep firstStep = stepList.get(0);
			//获取标准曲线
			LambdaQueryWrapper<PiCurveStandard> standardwrapper = new LambdaQueryWrapper<>();
			standardwrapper.eq(PiCurveStandard::getAreaNo, firstStep.getAreaId());
			standardwrapper.eq(PiCurveStandard::getUnitNo, firstStep.getUnitId());
			standardwrapper.eq(PiCurveStandard::getRecipeNo, firstStep.getRecipeId());
			//点位
//			String pointsStr = piRaStep.getPoints();
//			if (StrUtil.isNotBlank(pointsStr)) {
//				List<String> pointList = Arrays.asList(pointsStr.split(","));
//				standardwrapper.in(PiCurveStandard::getPointCode, pointList);
//			}

			//步骤 条件
			standardwrapper.and(w -> {
				for (PiRaStep step : stepList) {
					w.or(or -> or
							.eq(PiCurveStandard::getEopNo, step.getBopId())
							//.eq(PiCurveStandard::getRopId, step.getRopId())
					);
				}
			});

			standardwrapper.orderByAsc(PiCurveStandard::getParam).orderByAsc(PiCurveStandard::getSort);
			List<PiCurveStandard> list = iPiCurveStandardService.list(standardwrapper);
			// 排除list中设备编码在devicePointMap中匹配不上的, 匹配上的把pointCode同步上
			list = list.stream()
					.filter(standard -> StrUtil.isNotBlank(standard.getUnitNo()) &&
							StrUtil.isNotBlank(standard.getAreaNo()) && devicePointMap.containsKey(standard.getAreaNo()+"-"+standard.getUnitNo()))
					.peek(standard -> standard.setPointCode(devicePointMap.get(standard.getAreaNo()+"-"+standard.getUnitNo())))
					.collect(Collectors.toList());

			//通过参数分类
			standardMap = list.stream()
					.filter(item -> StrUtil.isNotBlank(item.getParam()))
					.collect(Collectors.groupingBy(PiCurveStandard::getParam));
		}


		//获取目标bindStandard 值集合 (可能有多标准 需要拆分a,b 得到 a和b)
		//List<String> bindStandardList = basePointList.stream().map(PiBasePoint::getBindStandard).collect(Collectors.toList());
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
//			List<MeasurementData> pointDataList = MeasurementDataGenerator.generateMeasurementData(piRaStep.getEventTime(),
//					piRaStep.getEndTime(), point);
//			measurementDataList.addAll(pointDataList);
//		}

		//点位
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
					String timeStr = DateConvertUtil.formatInstantToString(measurementData.getTime());
					dataPoint[0] = timeStr;
					dataPoint[1] = measurementData.getValue();
					pointDataList.add(dataPoint);
				}

			}

			pointMap.put(point, pointDataList);
		}
		//获取时间范围
//		List<String> timeRange = TimeRangeUtils.getMinuteTimeRange(startTime, endTime);

		for (Map.Entry<String, List<PiCurveStandard>> entry : standardMap.entrySet()) {
			//String point = "point_"+ num;
			List<Object[]> allPoints = new ArrayList<>();
			//参数名称
			String param = "";
			String unit = "";
			String point = "";
			//Double lastEndValue = null;

			//开始时间和步骤对齐：通过标准的第一个步骤在步骤集合中匹配的开始时间
			LocalDateTime currentStartTime = startTime;
			//找到标准的第一个步骤(sort最小的)，在stepList中匹配bopId，获取其eventTime作为开始时间
			List<PiCurveStandard> standardList = entry.getValue();
			if (standardList != null && !standardList.isEmpty() && stepList != null && !stepList.isEmpty()) {
				//标准已按sort排序，第一个就是sort最小的
				//PiCurveStandard firstStandard = standardList.get(0);
				PiCurveStandard firstStandard = standardList.stream()
						.filter(s -> s.getInitialValue() != null && !s.getInitialValue().isEmpty()
								&& s.getEndValue() != null && !s.getEndValue().isEmpty()
								&& s.getDuration() != null && !s.getDuration().isEmpty())
						.findFirst()
						.orElse(null);
				if (firstStandard == null) {
					continue;
				}

				String firstEopNo = firstStandard.getEopNo();
				//在stepList中找到与标准步骤号(eopNo)匹配的步骤(bopId)
				for (PiRaStep step : stepList) {
					if (step.getBopId() != null && String.valueOf(step.getBopId()).equals(firstEopNo)) {
						//匹配成功，使用该步骤的eventTime作为开始时间
						if (step.getEventTime() != null) {
							currentStartTime = DateConvertUtil.timestampToLocalDate(step.getEventTime());
						}
						break;
					}
				}
			}
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
				//标准时长(小时) 为空默认设置成0

				Double duration = safeParseDouble(standard.getDuration());
				if (duration == null) {
					duration = 0.0;
				}
				//标准时长类型
				Integer durationType = standard.getDurationType();
				if (durationType != null && durationType == 1) {
					//分钟
					duration = duration / 60.0;
				}

				if (StrUtil.isBlank(standard.getInitialValue())
						|| StrUtil.isBlank(standard.getEndValue())
						|| StrUtil.isBlank(standard.getDuration())
						|| durationType == null) {
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
				// 去掉首点，防止与上一段重复（先移除再添加）
				if (!allPoints.isEmpty() && !segment.isEmpty()) {
					segment.remove(0);
				}
				allPoints.addAll(segment);

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
			//标准时长
			Double duration = safeParseDouble(standard.getDuration());
			if (duration == null) {
				duration = 0.0;
			}
			//标准时长类型(1分钟/2小时)，默认小时
			Integer durationType = standard.getDurationType();
			//统一转换为小时传给底层方法
			double durationHours;
			long durationMinutes;
			if (durationType != null && durationType == 1) {
				//分钟
				durationHours = duration / 60.0;
				durationMinutes = duration.longValue();
			} else {
				//小时
				durationHours = duration;
				durationMinutes = (long) (duration * 60);
			}

			if (StrUtil.isBlank(standard.getInitialValue())
					|| StrUtil.isBlank(standard.getEndValue())
					|| StrUtil.isBlank(standard.getDuration())) {
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
					durationHours,
					timeInterval
			);
			// 去掉首点，防止与上一段重复（先移除再添加）
			if (!allPoints.isEmpty() && !segment.isEmpty()) {
				segment.remove(0);
			}
			allPoints.addAll(segment);

			// 更新游标
			currentStartTime = currentStartTime.plusMinutes(durationMinutes);
			// lastEndValue = end;
		}
		return allPoints;
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
				wholeHour ? alignToHour(startTime) : startTime.withSecond(0).withNano(0);

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

	//保留小数
	private static double round(double value, int scale) {
		return BigDecimal.valueOf(value)
				.setScale(scale, RoundingMode.HALF_UP)
				.doubleValue();
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

	/**
	 *  获取批次
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getRtBatches")
	public Result<?> getRtBatches(@RequestBody PiRaStep piRaStep) {
		LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
		//wrapper.eq(PiSistarRtBatches::getAreaId, piSistarRtBatches.getAreaId());
		wrapper.eq(PiRaStep::getAreaId, piRaStep.getAreaId());
		wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
		wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
		wrapper.eq(PiRaStep::getRecipeId, piRaStep.getRecipeId());

		if(StrUtil.isNotBlank(piRaStep.getUnitId())){
			//判断是否包含,
			if(piRaStep.getUnitId().contains(",")){
				wrapper.in(PiRaStep::getUnitId, piRaStep.getUnitId().split(","));
			}else {
				wrapper.eq(PiRaStep::getUnitId, piRaStep.getUnitId());
			}
		}
		wrapper.orderByAsc(PiRaStep::getOrderId).orderByAsc(PiRaStep::getBatchNr);

		List<PiRaStep> list = ipiRaStepService.list(wrapper);
		// 遍历根据 订单orderId 批次batchNr 区域areaId 去重 得到集合List<OrderBatchVO>
//		List<OrderBatchVo> distinctAndSortedList = list.stream().map(item -> {
//			OrderBatchVo orderBatchVO = new OrderBatchVo();
//			orderBatchVO.setOrderNo(item.getOrderId());
//			orderBatchVO.setBatchNo(item.getBatchNr());
//			orderBatchVO.setAreaNo(item.getAreaId());
//			orderBatchVO.setStartTime(item.getEventTime());
//			orderBatchVO.setEndTime(item.getEndTime());
//			return orderBatchVO;
//			}).distinct()
//			.sorted(Comparator.comparing(OrderBatchVo::getOrderNo))
//			.collect(Collectors.toList());

		return Result.OK(deduplicate(list));
	}


	/**
	 * 根据 orderNo 和 batchNo 分组，然后在每个组内取 startTime 的最小值和 endTime 的最大值
	 * @param list
	 * @return
	 */
	public List<OrderBatchVo> deduplicate(List<PiRaStep> list) {
//		return list.stream()
//				.collect(Collectors.groupingBy(
//						item -> item.getOrderId() + "_" + item.getBatchId(),
//						Collectors.collectingAndThen(
//								Collectors.toList(),
//								group -> {
//									// 按 startTime 排序
//									group.sort(Comparator.comparing(PiRaStep::getEventTime));
//									// 取第一个元素的 startTime 和最后一个元素的 endTime
//									PiRaStep first = group.get(0);
//									PiRaStep last = group.get(group.size() - 1);
//									// 创建新的对象或修改现有对象
//									OrderBatchVo result = new OrderBatchVo();
//									result.setOrderNo(first.getOrderId());
//									result.setBatchNo(first.getBatchNr());
//									result.setStartTime(first.getEventTime());
//									result.setEndTime(last.getEndTime());
//									return result;
//								}
//						)
//				))
//				.values()
//				.stream()
//				.collect(Collectors.toList());

		return list.stream()
				.collect(Collectors.groupingBy(
					item -> item.getOrderId() + "_" + item.getBatchId(),
					Collectors.collectingAndThen(
						Collectors.toList(),
						group -> {
							// 按 eventTime 排序
							group.sort(Comparator.comparing(PiRaStep::getEventTime));

							PiRaStep first = group.get(0);
							PiRaStep last = group.get(group.size() - 1);

							OrderBatchVo result = new OrderBatchVo();
							result.setOrderNo(first.getOrderId());
							result.setBatchNo(first.getBatchNr());
							result.setStartTime(first.getStartTime());
							result.setEndTime(last.getEndTime());
							return result;
						}
					)
				))
				.values()
				.stream()
				// 按 orderId、batchNr 排序（直接使用 int 类型）
				.sorted(
					Comparator.comparingInt(OrderBatchVo::getOrderNo)
					 .thenComparingInt(OrderBatchVo::getBatchNo)
				)
				.collect(Collectors.toList());
	}

	/**
	 *  获取设备和点位
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取设备和点位")
	@Operation(summary = "获取设备和点位", description = "获取设备和点位")
	@PostMapping(value = "/unitAndPoint")
	public Result<?> unitAndPoint(@RequestBody PiRaStep piRaStep) {
		return Result.OK(baseService.unitAndPoint(piRaStep));
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

		//点位取值间隔(秒)
		int interval = 60;
		//时间轴取值间隔(分钟)
		int timeAxisInterval = 1;
		if(piSistarRtSteps.getIsFermentation()){
			interval = 60*60;
			timeAxisInterval = 60;
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
			if(startTime != null && endTime != null){
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


		for (OrderPointDto points : jsonArray) {

			LocalDateTime startLocalTime = null;
			LocalDateTime endLocalTime = null;
			if(points.getStartDate() != null && points.getEndDate() != null){
				startLocalTime = DateConvertUtil.timestampToLocalDate(points.getStartDate());
				endLocalTime = DateConvertUtil.timestampToLocalDate(points.getEndDate());
			}else{
				if (maxDurationRecord != null){
					startLocalTime = DateConvertUtil.timestampToLocalDate(maxDurationRecord.getStartDate());
					endLocalTime = DateConvertUtil.timestampToLocalDate(maxDurationRecord.getEndDate());
					points.setStartDate(maxDurationRecord.getStartDate());
					points.setEndDate(maxDurationRecord.getEndDate());
				}
			}
			if(startLocalTime == null || endLocalTime == null){
				continue;
			}
			List<String> pointList = new ArrayList<>();
			pointList.add(points.getPointCode());
			List<MeasurementData> pointDataList = iPiComputationModelService.getMeasurementDataList(startLocalTime, endLocalTime ,pointList ,interval);


			//测试
			//List<MeasurementData> measurementDataList = new ArrayList<>();
//			Date startTime = points.getStartDate();
//			Date endTime = points.getEndDate();
//			if(points.getStartDate() != null && points.getEndDate() != null){
//				startTime = maxDurationRecord.getStartDate();
//				endTime = maxDurationRecord.getEndDate();
//			}
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
			// //开始时间和步骤对齐：通过标准的第一个步骤在步骤集合中匹配的开始时间
			Date standardStartTime = maxDurationRecord.getStartDate();
			//获取步骤集合
			List<PiSistarRtSteps> stepList = piSistarRtSteps.getStepList();
			if (stepList != null && !stepList.isEmpty()) {
				//获取第一个标准的standardids
				String standardids = standardtList.get(0).getStandardids();
				if (StrUtil.isNotBlank(standardids)) {
					//查询标准曲线配置，获取第一个步骤
					LambdaQueryWrapper<PiCurveStandard> wrapper = new LambdaQueryWrapper<>();
					wrapper.in(PiCurveStandard::getId, standardids.split(","));
					wrapper.orderByAsc(PiCurveStandard::getSort);
					wrapper.last("limit 1");
					PiCurveStandard firstStandard = iPiCurveStandardService.getOne(wrapper);
					if (firstStandard != null && StrUtil.isNotBlank(firstStandard.getEopNo())) {
						//在stepList中匹配eopNo
						for (PiSistarRtSteps step : stepList) {
							String stepEopNo = step.getEopNo() != null ? String.valueOf(step.getEopNo()) : null;
							if (stepEopNo != null && stepEopNo.equals(firstStandard.getEopNo())) {
								//匹配成功，优先使用eventTime，兼容前端StepVo的startTime
								Date matchTime = step.getEventTime() != null ? step.getEventTime() : step.getStartTime();
								if (matchTime != null) {
									standardStartTime = matchTime;
								}
								break;
							}
						}
					}
				}
			}

			//生成标准曲线
			Map<String, List<MeasurementData>> standardCurvepointMap = generateStandardCurveMap(standardtList,
					standardStartTime, timeAxisInterval, maxDurationRecord.getStartDate());
			pointMap.putAll(standardCurvepointMap);
		}

		List<Integer> intervals = new ArrayList<>();
		if(maxDurationRecord != null){
			minStartTime = maxDurationRecord.getStartDate();
			maxEndTime = maxDurationRecord.getEndDate();
			//获取时间范围
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
	 * @param startTime 标准曲线开始时间（匹配步骤的开始时间）
	 * @param timeInterval 时间间隔(分钟)
	 * @param chartStartTime 图表整体开始时间（用于前置补null）
	 * @return
	 */
	private Map<String, List<MeasurementData>> generateStandardCurveMap(List<OrderPointDto> standardtList, Date startTime
			,int timeInterval, Date chartStartTime) {
		LocalDateTime localDateTime = startTime.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.withSecond(0).withNano(0);
		LocalDateTime chartStartLocal = chartStartTime.toInstant()
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
			List<Object[]> allPoints = generateStandardCurve(piCurveStandardList, localDateTime, timeInterval);
			List<MeasurementData> pointDataList = new ArrayList<>();

			//前置补位：从图表开始时间到标准曲线开始时间，填充null值
			LocalDateTime paddingTime = chartStartLocal;
			while (paddingTime.isBefore(localDateTime)) {
				Instant paddingInstant = paddingTime.atZone(ZoneId.of("UTC")).toInstant();
				pointDataList.add(new MeasurementData(paddingInstant, pointCode, null));
				paddingTime = paddingTime.plusMinutes(timeInterval);
			}

			for (Object[] allPoint : allPoints) {
				// point[0] 是 LocalDateTime 类型
				LocalDateTime baseTime = (LocalDateTime) allPoint[0];
				// 转换为 Instant
				Instant instant = baseTime.atZone(ZoneId.of("UTC")).toInstant();
				MeasurementData dataPoint = new MeasurementData(instant, pointCode, (Double) allPoint[1]);
				pointDataList.add(dataPoint);
			}
			pointMap.put(pointCode, pointDataList);
			//System.out.println(allPoints);
		}
		return pointMap;
	}

	/**
	 *  获取产线
	 * @param areaId
	 * @return
	 */
	@AutoLog(value = "获取产线")
	@Operation(summary = "获取产线", description = "获取配方")
	@GetMapping(value = "/getLine")
	public Result<?> getLine(@RequestParam(name="areaId") Integer areaId) {
		List<RaLineVo> list = ipiRaStepService.getLine(areaId);
		return Result.OK(list);
	}


	/**
	 *  获取设备
	 * @param areaId
	 * @return
	 */
	@AutoLog(value = "获取设备")
	@Operation(summary = "获取设备", description = "获取配方")
	@GetMapping(value = "/getUnit")
	public Result<?> getUnit(@RequestParam(name="areaId") Integer areaId,@RequestParam(name="lineId") Integer lineId) {
		LambdaQueryWrapper<PiRaUnit> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PiRaUnit::getLineId, lineId);
		wrapper.eq(PiRaUnit::getAreaId, areaId);
		List<PiRaUnit> list = iPiRaUnitService.list(wrapper);
		return Result.OK(list);
	}

	/**
	 *  获取步骤
	 * @param areaId
	 * @return
	 */
	@AutoLog(value = "获取步骤")
	@Operation(summary = "获取步骤", description = "获取步骤")
	@GetMapping(value = "/getRaEop")
	public Result<?> getRaEop(@RequestParam(name="areaId") Integer areaId,@RequestParam(name="lineId") Integer lineId ,@RequestParam(name="unitId") Integer unitId) {
		LambdaQueryWrapper<PiRaBop> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PiRaBop::getAreaId, areaId);
		wrapper.eq(PiRaBop::getUnitId, unitId);
		List<PiRaBop> list = iPiRaBopService.list(wrapper);
		return Result.OK(list);
	}


	/**
	 *  获取步骤
	 * @param areaId
	 * @return
	 */
	@AutoLog(value = "获取步骤参数")
	@Operation(summary = "获取步骤", description = "获取步骤参数")
	@GetMapping(value = "/getRaEopParam")
	public Result<?> getRaEopParam(@RequestParam(name="areaId") Integer areaId,@RequestParam(name="bopId") Integer bopId ,@RequestParam(name="unitId") Integer unitId) {
		LambdaQueryWrapper<PiRaStepParam> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PiRaStepParam::getAreaId, areaId);
		wrapper.eq(PiRaStepParam::getUnitId, unitId);
		wrapper.eq(PiRaStepParam::getBopId, bopId);
		wrapper.select(PiRaStepParam::getParaName); // 只选择需要去重的字段
		List<PiRaStepParam> list = iPiRaStepParamService.list(wrapper);
		// 去重
		List<PiRaStepParam> distinctList = list.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PiRaStepParam::getParaName))),
						ArrayList::new
				));
		return Result.OK(distinctList);
	}


	/**
	 *  获取设备
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getDevice")
	public Result<?> getDevice(@RequestBody PiRaStep piRaStep) {
		return Result.OK(baseService.getDevice(piRaStep));

	}

	/**
	 * 获取下一批/上一批
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取下一批/上一批")
	@Operation(summary = "获取下一批/上一批", description = "获取下一批/上一批")
	@PostMapping(value = "/getBatchByDirection")
	public Result<?> getBatchByDirection(@NotNull @RequestBody PiRaStep piRaStep) {
		List<PiRaStep> list = ipiRaStepService.getBatchByDirection(piRaStep);
		return Result.OK(list);
	}

	/**
	 * 保存步骤配置
	 * @param piCurveStep
	 * @return
	 */
	@AutoLog(value = "保存步骤配置")
	@Operation(summary = "保存步骤配置", description = "保存步骤配置")
	@PostMapping(value = "/stepsSave")
	public Result<?> stepsSave(@RequestBody PiCurveStep piCurveStep) {
		baseService.stepsRaSave(piCurveStep);
		return Result.OK("保存成功!");
	}

	/**
	 * 获取其他参数
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取其他参数")
	@Operation(summary = "获取其他参数", description = "获取其他参数")
	@PostMapping(value = "/getOtherParam")
	public Result<?> getOtherParam(@RequestBody PiRaStep piRaStep) {
		return Result.OK(baseService.getOtherParam(piRaStep));
	}

	/**
	 *  获取糖化锅最新批次数据(上一批次)
	 * @param piRaStep
	 * @return
	 */
	@AutoLog(value = "获取糖化锅最新批次数据")
	@Operation(summary = "获取糖化锅最新批次数据", description = "获取糖化锅最新批次数据")
	@PostMapping(value = "/getMashingData")
	public Result<?> getMashingData(@RequestBody PiRaStep piRaStep) {

		return Result.OK(baseService.getMashingData(piRaStep));
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
}
