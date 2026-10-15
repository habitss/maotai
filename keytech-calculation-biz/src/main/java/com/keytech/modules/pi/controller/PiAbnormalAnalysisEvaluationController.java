package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.entity.PiAbnormalAnalysisEvaluation;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisEvaluationService;
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

 /**
 * @Description: 异常分析评价
 * @Author: jeecg-boot
 * @Date:   2025-03-03
 * @Version: V1.0
 */
@Tag(name="异常分析评价")
@RestController
@RequestMapping("/pi/piAbnormalAnalysisEvaluation")
@Slf4j
public class PiAbnormalAnalysisEvaluationController extends BaseController<IPiAbnormalAnalysisEvaluationService, PiAbnormalAnalysisEvaluation> {
	@Autowired
	private IPiAbnormalAnalysisEvaluationService piAbnormalAnalysisEvaluationService;
	
	/**
	 * 分页列表查询
	 *
	 * @param piAbnormalAnalysisEvaluation
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Override
	@Operation(summary="异常分析评价-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PiAbnormalAnalysisEvaluation>> queryPageList(PiAbnormalAnalysisEvaluation piAbnormalAnalysisEvaluation,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PiAbnormalAnalysisEvaluation> queryWrapper = QueryGenerator.initQueryWrapper(piAbnormalAnalysisEvaluation, req.getParameterMap());
		Page<PiAbnormalAnalysisEvaluation> page = new Page<PiAbnormalAnalysisEvaluation>(pageNo, pageSize);
		IPage<PiAbnormalAnalysisEvaluation> pageList = piAbnormalAnalysisEvaluationService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param piAbnormalAnalysisEvaluation
	 * @return
	 */
	@AutoLog(value = "异常分析评价-添加")
	@Operation(summary="异常分析评价-添加")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PiAbnormalAnalysisEvaluation piAbnormalAnalysisEvaluation) {
		piAbnormalAnalysisEvaluationService.save(piAbnormalAnalysisEvaluation);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param piAbnormalAnalysisEvaluation
	 * @return
	 */
	@AutoLog(value = "异常分析评价-编辑")
	@Operation(summary="异常分析评价-编辑")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_evaluation:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PiAbnormalAnalysisEvaluation piAbnormalAnalysisEvaluation) {
		piAbnormalAnalysisEvaluationService.updateById(piAbnormalAnalysisEvaluation);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "异常分析评价-通过id删除")
	@Operation(summary="异常分析评价-通过id删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_evaluation:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		piAbnormalAnalysisEvaluationService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "异常分析评价-批量删除")
	@Operation(summary="异常分析评价-批量删除")
	@RequiresPermissions("pi:mes3_pi_abnormal_analysis_evaluation:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.piAbnormalAnalysisEvaluationService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="异常分析评价-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PiAbnormalAnalysisEvaluation> queryById(@RequestParam(name="id",required=true) String id) {
		PiAbnormalAnalysisEvaluation piAbnormalAnalysisEvaluation = piAbnormalAnalysisEvaluationService.getById(id);
		if(piAbnormalAnalysisEvaluation==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(piAbnormalAnalysisEvaluation);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param piAbnormalAnalysisEvaluation
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_evaluation:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PiAbnormalAnalysisEvaluation piAbnormalAnalysisEvaluation) {
        return super.exportXls(request, piAbnormalAnalysisEvaluation, PiAbnormalAnalysisEvaluation.class, "异常分析评价");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("pi:mes3_pi_abnormal_analysis_evaluation:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PiAbnormalAnalysisEvaluation.class);
    }

}
