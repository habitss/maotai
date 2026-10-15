package com.keytech.modules.period.controller;

import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.cop.dto.CopSchedulerVo;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.dto.PeriodScheduleVO;
import com.keytech.modules.period.dto.PeriodStatisticsDto;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import com.keytech.modules.serviceAgreement.dto.ServiceAgreementExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 期间核查排期
 * @Author: qipengbo
 * @Date:   2025-03-11
 * @Version: V1.0
 */
@Tag(name="期间核查排期")
@RestController
@RequestMapping("/period/periodVerificationSchedule")
@Slf4j
public class PeriodVerificationScheduleController extends BaseController<IPeriodVerificationScheduleService, PeriodVerificationSchedule> {

	@Autowired
	private IPeriodVerificationScheduleService periodVerificationScheduleService;

	@Autowired
	private IPeriodVerificationSubmitService periodVerificationSubmitService;

	@Resource
	private JeecgBaseConfig jeecgBaseConfig;

	/**
	 * 分页列表查询
	 *
	 * @param periodScheduleVO
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "期间核查排期-分页列表查询")
	@Operation(summary="期间核查排期-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PeriodVerificationSchedule>> queryPageList(PeriodVerificationSchedule periodScheduleVO,
																   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
																   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, @RequestParam(name = "orderField", required = false) String orderField,
																   @RequestParam(name = "orderType", required = false) String orderType,
																   @RequestParam(name = "selectFields", required = false) String selectFields,
																   HttpServletRequest req) {
		List<String> monthList;
		if(null != periodScheduleVO && StringUtils.isNotEmpty(periodScheduleVO.getMonths())) {
			monthList = new ArrayList<>();
			if(periodScheduleVO.getMonths().contains(",")) {
				//测试发现服务器上使用Arrays.asList会出现数组越界错误，本地无法复现，暂时使用String[]进行分割
//				monthList = Arrays.asList(ArrayUtils.toStringArray(periodScheduleVO.getMonths().split(",")));
				String[] str = periodScheduleVO.getMonths().split(",");
				for(String s : str) {
					monthList.add(s);
				}
			} else {

				monthList.add(periodScheduleVO.getMonths());
			}
		} else {
			monthList = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12");
		}
		IPage<PeriodVerificationSchedule> pageList = periodVerificationScheduleService.findPage(periodScheduleVO,monthList, pageNo,pageSize);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param periodScheduleVO
	 * @return
	 */
	@AutoLog(value = "期间核查排期-添加")
	@Operation(summary="期间核查排期-添加")
	@PostMapping(value = "/saveOrUpdate")
	public Result<PeriodVerificationSchedule> saveOrUpdate(@RequestBody PeriodScheduleVO periodScheduleVO) {
		PeriodVerificationSchedule schedule = periodVerificationScheduleService.handlePeriodSchedule(periodScheduleVO);
		return Result.OK(schedule);
	}

	/**
	 *   取消所有排期
	 *
	 * @param periodScheduleVO
	 * @return
	 */
	@AutoLog(value = "期间核查排期-取消排期")
	@Operation(summary="期间核查排期-取消排期")
	@PostMapping(value = "/cancel")
	public Result<PeriodVerificationSchedule> cancel(@RequestBody PeriodScheduleVO periodScheduleVO) {
		PeriodVerificationSchedule schedule = periodVerificationScheduleService.handleCancelPeriodSchedule(periodScheduleVO);
		return Result.OK(schedule);
	}

	@AutoLog(value = "期间核查排期-排期配置更新")
	@Operation(summary = "期间核查排期-排期配置更新", description = "COP计划排期-排期配置更新")
	@PostMapping(value = "/updatePeriod")
	public Result<String> updatePeriod(@RequestBody List<PeriodScheduleVO> periodScheduleVO) {
		try {
			periodVerificationScheduleService.updatePeriod(periodScheduleVO);
		}catch (Exception e){
			return Result.error("保存失败，请检查该设备清洗点是否已有此步骤");
		}
		return Result.OK("保存成功");
	}

	/**
	 *  修改排期状态
	 *
	 * @param scheduleSubmit
	 * @return
	 */
	@AutoLog(value = "期间核查排期-修改排期状态")
	@Operation(summary="期间核查排期-修改排期状态")
	@RequestMapping(value = "/process", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> processEdit(@RequestBody PeriodScheduleSubmitVO scheduleSubmit) {
		periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(),scheduleSubmit.getPlanMonth(), String.valueOf(scheduleSubmit.getVerifyResult()));
		if(StringUtils.isNotEmpty(scheduleSubmit.getId())){
			PeriodVerificationSubmit submit =periodVerificationSubmitService.getById(scheduleSubmit.getId());
			submit.setCheckResult(0);
			submit.setVerifyResult(0);
			periodVerificationSubmitService.updateById(submit);
		}
		return Result.OK("编辑成功!");
	}

	/**
	 *  排期核查统计
	 *
	 * @param periodStatisticsDto
	 * @return
	 */
	@AutoLog(value = "期间核查排期-排期核查统计")
	@Operation(summary="期间核查排期-排期核查统计")
	@RequestMapping(value = "/periodStatistics", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<List<PeriodStatisticsDto>> periodStatistics(@RequestBody PeriodStatisticsDto periodStatisticsDto) {
		List<PeriodStatisticsDto> resultList = periodVerificationScheduleService.periodStatistics(periodStatisticsDto);
		return Result.OK(resultList);
	}

	@AutoLog(value = "期间核查排期-完成率排名")
	@Operation(summary = "期间核查排期-完成率排名", description = "期间核查排期-完成率排名")
	@PostMapping("/periodPlanRank")
	public Result<List<CopPlanRank>> periodPlanRank(@RequestBody WSActionStatisticsDto copStatisticsDto){
		List<CopPlanRank> list;
		try {
			list = periodVerificationScheduleService.periodPlanRank(copStatisticsDto);
		}catch (Exception e){
			log.error("统计表报错",e);
			return Result.error("查询出错");
		}
		return Result.OK(list);
	}

	@AutoLog(value = "期间核查排期-车间首页统计表")
	@Operation(summary = "期间核查排期-车间首页统计表", description = "期间核查排期-车间首页统计表")
	@PostMapping("/periodLineStatistics")
	public Result<List<CopPlanRank>> periodLineStatistics(@RequestBody WSActionStatisticsDto copStatisticsDto){
		List<CopPlanRank> list;
		try {
			list = periodVerificationScheduleService.periodLineStatistics(copStatisticsDto);
		}catch (Exception e){
			log.error("统计表报错",e);
			return Result.error("查询出错");
		}
		return Result.OK(list);
	}
	
	/**
	 *  编辑
	 *
	 * @param periodVerificationSchedule
	 * @return
	 */
	@AutoLog(value = "期间核查排期-编辑")
	@Operation(summary="期间核查排期-编辑")
	@RequiresPermissions("period:mes3_period_verification_schedule:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PeriodVerificationSchedule periodVerificationSchedule) {
		periodVerificationScheduleService.updateById(periodVerificationSchedule);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "期间核查排期-通过id删除")
	@Operation(summary="期间核查排期-通过id删除")
	@RequiresPermissions("period:mes3_period_verification_schedule:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		periodVerificationScheduleService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "期间核查排期-批量删除")
	@Operation(summary="期间核查排期-批量删除")
	@RequiresPermissions("period:mes3_period_verification_schedule:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.periodVerificationScheduleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="期间核查排期-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PeriodVerificationSchedule> queryById(@RequestParam(name="id") String id) {
		PeriodVerificationSchedule periodVerificationSchedule = periodVerificationScheduleService.getById(id);
		if(periodVerificationSchedule==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(periodVerificationSchedule);
	}

    /**
    * 导出excel
    *
    * @param periodStatisticsDto
    */
    //@RequiresPermissions("period:mes3_period_verification_schedule:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(PeriodStatisticsDto periodStatisticsDto) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<PeriodStatisticsDto> resultList = periodVerificationScheduleService.periodStatistics(periodStatisticsDto);
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject("fileName", "期间核查统计表");
		mv.addObject("entity", PeriodStatisticsDto.class);
		ExportParams exportParams = new ExportParams("期间核查统计" + "报表", "导出人:" + sysUser.getRealname(), "期间核查统计表");
		exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
		mv.addObject("params", exportParams);
		mv.addObject("data", resultList);
		return mv;
	}

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("period:mes3_period_verification_schedule:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PeriodVerificationSchedule.class);
    }

}
