package com.keytech.modules.pi.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtBatches;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.service.IPiSistarRtBatchesService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: BM-实时订单记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-23
 * @Version: V1.0
 */
@Tag(name="BM-实时订单记录表")
@RestController
@RequestMapping("/pi/adapter/piSistarRtBatches")
@Slf4j
public class PiSistarRtBatchesController extends BaseController<IPiSistarRtBatchesService, PiSistarRtBatches> {
	private static final Logger logger = LoggerFactory.getLogger(PiSistarRtBatchesController.class);
	@Autowired
	private IPiSistarRtBatchesService piSistarRtBatchesService;
	@Autowired
	private IPiSistarRtStepsService piSistarRtStepsService;

	/**
	 * 定时任务接口-Adapter-批批数据抽取
	 */
	@AutoLog(value = "定时任务接口-Adapter批批数据抽取")
	@Operation(summary = "定时任务接口-Adapter批批数据抽取", description = "定时任务接口-Adapter批批数据抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("Adapter批次数据抽取执行---ok");
		piSistarRtBatchesService.extractData();
		logger.info("Adapter批次数据抽取---ok,");
		return Result.OK("调用成功！");
	}


	/**
	 *  获取批次
	 * @param piSistarRtSteps
	 * @return
	 */
	@AutoLog(value = "获取批次")
	@Operation(summary = "获取批次", description = "获取批次")
	@PostMapping(value = "/getRtBatches")
	public Result<?> getRtBatches(@RequestBody PiSistarRtSteps piSistarRtSteps) {
		String unitNames = piSistarRtSteps.getUnitNames();
		String pcuNos = piSistarRtSteps.getPcuNos();
		String[] pcuNoArr = null;
		if(pcuNos.contains(",")){
			pcuNoArr = pcuNos.split(",");
		}
		LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
		wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEventTime());
		wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
		wrapper.eq(PiSistarRtSteps::getRecipeNo, piSistarRtSteps.getRecipeNo());
		wrapper.eq(PiSistarRtSteps::getRecipeName, piSistarRtSteps.getRecipeName());

		if(StrUtil.isNotBlank(piSistarRtSteps.getUnitId())){
			//判断是否包含,
//			if(piSistarRtSteps.getUnitId().contains(",")){
//				wrapper.in(PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitId().split(","));
//			}else {
//				wrapper.eq(PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitId());
//			}
			//判断是否包含,
			if(piSistarRtSteps.getUnitId().contains(",")){
				String [] unitIdArr = piSistarRtSteps.getUnitId().split(",");
				wrapper.and(wr -> {
					for (String unitIdStr : unitIdArr) {
						String[] parts = unitIdStr.split("-");
						wr.or(w -> w.eq(PiSistarRtSteps::getUnitNo, parts[1])
								.eq(PiSistarRtSteps::getPcuNo, parts[2]));
					}
				});
			}else {
				String[] parts = piSistarRtSteps.getUnitId().split("-");
				wrapper.eq(PiSistarRtSteps::getUnitNo, parts[1]);
				wrapper.eq(PiSistarRtSteps::getPcuNo, parts[2]);
			}
		}

		wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
		wrapper.eq(piSistarRtSteps.getReccatNo() != null,PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
		if(pcuNoArr != null && pcuNoArr.length > 0){
			wrapper.in( PiSistarRtSteps::getPcuNo, pcuNoArr);
		}else{
			wrapper.eq(PiSistarRtSteps::getPcuNo, pcuNos);
		}

		//设备-查询条件
		if(StrUtil.isNotBlank(unitNames)) {
			String[] unitName = unitNames.split(",");
			if (unitName != null && unitName.length > 0) {
				wrapper.and(w -> {
					for (String name : unitName) {
						w.or().like(PiSistarRtSteps::getUnitName, name);
					}
				});
			}
		}
		wrapper.orderByAsc(PiSistarRtSteps::getOrderNo).orderByAsc(PiSistarRtSteps::getBatchNo);
		List<PiSistarRtSteps> list = piSistarRtStepsService.list(wrapper);

		return Result.OK(deduplicate(list));
	}

	/**
	 * 根据 orderNo 和 batchNo 分组，然后在每个组内取 startTime 的最小值和 endTime 的最大值
	 * @param list
	 * @return
	 */
	public List<PiSistarRtSteps> deduplicate(List<PiSistarRtSteps> list) {
		return list.stream()
				.collect(Collectors.groupingBy(
						item -> item.getOrderNo() + "_" + item.getBatchNo(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								group -> {
									// 按 startTime 排序
									group.sort(Comparator.comparing(PiSistarRtSteps::getStartTime));

									PiSistarRtSteps first = group.get(0);
									PiSistarRtSteps last = group.get(group.size() - 1);

									PiSistarRtSteps result = new PiSistarRtSteps();
									result.setOrderNo(first.getOrderNo());
									result.setBatchNo(first.getBatchNo());
									result.setStartTime(first.getStartTime());
									result.setEndTime(last.getEndTime());
									return result;
								}
						)
				))
				.values()
				.stream()
				//按 orderNo、batchNo 排序
				.sorted(
						Comparator.comparing(
								(PiSistarRtSteps s) -> Integer.parseInt(s.getOrderNo())
						).thenComparing(
								s -> Integer.parseInt(s.getBatchNo())
						)
				)
				.collect(Collectors.toList());
	}
}
