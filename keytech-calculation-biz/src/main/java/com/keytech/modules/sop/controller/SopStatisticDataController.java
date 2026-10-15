package com.keytech.modules.sop.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.sop.entity.SopDiagnoseData;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.service.ISopDiagnoseDataService;
import com.keytech.modules.sop.service.ISopStatisticDataService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: SOP-诊断计划
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Tag(name="SOP-诊断计划")
@RestController
@RequestMapping("/sop/sopStatisticData")
@Slf4j
public class SopStatisticDataController extends BaseController<ISopStatisticDataService, SopStatisticData> {
	@Autowired
	private ISopStatisticDataService sopStatisticDataService;
	@Autowired
	private ISopDiagnoseDataService sopDiagnoseDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param sopStatisticData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="SOP-诊断计划-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<SopStatisticData>> queryPageList(SopStatisticData sopStatisticData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		Page<SopStatisticData> page = new Page<SopStatisticData>(pageNo, pageSize);
		IPage<SopStatisticData> pageList = sopStatisticDataService.getData(page, sopStatisticData,req);
		return Result.OK(pageList);
	}

	/**
	 *  保存
	 *
	 * @param input
	 * @return
	 */
	@AutoLog(value = "SOP-诊断计划-保存")
	@Operation(summary="SOP-诊断计划-保存")
	@RequiresPermissions("sop:mes3_sop_statistic_data:add")
	@RequestMapping(value = "/sopSubmit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> sopSubmit(@RequestBody List<JSONObject> input) {
		sopStatisticDataService.sopSubmit(input);
		return Result.OK("保存成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "SOP-诊断计划-通过id删除")
	@Operation(summary="SOP-诊断计划-通过id删除")
	@RequiresPermissions("sop:mes3_sop_statistic_data:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		sopStatisticDataService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "SOP-诊断计划-批量删除")
	@Operation(summary="SOP-诊断计划-批量删除")
	@RequiresPermissions("sop:mes3_sop_statistic_data:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sopStatisticDataService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	 /**
	  * 获取周
	  * @return
	  */
	 @Operation(summary="获取周")
	 @GetMapping(value = "/getWeek")
	 public Result<?> getWeek(int year, @RequestParam(required = false) List<String> months) {
		 List<MdmWeek> mdmWeekList = sopStatisticDataService.getWeek(year, months);
		 return Result.OK(mdmWeekList);
	 }



	/**
	 *  操作性工作诊断表保存
	 *
	 * @param sopDiagnoseData
	 * @return
	 */
	@AutoLog(value = "SOP-诊断计划-诊断保存")
	@Operation(summary="SOP-诊断计划-诊断保存")
	@RequestMapping(value = "/diagnosticPostSave", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> diagnosticPostSave(@RequestBody SopDiagnoseData sopDiagnoseData) {
		return sopDiagnoseDataService.diagnosticPostSave(sopDiagnoseData);
	}

	@Operation(summary="SOP-诊断计划-诊断查询")
	@GetMapping(value = "/getDiagnosticPost")
	public Result<?> getDiagnosticPost(@RequestParam(name="sopStatisticId",required=true) String sopStatisticId,
									 @RequestParam(name="week",required=false) String week,
									 @RequestParam(name="year",required=false) String year) {

		return sopDiagnoseDataService.getDiagnosticPost(sopStatisticId, week, year);
	}



	/**
	 * 导出excel
	 *
	 * @param request
	 * @param sopStatisticData
	 */
	@RequiresPermissions("sop:mes3_sop_statistic_data:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SopStatisticData sopStatisticData) {
		return sopStatisticDataService.exportXlsAll(request, sopStatisticData, SopStatisticData.class, "SOP计划");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sop:mes3_sop_statistic_data:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return sopStatisticDataService.sopImportExcel(request, response, SopStatisticData.class);
	}

}
