package com.keytech.modules.period.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * @Description: 期间排查排查内容提交
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Tag(name="期间排查排查内容提交")
@RestController
@RequestMapping("/period/periodVerificationSubmit")
@Slf4j
public class PeriodVerificationSubmitController extends BaseController<IPeriodVerificationSubmitService, PeriodVerificationSubmit> {
	@Autowired
	private IPeriodVerificationSubmitService periodVerificationSubmitService;

	@Autowired
	private IPeriodVerificationScheduleService periodVerificationScheduleService;

	@Autowired
	private IActionPlanTrackService actionPlanTrackService;

	@Autowired
	private ISysUserService sysUserService;
	
	/**
	 * 分页列表查询
	 *
	 * @param periodVerificationSubmit
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="期间排查排查内容提交-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PeriodVerificationSubmit>> queryPageList(PeriodVerificationSubmit periodVerificationSubmit,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PeriodVerificationSubmit> queryWrapper = QueryGenerator.initQueryWrapper(periodVerificationSubmit, req.getParameterMap());
		queryWrapper.orderByAsc("sort");
		Page<PeriodVerificationSubmit> page = new Page<>(pageNo, pageSize);
		IPage<PeriodVerificationSubmit> pageList = periodVerificationSubmitService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param scheduleSubmit
	 * @return
	 */
	@AutoLog(value = "期间排查排查内容提交-添加")
	@Operation(summary="期间排查排查内容提交-添加")
	//@RequiresPermissions("period:mes3_period_verification_submit:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PeriodScheduleSubmitVO scheduleSubmit) {
		PeriodVerificationSubmit periodVerificationSubmit = periodVerificationSubmitService.findSubmitByParam(scheduleSubmit);
		PeriodVerificationSchedule schedule = periodVerificationScheduleService.getById(scheduleSubmit.getPeriodScheduleId());
		if(null == periodVerificationSubmit) {
			periodVerificationSubmit = new PeriodVerificationSubmit();
		}
		BeanUtils.copyProperties(scheduleSubmit, periodVerificationSubmit);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(StringUtils.isNotEmpty(scheduleSubmit.getCheckDate())){
			try {
				periodVerificationSubmit.setCheckDate(sdf.parse(scheduleSubmit.getCheckDate()));
			} catch (Exception e) {
				log.error("日期格式错误，请检查日期格式yyyy-MM-dd HH:mm:ss");
			}
		}
		if(0 != scheduleSubmit.getVerifyResult()){
			periodVerificationSubmit.setVerifyDate(new Date());
		}
		if(null != schedule) {
			int frequency = Integer.parseInt(schedule.getFrequency());
			if (scheduleSubmit.getVerifyResult() == 1) {
				DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					// 定义目标日期格式
				if (StringUtils.isNotEmpty(scheduleSubmit.getCheckDate())) {
					// 解析原始日期时间字符串
					LocalDateTime localDateTime = LocalDateTime.parse(scheduleSubmit.getCheckDate(), originalFormatter);
					// 格式化日期时间为目标格式
					LocalDate checkDate = localDateTime.toLocalDate();

					// 获取当前日期是当年的第几周
					int weekOfYear = DateUtils.getCurrentOfWeek(checkDate);
					String state = "1";
					int planWeek = scheduleSubmit.getPlanWeek();
					//判断处理时间是否在计划时间内
					if (planWeek == weekOfYear) {
						periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getPlanWeek(), state);
					} else {
						if (frequency > 2) {
							//需要在计划时间后4周内审核
							if (weekOfYear - planWeek > 4) {
								state = "3";
							}
						} else if (frequency == 2) {
							//需要在计划时间后2周内审核
							if (weekOfYear - planWeek > 2) {
								state = "3";
							}
						} else if (frequency == 1) {
							state = "3";
						}
						periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getPlanWeek(), state);
					}
				}
			} else if (scheduleSubmit.getVerifyResult() == 2) {
				periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getPlanWeek(), "0");
//				if (StringUtils.isNotEmpty(scheduleSubmit.getId())){
//					periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getPlanWeek(), "4");
//				}
			} else {
				periodVerificationScheduleService.processPeriodSchedule(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getPlanWeek(), "4");
			}
		}
		periodVerificationSubmit.setVerifyResult(scheduleSubmit.getVerifyResult());
		periodVerificationSubmitService.saveOrUpdate(periodVerificationSubmit);
		if(CollectionUtils.isNotEmpty(scheduleSubmit.getActionPlanTrackList())) {
			actionPlanTrackService.saveOrUpdateAction(scheduleSubmit.getPeriodScheduleId(), scheduleSubmit.getActionPlanTrackList());
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 通过年月周查询
	 *
	 * @param scheduleSubmit
	 * @return
	 */
	@Operation(summary="期间排查排查内容提交-通过年月周查询")
	@PostMapping(value = "/queryByParam")
	public Result<PeriodVerificationSubmit> queryByParam(@RequestBody PeriodScheduleSubmitVO scheduleSubmit) {
		PeriodVerificationSubmit periodVerificationSubmit = periodVerificationSubmitService.findSubmitByParam(scheduleSubmit);
		return Result.OK(periodVerificationSubmit);
	}

	/**
	 * 通过年月周查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="期间排查历史记录查询")
	@GetMapping(value = "/queryList")
	public Result<List<PeriodVerificationSubmit>> queryList(@RequestParam(name="id") String id) {
		List<PeriodVerificationSubmit> periodVerificationSubmit = periodVerificationSubmitService
				.list(new LambdaQueryWrapper<PeriodVerificationSubmit>()
						.eq(PeriodVerificationSubmit::getPeriodScheduleId,id)
						.eq(PeriodVerificationSubmit::getPlanYear,DateUtils.getCurrentYear()));
		List<PeriodVerificationSubmit> result = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(periodVerificationSubmit)){
			for(PeriodVerificationSubmit submit : periodVerificationSubmit){
				if(StringUtils.isNotEmpty(submit.getCheckUser())){
					submit.setCheckUser(sysUserService.getById(submit.getCheckUser()).getRealname());
				}
				if(StringUtils.isNotEmpty(submit.getVerifyUser())){
					submit.setVerifyUser(sysUserService.getById(submit.getVerifyUser()).getRealname());
				}
				result.add(submit);
			}
		}
		return Result.OK(result);
	}
	
	/**
	 *  编辑
	 *
	 * @param periodVerificationSubmit
	 * @return
	 */
	@AutoLog(value = "期间排查排查内容提交-编辑")
	@Operation(summary="期间排查排查内容提交-编辑")
	@RequiresPermissions("period:mes3_period_verification_submit:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PeriodVerificationSubmit periodVerificationSubmit) {
		periodVerificationSubmitService.updateById(periodVerificationSubmit);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "期间排查排查内容提交-通过id删除")
	@Operation(summary="期间排查排查内容提交-通过id删除")
	//@RequiresPermissions("period:mes3_period_verification_submit:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		periodVerificationSubmitService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "期间排查排查内容提交-批量删除")
	@Operation(summary="期间排查排查内容提交-批量删除")
	@RequiresPermissions("period:mes3_period_verification_submit:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.periodVerificationSubmitService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="期间排查排查内容提交-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PeriodVerificationSubmit> queryById(@RequestParam(name="id") String id) {
		PeriodVerificationSubmit periodVerificationSubmit = periodVerificationSubmitService.getById(id);
		if(periodVerificationSubmit==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(periodVerificationSubmit);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param periodVerificationSubmit
    */
    @RequiresPermissions("period:mes3_period_verification_submit:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PeriodVerificationSubmit periodVerificationSubmit) {
        return super.exportXls(request, periodVerificationSubmit, PeriodVerificationSubmit.class, "期间排查排查内容提交");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("period:mes3_period_verification_submit:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PeriodVerificationSubmit.class);
    }

	/**
	 * 定时任务
	 * @return
	 */
	@AutoLog(value = "期间核查填报-定时任务")
	@Operation(summary="期间核查P填报-定时任务")
	@PostMapping(value = "/periodSubmitByJob")
	public Result<String> periodSubmitByJob() {
		periodVerificationSubmitService.scheduleTask();
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
}
