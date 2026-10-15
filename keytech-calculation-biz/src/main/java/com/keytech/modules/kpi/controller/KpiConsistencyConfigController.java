package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiConsistencyConfig;
import com.keytech.modules.kpi.service.IKpiConsistencyConfigService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 一致KPI数据配置
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
@Tag(name="一致KPI数据配置")
@RestController
@RequestMapping("/kpi/kpiConsistencyConfig")
@Slf4j
public class KpiConsistencyConfigController extends BaseController<IKpiConsistencyConfigService, KpiConsistencyConfig> {
	@Autowired
	private IKpiConsistencyConfigService kpiConsistencyConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param kpiConsistencyConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="一致KPI数据配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<KpiConsistencyConfig>> queryPageList(KpiConsistencyConfig kpiConsistencyConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<KpiConsistencyConfig> queryWrapper = QueryGenerator.initQueryWrapper(kpiConsistencyConfig, req.getParameterMap());
		Page<KpiConsistencyConfig> page = new Page<KpiConsistencyConfig>(pageNo, pageSize);
		IPage<KpiConsistencyConfig> pageList = kpiConsistencyConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	
	/**
	 *  编辑
	 *
	 * @param kpiConsistencyConfig
	 * @return
	 */
	@AutoLog(value = "一致KPI数据配置-编辑")
	@Operation(summary="一致KPI数据配置-编辑")
	//@RequiresPermissions("kpi:mes3_kpi_consistency_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody KpiConsistencyConfig kpiConsistencyConfig) {
		kpiConsistencyConfigService.updateById(kpiConsistencyConfig);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "一致KPI数据配置-通过id删除")
	@Operation(summary="一致KPI数据配置-通过id删除")
	@RequiresPermissions("kpi:mes3_kpi_consistency_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		kpiConsistencyConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "一致KPI数据配置-批量删除")
	@Operation(summary="一致KPI数据配置-批量删除")
	@RequiresPermissions("kpi:mes3_kpi_consistency_config:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.kpiConsistencyConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="一致KPI数据配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<KpiConsistencyConfig> queryById(@RequestParam(name="id",required=true) String id) {
		KpiConsistencyConfig kpiConsistencyConfig = kpiConsistencyConfigService.getById(id);
		if(kpiConsistencyConfig==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(kpiConsistencyConfig);
	}

	/**
	 * 定时任务接口-一致KPI数据计算定时任务
	 */
	@AutoLog(value = "定时任务接口-一致KPI数据计算定时任务")
	@Operation(summary = "定时任务接口-一致KPI数据计算定时任务", description = "定时任务接口-一致KPI数据计算定时任务")
	@PostMapping(value = "/extractData")
	public Result<String> extractData(@RequestBody Map map) {
		log.info("定时任务接口-一致KPI数据计算定时任务开始执行---ok");
		kpiConsistencyConfigService.extractData(map);
		log.info("定时任务接口-一致KPI数据计算定时任务执行完成---ok,");
		return Result.OK("调用成功！");
	}


}
