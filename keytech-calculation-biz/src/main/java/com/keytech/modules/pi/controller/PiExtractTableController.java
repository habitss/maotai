package com.keytech.modules.pi.controller;

import com.keytech.modules.kpi.controller.KpiThirdPlayDataController;
import com.keytech.modules.pi.entity.PiExtractTable;
import com.keytech.modules.pi.service.IPiExtractTableService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: PI-数据抽取配置主表
 * @Author: HQ
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Tag(name="PI-数据抽取配置主表")
@RestController
@RequestMapping("/pi/piExtractTable")
@Slf4j
public class PiExtractTableController extends BaseController<IPiExtractTableService, PiExtractTable> {
	private static final Logger logger = LoggerFactory.getLogger(KpiThirdPlayDataController.class);
	@Autowired
	private IPiExtractTableService piExtractTableService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piExtractTable
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="PI-数据抽取配置主表-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiExtractTable>> queryPageList(PiExtractTable piExtractTable,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {


        QueryWrapper<PiExtractTable> queryWrapper = QueryGenerator.initQueryWrapper(piExtractTable, req.getParameterMap());
		Page<PiExtractTable> page = new Page<PiExtractTable>(pageNo, pageSize);
		IPage<PiExtractTable> pageList = piExtractTableService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piExtractTable
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置主表-添加")
	@Operation(summary="PI-数据抽取配置主表-添加")
	@RequiresPermissions("pi:mes3_pi_extract_table:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiExtractTable piExtractTable) {
		piExtractTableService.add(piExtractTable);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piExtractTable
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置主表-编辑")
	@Operation(summary="PI-数据抽取配置主表-编辑")
	@RequiresPermissions("pi:mes3_pi_extract_table:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiExtractTable piExtractTable) {
		piExtractTableService.updateById(piExtractTable);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置主表-通过id删除")
	@Operation(summary="PI-数据抽取配置主表-通过id删除")
	@RequiresPermissions("pi:mes3_pi_extract_table:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piExtractTableService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "PI-数据抽取配置主表-批量删除")
	@Operation(summary="PI-数据抽取配置主表-批量删除")
	@RequiresPermissions("pi:mes3_pi_extract_table:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piExtractTableService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="PI-数据抽取配置主表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiExtractTable> queryById(@RequestParam(name="id",required=true) String id) {
		PiExtractTable piExtractTable = piExtractTableService.getById(id);
		if(piExtractTable==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piExtractTable);
	}

	 /**
	  * 获取库中表(来原表)
	  */
	 @Operation(summary="PI-数据抽取配置主表-获取库中表")
	 @GetMapping(value = "/getTable")
	 public Result<?> getTable() {
		 return Result.OK(piExtractTableService.getTable());
	 }

	/**
	 * 获取库中表(目标表)
	 */
	@Operation(summary="PI-数据抽取配置主表-获取库中目标表")
	@GetMapping(value = "/getTargetTable")
	public Result<?> getTargetTable() {
		return Result.OK(piExtractTableService.getTargetTable());
	}



	/**
	 * 定时任务接口-数据抽取
	 */
	@AutoLog(value = "定时任务接口-数据抽取")
	@Operation(summary = "定时任务接口-数据抽取", description = "定时任务接口-数据抽取")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		logger.info("数据抽取执行---ok");
		piExtractTableService.extractData();
		logger.info("数据抽取---ok,");
		return Result.OK("调用成功！");
	}




	 /**
	  * 数据抽取测试
	  */
	 @Operation(summary="PI-数据抽取配置主表-数据抽取测试")
	 @GetMapping(value = "/extractTest")
	 public Result<String> extractTest() {
		 piExtractTableService.extractTest();
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }




}
