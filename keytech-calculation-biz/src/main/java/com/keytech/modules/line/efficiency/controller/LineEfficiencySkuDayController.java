package com.keytech.modules.line.efficiency.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.line.efficiency.entity.LineEfficiencySku;
import com.keytech.modules.line.efficiency.entity.LineEfficiencySkuDay;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuDayService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 产线-SKU天记录表/拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-20
 * @Version: V1.0
 */
@Tag(name="产线-SKU天记录表/拆分")
@RestController
@RequestMapping("/line/efficiency/lineEfficiencySkuDay")
@Slf4j
public class LineEfficiencySkuDayController extends BaseController<ILineEfficiencySkuDayService, LineEfficiencySkuDay> {
	@Autowired
	private ILineEfficiencySkuDayService lineEfficiencySkuDayService;
	@Autowired
	private ILineEfficiencySkuService lineEfficiencySkuService;

	/**
	 * 分页列表查询
	 *
	 * @param lineEfficiencySkuDay
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="产线-SKU天记录表/拆分-分页列表查询")
	@GetMapping(value = "/queryPageData")
	public Result<IPage<LineEfficiencySkuDay>> queryPageList(LineEfficiencySkuDay lineEfficiencySkuDay,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<LineEfficiencySkuDay> queryWrapper = QueryGenerator.initQueryWrapper(lineEfficiencySkuDay, req.getParameterMap());
		//queryWrapper 添加产量(OutputValue)不为0和空 条件
		queryWrapper.lambda()
				//.ne(LineEfficiencySkuDay::getSkuCode, "")
				.ne(LineEfficiencySkuDay::getOutputValue, 0)
				.isNotNull(LineEfficiencySkuDay::getOutputValue);

		Page<LineEfficiencySkuDay> page = new Page<LineEfficiencySkuDay>(pageNo, pageSize);
		IPage<LineEfficiencySkuDay> pageList = lineEfficiencySkuDayService.page(page, queryWrapper);
		//skuCode处理
		List<LineEfficiencySkuDay> list = pageList.getRecords();
		//skuCode 数字 去掉小数点保留整数
		for (LineEfficiencySkuDay skuDay : list) {
			if(skuDay.getSkuCode() != null && skuDay.getSkuCode().contains(".")){
				String skuCode = skuDay.getSkuCode();
				skuDay.setSkuCode(skuCode.split("\\.")[0]);
			}
		}
		return Result.OK(pageList);
	}

	
	/**
	 *   添加
	 *
	 * @param lineEfficiencySkuDay
	 * @return
	 */
	@AutoLog(value = "产线-SKU天记录表/拆分-添加")
	@Operation(summary="产线-SKU天记录表/拆分-添加")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_day:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody LineEfficiencySkuDay lineEfficiencySkuDay) {
		lineEfficiencySkuDayService.save(lineEfficiencySkuDay);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param lineEfficiencySkuDay
	 * @return
	 */
	@AutoLog(value = "产线-SKU天记录表/拆分-编辑")
	@Operation(summary="产线-SKU天记录表/拆分-编辑")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_day:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody LineEfficiencySkuDay lineEfficiencySkuDay) {
		lineEfficiencySkuDayService.updateById(lineEfficiencySkuDay);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "产线-SKU天记录表/拆分-通过id删除")
	@Operation(summary="产线-SKU天记录表/拆分-通过id删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_day:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		lineEfficiencySkuDayService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "产线-SKU天记录表/拆分-批量删除")
	@Operation(summary="产线-SKU天记录表/拆分-批量删除")
	@RequiresPermissions("line.efficiency:mes3_line_efficiency_sku_day:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.lineEfficiencySkuDayService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="产线-SKU天记录表/拆分-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<LineEfficiencySkuDay> queryById(@RequestParam(name="id",required=true) String id) {
		LineEfficiencySkuDay lineEfficiencySkuDay = lineEfficiencySkuDayService.getById(id);
		if(lineEfficiencySkuDay==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(lineEfficiencySkuDay);
	}

	/**
	 *  拆分
	 *
	 * @param lineEfficiencySkuDayList
	 * @return
	 */
	@AutoLog(value = "OPI拆分-拆分")
	@Operation(summary="OPI拆分-拆分")
	@PostMapping(value = "/dataSplit")
	public Result<String> dataSplit(@RequestBody List<LineEfficiencySkuDay> lineEfficiencySkuDayList) {
		return lineEfficiencySkuDayService.dataSplit(lineEfficiencySkuDayList);
	}

	/**
	 * 获取产量
	 * queryDataProduction
	 */
	@AutoLog(value = "获取产量")
	@Operation(summary="获取产量")
	@PostMapping(value = "/getOutputValue")
	public Result<String> getOutputValue(@RequestBody LineEfficiencySkuDay lineEfficiencySkuDay) {
		if(lineEfficiencySkuDay != null && lineEfficiencySkuDay.getStartTime() != null && lineEfficiencySkuDay.getEndTime() != null){
			Long startTime = lineEfficiencySkuDay.getStartTime().getTime();
			Long endTime = lineEfficiencySkuDay.getEndTime().getTime();
			Double outputValue = lineEfficiencySkuService.queryDataProduction(lineEfficiencySkuDay.getOutputCode(), startTime, endTime);
			return Result.OK(GlobalErrorEnum.SUCCESS.getCode(),outputValue+"");
		}
		return Result.error("参数无效");
	}

	/**
	 * 获取SKU
	 * queryDataProduction
	 */
	@AutoLog(value = "获取SKU")
	@Operation(summary="获取SKU")
	@PostMapping(value = "/getSkuList")
	public Result<List<LineEfficiencySku>> getSkuList(@RequestBody LineEfficiencySkuDay lineEfficiencySkuDay) {
		List<LineEfficiencySku> skuList = new ArrayList<>();
		if(lineEfficiencySkuDay != null){
			LambdaQueryWrapper<LineEfficiencySku> queryWrapper = new LambdaQueryWrapper();
			queryWrapper.eq(LineEfficiencySku::getLineCode, lineEfficiencySkuDay.getLineCode());
			queryWrapper.isNotNull(LineEfficiencySku::getSkuName);
			//skucode不为空且不为null
			queryWrapper.isNotNull(LineEfficiencySku::getSkuCode);
			queryWrapper.ne(LineEfficiencySku::getSkuCode, "");
			skuList = lineEfficiencySkuService.list(queryWrapper);
		}
		return Result.OK(skuList);
	}

	/**
	 *  速度编辑
	 * @param lineEfficiencySkuDay
	 * @return
	 */
	@AutoLog(value = "线效清零分段-按照sku记录-速度编辑")
	@Operation(summary="线效清零分段-按照sku记录-速度编辑")
	@RequestMapping(value = "/speedEdit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> speedEdit(@RequestBody LineEfficiencySkuDay lineEfficiencySkuDay) {
		lineEfficiencySkuDayService.updateById(lineEfficiencySkuDay);
		return Result.OK("编辑成功!");
	}
}
