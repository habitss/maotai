package com.keytech.modules.plan.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.plan.dto.PlanDeviceDto;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.modules.plan.service.IPlanDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description: 工单管理-机台计划
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
@Tag(name="工单管理-机台计划")
@RestController
@RequestMapping("/plan/planDevice")
@Slf4j
public class PlanDeviceController extends BaseController<IPlanDeviceService, PlanDevice> {
	@Autowired
	private IPlanDeviceService planDeviceService;
	
	/**
	 * 分页列表查询
	 *
	 * @param planDevice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="工单管理-机台计划-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PlanDevice>> queryPageList(PlanDevice planDevice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<PlanDevice> queryWrapper = QueryGenerator.initQueryWrapper(planDevice, req.getParameterMap());
		Page<PlanDevice> page = new Page<PlanDevice>(pageNo, pageSize);
		IPage<PlanDevice> pageList = planDeviceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param planDevice
	 * @return
	 */
	@AutoLog(value = "工单管理-机台计划-添加")
	@Operation(summary="工单管理-机台计划-添加")
	@RequiresPermissions("plan:mes3_plan_device:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PlanDevice planDevice) {
		planDeviceService.save(planDevice);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param planDevice
	 * @return
	 */
	@AutoLog(value = "工单管理-机台计划-编辑")
	@Operation(summary="工单管理-机台计划-编辑")
	@RequiresPermissions("plan:mes3_plan_device:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PlanDevice planDevice) {
		planDeviceService.updateById(planDevice);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "工单管理-机台计划-通过id删除")
	@Operation(summary="工单管理-机台计划-通过id删除")
	@RequiresPermissions("plan:mes3_plan_device:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		planDeviceService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "工单管理-机台计划-批量删除")
	@Operation(summary="工单管理-机台计划-批量删除")
	@RequiresPermissions("plan:mes3_plan_device:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.planDeviceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="工单管理-机台计划-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PlanDevice> queryById(@RequestParam(name="id",required=true) String id) {
		PlanDevice planDevice = planDeviceService.getById(id);
		if(planDevice==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(planDevice);
	}

	/**
	 * 定时任务
	 * @param paramJson 定时任务参数
	 */
	@AutoLog(value = "定时任务")
	@Operation(summary="定时任务")
	@PostMapping(value = "/planJob")
	public Result<String> planJob(@RequestBody String paramJson) {
		planDeviceService.planJob(paramJson);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 延迟关闭定时任务
	 * @param paramJson 定时任务参数
	 */
	@AutoLog(value = "延迟关闭定时任务")
	@Operation(summary="延迟关闭定时任务")
	@PostMapping(value = "/delayClosPlanJob")
	public Result<String> delayClosPlanJob(@RequestBody String paramJson) {
		planDeviceService.delayClosPlanJob(paramJson);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 自动触发任务
	 * @param paramJson 自动触发任务
	 */
	@AutoLog(value = "自动触发任务")
	@Operation(summary="自动触发任务")
	@PostMapping(value = "/autoTriggerPlanJob")
	public Result<String> autoTriggerPlanJob(@RequestBody String paramJson) {
//		Date ed = DateUtil.parse("2026-05-27 00:00:00");
//		Date ff = DateUtil.parse("2026-05-28 00:00:00");
//		int i=0;
//		int a = 10;
//		while (DateUtil.offsetMinute(ed, i*a).before(ff)){
//			Date currentDate = DateUtil.offsetMinute(ed, i*a);
//			//获取当前时间
//			LocalDateTime now = currentDate.toInstant()
//					.atZone(ZoneId.systemDefault())
//					.toLocalDateTime();;
//			//将秒和毫秒变为0
//			LocalDateTime currentLocalDateTime = now.truncatedTo(ChronoUnit.MINUTES);
//			planDeviceService.autoTriggerPlanJob(paramJson, currentDate, currentLocalDateTime);
//			i++;
//		}
		//获取当前时间
		LocalDateTime now = LocalDateTime.now();
		//将秒和毫秒变为0
		LocalDateTime currentLocalDateTime = now.truncatedTo(ChronoUnit.MINUTES);
		Date currentDate = Date.from(currentLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
		planDeviceService.autoTriggerPlanJob(paramJson, currentDate, currentLocalDateTime);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 手动触发任务
	 */
	@AutoLog(value = "手动触发任务")
	@Operation(summary="手动触发任务")
	@PostMapping(value = "/sendManualTrigger")
	public Result<String> sendManualTrigger(@RequestBody List<MdmTask> taskList) {
		planDeviceService.sendManualTrigger(taskList, new Date());
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	 /**
	  * 关闭机台计划
	  * @return
	  */
	 @AutoLog(value = "关闭机台计划")
	 @Operation(summary="关闭机台计划")
	 @PostMapping(value = "/endPlanDevice")
	 public Result<String> endPlanDevice(@RequestBody PlanDeviceDto planDeviceDto) {
		 //当前时间
		 Date now = new Date();
		 //下一个整点时间
		 Date nextHour = DateUtil.beginOfHour(DateUtil.offsetHour(now, 1));
		 //实际结束时间
		 Date realEndTime = null == planDeviceDto.getRealEndTime() ? new Date() : planDeviceDto.getRealEndTime();
		 //将秒变为0，避免计算错误
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(realEndTime);
		 cal.set(Calendar.SECOND, 0);
		 cal.set(Calendar.MILLISECOND, 0);
		 Date calcEndTime = cal.getTime();
		 //计算结束时间和当前时间相差多少分钟
		 long minutes = DateUtil.between(now, calcEndTime, DateUnit.MINUTE);
		 //非开班计划或者大于120分钟直接结束计划
		 if(!"1".equals(planDeviceDto.getPlanType()) || minutes > 120){
			 planDeviceService.endPlanDevice(planDeviceDto.getPlanId(), planDeviceDto.getDeviceIds(), realEndTime, false);
		 } else {
			 //计算实际结束时间于下一个整数小时间相差多少分钟
			 minutes = DateUtil.between(nextHour, calcEndTime, DateUnit.MINUTE);
			 //小于等于15分钟，将整点时间延迟为后两个小时
			 if(minutes <= 15){
				 nextHour = DateUtil.beginOfHour(DateUtil.offsetHour(now, 2));
			 }
			 planDeviceService.delayEndPlanDevice(planDeviceDto.getPlanId(), planDeviceDto.getDeviceIds(), realEndTime, nextHour);
		 }
		 return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param planDevice
    */
    @RequiresPermissions("plan:mes3_plan_device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PlanDevice planDevice) {
        return super.exportXls(request, planDevice, PlanDevice.class, "工单管理-机台计划");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("plan:mes3_plan_device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PlanDevice.class);
    }
	@GetMapping(value = "/planOrderTest")
	public Result<?> planOrderTest(
			@RequestParam(name="pageNo") String id,
			@RequestParam(name="pageSize") String startTime) {
		return Result.OK(this.planDeviceService.planOrderTest(id, startTime));
	}
}
