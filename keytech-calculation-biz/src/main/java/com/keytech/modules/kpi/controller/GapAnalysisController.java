package com.keytech.modules.kpi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.kpi.dto.KpiDiffAnalysisDto;
import com.keytech.modules.kpi.entity.GapAnalysis;
import com.keytech.modules.kpi.service.IGapAnalysisService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 差距分析
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="差距分析")
@RestController
@RequestMapping("/kpi/gapAnalysis")
@Slf4j
public class GapAnalysisController extends BaseController<IGapAnalysisService, GapAnalysis> {
	@Autowired
	private IGapAnalysisService gapAnalysisService;

	@Autowired
	private IFiveWhyDetailService fiveWhyDetailService;
	/**
	 * 分页列表查询
	 *
	 * @param gapAnalysis
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="差距分析-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<GapAnalysis>> queryPageList(GapAnalysis gapAnalysis,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<GapAnalysis> queryWrapper = QueryGenerator.initQueryWrapper(gapAnalysis, req.getParameterMap());
		Page<GapAnalysis> page = new Page<GapAnalysis>(pageNo, pageSize);
		IPage<GapAnalysis> pageList = gapAnalysisService.page(page, queryWrapper);
		return Result.OK(pageList);
	}



	 /**
	  * 分页列表查询
	  *
	  * @param gapAnalysis
	  * @param req
	  * @return
	  */
	 @Operation(summary="差距分析-分页列表查询")
	 @GetMapping(value = "/queryData")
	 public Result<GapAnalysis> queryData(GapAnalysis gapAnalysis,
													 HttpServletRequest req) {
		 QueryWrapper<GapAnalysis> queryWrapper = QueryGenerator.initQueryWrapper(gapAnalysis, req.getParameterMap());
		 List<GapAnalysis> list = gapAnalysisService.list(queryWrapper);
		 GapAnalysis result = new GapAnalysis();
		 if(list.size() > 0) {
			 result = list.get(0);
			 LambdaQueryWrapper<FiveWhyDetail> lambdaQueryWrapper = new LambdaQueryWrapper<FiveWhyDetail>()
					 .eq(FiveWhyDetail::getRecordId, result.getId());
			 result.setDetailList(fiveWhyDetailService.list(lambdaQueryWrapper));
		 }
		 return Result.OK(result);
	 }
	
	/**
	 *   添加
	 *
	 * @param gapAnalysis
	 * @return
	 */
	@AutoLog(value = "差距分析-添加")
	@Operation(summary="差距分析-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody GapAnalysis gapAnalysis) {
		gapAnalysisService.add(gapAnalysis);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "差距分析-通过id删除")
	@Operation(summary="差距分析-通过id删除")
	@RequiresPermissions("kpi:mes3_gap_analysis:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		gapAnalysisService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "差距分析-批量删除")
	@Operation(summary="差距分析-批量删除")
	@RequiresPermissions("kpi:mes3_gap_analysis:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.gapAnalysisService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="差距分析-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<GapAnalysis> queryById(@RequestParam(name="id",required=true) String id) {
		GapAnalysis gapAnalysis = gapAnalysisService.getById(id);
		if(gapAnalysis==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(gapAnalysis);
	}


	/**
	 * 差异分析 当月差距值
	 *
	 * @param dto
	 * @return
	 */
	@PostMapping(value = "/monthGapValue")
	@AutoLog(value = "差异分析-当月差距")
	public Result<?> monthGapValue(@RequestBody KpiDiffAnalysisDto dto) {
		return Result.OK(gapAnalysisService.monthGapValue(dto));
	}

	/**
	 * 差异分析 月度跟踪
	 *
	 * @param dto
	 * @return
	 */
	@PostMapping(value = "/monthTracking")
	@AutoLog(value = "差异分析-月度跟踪")
	public Result<?> monthTracking(@RequestBody KpiDiffAnalysisDto dto) {
		return Result.OK(gapAnalysisService.monthTracking(dto));
	}
}
