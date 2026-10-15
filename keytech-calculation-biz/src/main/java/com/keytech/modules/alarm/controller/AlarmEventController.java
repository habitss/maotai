package com.keytech.modules.alarm.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.alarm.constants.AlarmStatusEnum;
import com.keytech.modules.alarm.constants.TsmStatusEnum;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.service.IAlarmPointService;
import org.apache.logging.log4j.util.Strings;
import org.apache.shiro.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.service.IAlarmEventService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报警事实
 * @author liu
 * @since 2026.01.12
 * */
@Tag(name="报警事实")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/alarm/alarmEvent")
public class AlarmEventController extends BaseController<IAlarmEventService, AlarmEvent> {
	private final IAlarmPointService alarmPointService;

	/**
	 * 报警事实分页查询
	 * @author Rocye
	 * @since 2026.02.26
	 */
	@Operation(summary = "报警事实分页查询", description = "报警事实分页查询")
	@AutoDict
	@GetMapping({"/list/page"})
	public Result<IPage<AlarmEvent>> getPageList(AlarmEvent entity, HttpServletRequest req,
												 @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
												 @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
												 @RequestParam(name = "orderField",required = false) String orderField,
												 @RequestParam(name = "orderType",required = false) String orderType,
												 @RequestParam(name = "selectFields",required = false) String selectFields) {
		QueryWrapper<AlarmEvent> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
		//根据当前登录用户部门展示数据
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String orgCode = loginUser.getOrgCode();
		if (StringUtils.isNotEmpty(orgCode)) {
			queryWrapper.likeRight("factory_code", orgCode);
		}
		if (Strings.isNotEmpty(selectFields)) {
			List<String> selectFieldList = Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList();
			queryWrapper.select(selectFieldList);
		}
		if (Strings.isNotEmpty(orderType) && Strings.isNotEmpty(orderField)) {
			if ("desc".equals(orderType)) {
				queryWrapper.orderByDesc(StrUtil.toUnderlineCase(orderField));
			}
			if ("asc".equals(orderType)) {
				queryWrapper.orderByAsc(StrUtil.toUnderlineCase(orderField));
			}
		}
		queryWrapper.eq("tenant_id", req.getIntHeader("X-Tenant-Id"));
		Page<AlarmEvent> page = new Page<>(pageNo, pageSize);
		return Result.OK(this.baseService.page(page, queryWrapper));
	}

	/**
	 * 添加
	 */
	@AutoLog(value = "报警事实-添加")
	@Operation(summary="报警事实-添加")
	@RequiresPermissions("alarm:alarm_event:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AlarmEvent alarmEvent) {
		baseService.save(alarmEvent);
		return Result.OK("添加成功！");
	}
	
	/**
	 * 编辑
	 */
	@AutoLog(value = "报警事实-编辑")
	@Operation(summary="报警事实-编辑")
	@RequiresPermissions("alarm:alarm_event:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AlarmEvent alarmEvent) {
		baseService.updateById(alarmEvent);
		return Result.OK("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 */
	@AutoLog(value = "报警事实-通过id删除")
	@Operation(summary="报警事实-通过id删除")
	@RequiresPermissions("alarm:alarm_event:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		baseService.removeById(id);
		return Result.OK("删除成功!");
	}
	
    /**
     * 导出excel
     */
    @RequiresPermissions("alarm:alarm_event:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AlarmEvent alarmEvent) {
        return super.exportXls(request, alarmEvent, AlarmEvent.class, "报警事实");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("alarm:alarm_event:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AlarmEvent.class);
    }

	/**
	* 报警中心大屏-同比环比
	*/
	@GetMapping(value = "/bigScreenComparison")
	public Result<?> bigScreenComparison(@RequestParam(required = false) String factoryCode) {
		if (StringUtils.isEmpty(factoryCode)) {
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			factoryCode = loginUser.getOrgCode();
		}
		return Result.OK(baseService.bigScreenComparison(factoryCode));
	}

	 /**
	  * 报警中心大屏-报警类型
	  */
	 @GetMapping(value = "/bigScreenAlarmType")
	 public Result<?> bigScreenAlarmType(@RequestParam(required = false) String factoryCode) {
		 if (StringUtils.isEmpty(factoryCode)) {
			 LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			 factoryCode = loginUser.getOrgCode();
		 }
		 return Result.OK(baseService.bigScreenAlarmType(factoryCode));
	 }

	 /**
	  * 报警中心大屏-设备异常排名TOP5
	  */
	 @GetMapping(value = "/bigScreenDeviceTopFive")
	 public Result<?> bigScreenDeviceTopFive(@RequestParam(required = false) String factoryCode) {
		 if (StringUtils.isEmpty(factoryCode)) {
			 LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			 factoryCode = loginUser.getOrgCode();
		 }
		 return Result.OK(baseService.bigScreenDeviceTopFive(factoryCode));
	 }

	 /**
	  * 报警中心大屏-报警分析
	  */
	 @GetMapping(value = "/bigScreenStatus")
	 public Result<?> bigScreenStatus(@RequestParam(required = false) String factoryCode) {
		 if (StringUtils.isEmpty(factoryCode)) {
			 LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			 factoryCode = loginUser.getOrgCode();
		 }
		 return Result.OK(baseService.bigScreenStatus(factoryCode));
	 }

	 /**
	  * 报警中心大屏-报警信息列表
	  */
	 @AutoDict
	 @GetMapping(value = "/bigScreenList")
	 public Result<IPage<AlarmEvent>> bigScreenList(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
							   @RequestParam(required = false, defaultValue = "10") Integer pageSize,
							   @RequestParam(required = false) String factoryCode) {
		 if (StringUtils.isEmpty(factoryCode)) {
			 LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			 factoryCode = loginUser.getOrgCode();
		 }
		 IPage<AlarmEvent> pageInfo = new Page<>(pageNum, pageSize);
		 QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<>();
		 queryWrapper.select("id,line_code,device_code,alarm_level,alarm_point_id,on_duty_person,alarm_time,alarm_desc,status")
				 .likeRight(StringUtils.isNotEmpty(factoryCode), "dept_code", factoryCode)
				 .orderByDesc("alarm_time");
		 IPage<AlarmEvent> alarmEventPage = baseService.page(pageInfo, queryWrapper);
		 List<AlarmEvent> resultList = alarmEventPage.getRecords();
		 if(resultList != null && !resultList.isEmpty()) {
			 //添加报警种类
			 Set<Integer> pointIds = resultList.stream().map(AlarmEvent::getAlarmPointId).collect(Collectors.toSet());
			 LambdaQueryWrapper<AlarmPoint> pointQueryWrapper = new LambdaQueryWrapper<>();
			 pointQueryWrapper.select(Arrays.asList(AlarmPoint::getId, AlarmPoint::getAlarmCategory, AlarmPoint::getAlarmName)).in(AlarmPoint::getId, pointIds);
			 List<AlarmPoint> alarmPointList = alarmPointService.list(pointQueryWrapper);
			 Map<Integer, AlarmPoint> alarmPointMap = alarmPointList.stream().collect(Collectors.toMap(AlarmPoint::getId, Function.identity()));
			 for(AlarmEvent alarmEvent : resultList) {
				 AlarmPoint alarmPoint = alarmPointMap.get(alarmEvent.getAlarmPointId());
				 //添加报警种类
				 if(StringUtils.isNotBlank(alarmPoint.getAlarmCategory())) {
					alarmEvent.setAlarmCategory(alarmPoint.getAlarmCategory());
				 }
				 //添加报警点位名称
				 if(StringUtils.isNotBlank(alarmPoint.getAlarmName())) {
					 //聚合报警点50-瞬时值_BBT04区域CO2黄区报警 只显示 瞬时值_ 后面内容
					alarmEvent.setAlarmName(StringUtils.substringAfter(alarmPoint.getAlarmName(), "_"));
				 }
				 alarmEvent.setStatusName(AlarmStatusEnum.getNameByVal(alarmEvent.getStatus()));
			 }
		 }
		 return Result.OK(alarmEventPage);
	 }

	/**
	 * 报警中心大屏-待办事项
	 */
	@AutoDict
	@GetMapping(value = "/bigScreenTodo")
	public Result<List<AlarmEvent>> bigScreenTodo(@RequestParam(required = false) String factoryCode) {
		if (StringUtils.isEmpty(factoryCode)) {
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			factoryCode = loginUser.getOrgCode();
		}
		IPage<AlarmEvent> pageInfo = new Page<>(1, 10);
		QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("alarm_point_id,line_code,device_code,alarm_desc,on_duty_person,alarm_time,status")
				.eq("status", AlarmStatusEnum.RESPONDED.getVal()).eq("tsm_state", TsmStatusEnum.WAITING_AUDIT.getVal())
				.likeRight(StringUtils.isNotEmpty(factoryCode), "dept_code", factoryCode)
				.orderByDesc("alarm_time");
		List<AlarmEvent> resultList = baseService.page(pageInfo, queryWrapper).getRecords();
		if(resultList != null && !resultList.isEmpty()) {
			//添加报警种类
			Set<Integer> pointIds = resultList.stream().map(AlarmEvent::getAlarmPointId).collect(Collectors.toSet());
			LambdaQueryWrapper<AlarmPoint> pointQueryWrapper = new LambdaQueryWrapper<>();
			pointQueryWrapper.select(Arrays.asList(AlarmPoint::getId, AlarmPoint::getAlarmCategory, AlarmPoint::getAlarmName)).in(AlarmPoint::getId, pointIds);
			List<AlarmPoint> alarmPointList = alarmPointService.list(pointQueryWrapper);
			Map<Integer, AlarmPoint> alarmPointMap = alarmPointList.stream().collect(Collectors.toMap(AlarmPoint::getId, Function.identity()));
			for(AlarmEvent alarmEvent : resultList) {
				AlarmPoint alarmPoint = alarmPointMap.get(alarmEvent.getAlarmPointId());
				//添加报警种类
				if(StringUtils.isNotBlank(alarmPoint.getAlarmCategory())) {
					alarmEvent.setAlarmCategory(alarmPoint.getAlarmCategory());
				}
				//添加报警点位名称
				if(StringUtils.isNotBlank(alarmPoint.getAlarmName())) {
					alarmEvent.setAlarmName(alarmPoint.getAlarmName());
				}
				alarmEvent.setStatusName(AlarmStatusEnum.getNameByVal(alarmEvent.getStatus()));
			}
		}
		return Result.OK(resultList);
	}

}
