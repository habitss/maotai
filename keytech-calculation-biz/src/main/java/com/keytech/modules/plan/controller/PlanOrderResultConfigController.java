package com.keytech.modules.plan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.plan.entity.PlanOrderResultConfig;
import com.keytech.modules.plan.service.IPlanOrderResultConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 工单记录配置表，用于存储工单生成时候的任务条目配置
 * @Author: jeecg-boot
 * @Date:   2025-06-19
 * @Version: V1.0
 */
@Tag(name="工单记录配置表，用于存储工单生成时候的任务条目配置")
@RestController
@RequestMapping("/plan/planOrderResultConfig")
@Slf4j
public class PlanOrderResultConfigController extends BaseController<IPlanOrderResultConfigService, PlanOrderResultConfig> {
	@Autowired
	private IPlanOrderResultConfigService planOrderResultConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param planOrderResultConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="工单记录配置表，用于存储工单生成时候的任务条目配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PlanOrderResultConfig>> queryPageList(PlanOrderResultConfig planOrderResultConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PlanOrderResultConfig> queryWrapper = QueryGenerator.initQueryWrapper(planOrderResultConfig, req.getParameterMap());
		Page<PlanOrderResultConfig> page = new Page<PlanOrderResultConfig>(pageNo, pageSize);
		IPage<PlanOrderResultConfig> pageList = planOrderResultConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}



	/**
	 * 通过resultConfigId查询
	 * @param resultConfigId
	 * @return
	 */
	@Operation(summary="工单历史配置-通过resultConfigId查询")
	@GetMapping(value = "/queryByResultConfigId")
	public Result<List<PlanOrderResultConfig>> queryByResultConfigId(@RequestParam(name="resultConfigId") String resultConfigId) {
		return Result.OK(planOrderResultConfigService.lambdaQuery().eq(PlanOrderResultConfig::getResultConfigId, resultConfigId).list());
	}

}
