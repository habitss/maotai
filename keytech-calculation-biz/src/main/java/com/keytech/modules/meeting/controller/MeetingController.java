package com.keytech.modules.meeting.controller;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.exception.dto.ExceptionReportVO;
import com.keytech.modules.exception.dto.ExceptionTotalVO;
import com.keytech.modules.meeting.dto.*;
import com.keytech.modules.meeting.entity.*;
import com.keytech.modules.meeting.service.*;
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
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 会议信息
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="会议信息")
@RestController
@RequestMapping("/meeting/meeting")
@Slf4j
public class MeetingController extends BaseController<IMeetingService, Meeting> {
	@Autowired
	private IMeetingService meetingService;

	@Autowired
	private IMeetingInfoService meetingInfoService;

	@Autowired
	 private IMeetingTemplateService meetingTemplateService;

	@Autowired
	private ISysDepartService sysDepartService;

	@Autowired
	private IMeetingScheduleService meetingScheduleService;

	@Autowired
	private IMeetingConfigService meetingConfigService;

	@Autowired
	private IMeetingCheckService meetingCheckService;


	/**
	 * 分页列表查询
	 *
	 * @param meeting
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议信息-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<Meeting>> queryPageList(Meeting meeting,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		LambdaQueryWrapper<Meeting> queryWrapper =new LambdaQueryWrapper<>();
		String[] years = req.getParameterMap().get("year");
		String[] months = req.getParameterMap().get("month");
		String[] currentDates = req.getParameterMap().get("currentDate");
		String year = null;
		String month = null;
		String currentDate = null;
		if(years!= null && years.length > 0){
			year = years[0];
		}
		if(months!= null && months.length > 0){
			month = months[0];
		}
		if(StringUtils.isNotBlank(year) && StringUtils.isNotBlank(month)){
			try {
				YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
				LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
				queryWrapper.between(Meeting::getMeetingDate,sdf.parse(year+"-"+month+"-01"),lastDayOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if(currentDates!= null && currentDates.length > 0){
			currentDate = currentDates[0];
			meeting.setCurrentDate(currentDate);
			try {
				queryWrapper.eq(Meeting::getMeetingDate,sdf.parse(currentDate));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if(StringUtils.isNotEmpty(meeting.getFactoryCode()) && StringUtils.isNotEmpty(meeting.getDeptCode())){
			if(meeting.getFactoryCode().equals(meeting.getDeptCode())){
				meeting.setDeptCode(null);
				queryWrapper.eq(StringUtils.isNotEmpty(meeting.getFactoryCode()),Meeting::getFactoryCode,meeting.getFactoryCode());
			}
		}
		if(StringUtils.isNotEmpty(meeting.getDeptCode())){
			queryWrapper.nested(wrapper -> {
						wrapper.eq(StringUtils.isNotEmpty(meeting.getDeptCode()), Meeting::getDeptCode, meeting.getDeptCode());
						wrapper.or().eq(StringUtils.isNotEmpty(meeting.getDeptCode()), Meeting::getWorkshop, meeting.getDeptCode());
						wrapper.or().eq(StringUtils.isNotEmpty(meeting.getDeptCode()), Meeting::getLineCode, meeting.getDeptCode());
			});
		}
		Page<Meeting> page = new Page<>(pageNo, pageSize);
		IPage<Meeting> pageList = meetingService.pageList(page, queryWrapper,year,month);
		return Result.OK(pageList);
	}

	/**
	 * 列表查询
	 *
	 * @param meeting
	 * @param req
	 * @return
	 */
	@Operation(summary="会议信息-列表查询")
	@GetMapping(value = "/queryList")
	public Result<List<Meeting>> queryList(Meeting meeting, HttpServletRequest req) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		LambdaQueryWrapper<Meeting> queryWrapper = new LambdaQueryWrapper<>();
		String[] years = req.getParameterMap().get("year");
		String[] months = req.getParameterMap().get("month");
		String year = null;
		String month = null;
		if(years!= null && years.length > 0){
			year = years[0];
		}
		if(months!= null && months.length > 0){
			month = months[0];
		}
		if(StringUtils.isNotBlank(year) && StringUtils.isNotBlank(month)){
			try {
				YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
				LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
				ZoneId zoneId = ZoneId.systemDefault();
				ZonedDateTime zonedDateTime = lastDayOfMonth.atStartOfDay(zoneId);
				Date date = Date.from(zonedDateTime.toInstant());
				queryWrapper.between(Meeting::getMeetingDate,sdf.parse(year+"-"+month+"-01"),date);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		if(StringUtils.isNotEmpty(meeting.getFactoryCode()) && StringUtils.isNotEmpty(meeting.getDeptCode())){
			if(meeting.getFactoryCode().equals(meeting.getDeptCode())){
				meeting.setDeptCode(null);
				queryWrapper.eq(StringUtils.isNotEmpty(meeting.getFactoryCode()),Meeting::getFactoryCode,meeting.getFactoryCode());
			}
		} else if(StringUtils.isNotEmpty(meeting.getDeptCode())){
			if(meeting.getDeptCode().length() <= 9){
				queryWrapper.eq(Meeting::getFactoryCode,meeting.getDeptCode());
				meeting.setDeptCode(null);
			}
		}

		queryWrapper.eq(StringUtils.isNotEmpty(meeting.getDeptCode()),Meeting::getDeptCode,meeting.getDeptCode());
		queryWrapper.or().eq(StringUtils.isNotEmpty(meeting.getDeptCode()),Meeting::getWorkshop,meeting.getDeptCode());
		queryWrapper.or().eq(StringUtils.isNotEmpty(meeting.getDeptCode()),Meeting::getLineCode,meeting.getDeptCode());
		List<Meeting> list = meetingService.queryList(queryWrapper,year,month);
		if(CollectionUtils.isNotEmpty(list)){
			for (Meeting meetingInfo : list) {
				MeetingConfig config = meetingConfigService.getById(meetingInfo.getMeetingType());
				if (null != config && config.getMeetingCycle() != null && config.getMeetingCycle() > 3) {
					meetingInfo.setColor(0);
				} else {
					//1.黄色：做完排期，没到时间
					if (meetingInfo.getMeetingDate()
							.after(new Date()) && meetingInfo.getActive() == 1) {
						meetingInfo.setColor(1);
					}
					//2.灰色：会议取消，必须填写取消理由
					else if (null != meetingInfo.getActive() && meetingInfo.getActive() == 2) {
						meetingInfo.setColor(2);
					}
					//3.红色：会议开始时间30min内签到人数未满足要求，并且未识别取消
					// （如因系统问题无法识别，拍照留存证据，会议开始时间30min内识别取消）
					//4.绿色：
					//a、固定参会人数≤5人的，会议开始时间30min内签到人数≥60%；
					//b、固定参会人数＞5人的，会议开始时间30min内签到人数≥40%
					else {
						// 判断会议开始30分钟内是否满足签到要求
						String meetingDateTime = DateUtils.date2String(meetingInfo.getMeetingDate()) + " " + meetingInfo.getMeetingTime();
						MeetingConfig meetingConfig = meetingConfigService.getById(meetingInfo.getMeetingType());
						if (null != meetingConfig) {
							String userIds = meetingConfig.getParticipant();
							if (StringUtils.isNotEmpty(userIds)) {
								String[] userIdsArr = userIds.split(",");
								long count = getCheckCount(meetingInfo.getId(), userIdsArr, meetingDateTime, 1);
								boolean flag = false;
								//查询固定参会人请假人数
								long qj = getCheckCount(meetingInfo.getId(), userIdsArr, meetingDateTime, 2);
								//固定参数人请假的不计数
								if (userIdsArr.length >= 5) {
									flag = (double) count / (userIdsArr.length - qj) >= 0.6;
								} else {
									flag = (double) count / (userIdsArr.length - qj) >= 0.4;
								}
								if (flag) {
									meetingInfo.setColor(4);
								} else {
									meetingInfo.setColor(3);
								}
							} else {
								meetingInfo.setColor(3);
							}
						}
					}
				}
			}
		}
		return Result.OK(list);
	}

	/**
	 * 查询签到人数
	 * @param id
	 * @param userIdsArr
	 * @param time
	 * @param active
	 * @return
	 */
	private long getCheckCount(String id,String[] userIdsArr ,String time,int active){
        try {
            return meetingCheckService.count(new LambdaQueryWrapper<MeetingCheck>()
                    .eq(MeetingCheck::getMeetingId,id)
                    .in(MeetingCheck::getCheckPerson,userIdsArr)
                    .le(MeetingCheck::getCheckTime, DateUtils.addHalfHour(time))
                    .eq(MeetingCheck::getActive,active));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 *
	 * @param date
	 * @return
	 */
	@Operation(summary="会议信息-通过时间查询")
	@GetMapping(value = "/queryByDate")
	public Result<List<Meeting>> queryByDate(@RequestParam(name="date") String date) {
		return Result.OK(meetingService.list(new LambdaQueryWrapper<Meeting>().eq(Meeting::getMeetingDate,date)));
	}

	/**
	 *
	 * @param deptCode
	 * @return
	 */
	@Operation(summary="会议信息-通过机构编码查询id")
	@GetMapping(value = "/queryDeptByCode")
	public Result<SysDepart> queryDeptByCode(@RequestParam(name="deptCode") String deptCode) {
		return Result.OK(sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,deptCode)));
	}
	
	/**
	 *   添加
	 *
	 * @param meeting
	 * @return
	 */
	@AutoLog(value = "会议信息-添加")
	@Operation(summary="会议信息-添加")
//	@RequiresPermissions("meeting:mes3_meeting:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody Meeting meeting) {
		List<MeetingTemplate> list = meetingTemplateService.getMeetingTemplates(meeting.getMeetingType());
		List<MeetingInfo> meetingList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(list)) {
			if(StringUtils.isNotEmpty(meeting.getDeptCode())){
				String deptCodes = meeting.getDeptCode();
				SysDepart sysDepart = sysDepartService.getById(deptCodes);
				if(null != sysDepart && StringUtils.isNotEmpty(sysDepart.getOrgType())){
					meetingService.setMeetingRecursively(sysDepart, Integer.parseInt(sysDepart.getOrgType()), meeting);
				}
			}
			meeting.setMeetingName(list.get(0).getMeetingName());
			meeting.setParticipant(list.get(0).getParticipant());
			long count = meetingService.count(new LambdaQueryWrapper<Meeting>()
					.eq(Meeting::getMeetingType,meeting.getMeetingType())
					.eq(Meeting::getMeetingName,meeting.getMeetingName())
					.eq(Meeting::getMeetingDate,meeting.getMeetingDate())
					.eq(Meeting::getMeetingTime,meeting.getMeetingTime()));
			if(count < 1) {
				meetingService.save(meeting);
				//新增会议模版
				for (MeetingTemplate meetingTemplate : list) {
					MeetingInfo meetingInfo = new MeetingInfo();
					BeanUtils.copyProperties(meetingTemplate, meetingInfo);
					meetingInfo.setMeetingId(meeting.getId());
					meetingInfo.setTemplateId(meetingTemplate.getId());
					meetingInfo.setId(null);
					meetingList.add(meetingInfo);
				}
				meetingInfoService.saveBatch(meetingList);
				//新增会议评估信息
				meetingService.setTemplate(meeting);
			} else {
				return Result.error("当前时间已经存在相同的会议！");
			}
		} else {
			return Result.error("当前会议类型没有模板，请先添加会议模板！");
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meeting
	 * @return
	 */
	@AutoLog(value = "会议信息-编辑")
	@Operation(summary="会议信息-编辑")
//	@RequiresPermissions("meeting:mes3_meeting:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody Meeting meeting) {
		if(StringUtils.isNotEmpty(meeting.getDeptCode())){
			String deptCodes = meeting.getDeptCode();
			SysDepart sysDepart = sysDepartService.getById(deptCodes);
			if(null != sysDepart && StringUtils.isNotEmpty(sysDepart.getOrgType())){
				meetingService.setMeetingRecursively(sysDepart, Integer.parseInt(sysDepart.getOrgType()), meeting);
			}
		}
		meetingService.updateById(meeting);
		return Result.OK("编辑成功!");
	}

	/**
	 *   取消会议
	 *
	 * @param meeting
	 * @return
	 */
	@AutoLog(value = "会议信息-取消会议")
	@Operation(summary="会议信息-取消会议")
	@RequestMapping(value = "/active", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> active(@RequestBody Meeting meeting) {
		Meeting info = meetingService.getById(meeting.getId());
		info.setActive(2);
		info.setNotes(meeting.getNotes());
		meetingService.updateById(info);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "会议信息-通过id删除")
	@Operation(summary="会议信息-通过id删除")
//	@RequiresPermissions("meeting:mes3_meeting:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		meetingService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议信息-批量删除")
	@Operation(summary="会议信息-批量删除")
	@RequiresPermissions("meeting:mes3_meeting:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<Meeting> queryById(@RequestParam(name="id") String id) {
		Meeting meeting = meetingService.getById(id);
		if(meeting==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meeting);
	}

	/**
	 *   添加
	 *
	 * @param meeting
	 * @return
	 */
	@AutoLog(value = "会议信息-自动排期")
	@Operation(summary="会议信息-自动排期")
//	@RequiresPermissions("meeting:mes3_meeting:add")
	@PostMapping(value = "/scheduleMeeting")
	public Result<String> scheduleMeeting(@RequestBody MeetingScheduleVO meeting) {
		//保存会议排期配置
		if(StringUtils.isNotEmpty(meeting.getMeetingType())){
			MeetingSchedule meetingSchedule = meetingScheduleService.getOne(new LambdaQueryWrapper<MeetingSchedule>().eq(MeetingSchedule::getConfigId,meeting.getMeetingType()).eq(MeetingSchedule::getMeetingType,meeting.getMeetingType()).eq(MeetingSchedule::getStatus,1));
			String scheduleId;
			//如果排期存在，则更新排期，然后删除当前时间后已经排期的会议并重新生成会议
			if(null != meetingSchedule){
				scheduleId = meetingSchedule.getId();
//				meetingSchedule.setMeetingDate(meeting.getDay());
				meetingSchedule.setMeetingTime(meeting.getTime());
				if(StringUtils.isNotEmpty(meeting.getWeek())) {
					//meetingSchedule.setMeetingNum(DateUtils.parseWeekNumber(meeting.getWeek()));
					meetingSchedule.setMeetingNum(Integer.parseInt(meeting.getWeek()));
				}
				if(StringUtils.isNotEmpty(meeting.getWeek())) {
					meetingSchedule.setMeetingWeek(meeting.getDay());
				}
				if(StringUtils.isNotEmpty(meeting.getDayWeek())) {
					meetingSchedule.setMeetingWeek(meeting.getDayWeek());
				}
				meetingSchedule.setMeetingType(String.valueOf(meeting.getType()));
				meetingScheduleService.updateById(meetingSchedule);
				List<Meeting> meetingList = meetingService.list(new LambdaQueryWrapper<Meeting>().eq(Meeting::getScheduleId,meetingSchedule.getId()));
				//删除当前时间后已经排期的会议
				meetingService.remove(
						new LambdaQueryWrapper<Meeting>()
								.eq(Meeting::getScheduleId,meetingSchedule.getId())
								.gt(Meeting::getMeetingDate, DateUtils.localDate2Date(DateUtils.getCurrentDateString()))
				);
				if(CollectionUtils.isNotEmpty(meetingList)) {
					meetingInfoService.remove(new LambdaQueryWrapper<MeetingInfo>().in(MeetingInfo::getMeetingId, meetingList.stream().map(Meeting::getId).collect(Collectors.toList())));
				}
			} else {
				MeetingConfig meetingConfig = meetingConfigService.getById(meeting.getMeetingType());
				//如果排期不存在，则新增排期，然后生成会议信息
				MeetingSchedule schedule = new MeetingSchedule();
				schedule.setFactoryCode(meetingConfig.getFactoryCode());
				schedule.setDeptCode(meetingConfig.getDeptCode());
				schedule.setLineCode(meetingConfig.getLineCode());
//				meetingSchedule.setMeetingDate(meeting.get());
				schedule.setMeetingTime(meeting.getTime());
				if(StringUtils.isNotEmpty(meeting.getWeek())) {
					//meetingSchedule.setMeetingNum(DateUtils.parseWeekNumber(meeting.getWeek()));
					schedule.setMeetingNum(Integer.parseInt(meeting.getWeek()));
				}
				if(StringUtils.isNotEmpty(meeting.getDay())) {
					schedule.setMeetingWeek(meeting.getDay());
				}
				if(StringUtils.isNotEmpty(meeting.getDayWeek())) {
					schedule.setMeetingWeek(meeting.getDayWeek());
				}
				schedule.setMeetingType(String.valueOf(meeting.getType()));
				schedule.setConfigId(meeting.getMeetingType());
				meetingScheduleService.save(schedule);
				scheduleId = schedule.getId();
			}
			//生产并保存会议信息
			meetingService.scheduleMeeting(meeting, scheduleId);
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *   添加
	 *
	 * @param meeting
	 * @return
	 */
	@AutoLog(value = "会议信息-取消自动排期")
	@Operation(summary="会议信息-取消自动排期")
//	@RequiresPermissions("meeting:mes3_meeting:add")
	@PostMapping(value = "/scheduleMeetingCancel")
	public Result<String> scheduleMeetingCancel(@RequestBody MeetingScheduleVO meeting) {
		//保存会议排期配置
		if (StringUtils.isNotEmpty(meeting.getScheduleId())) {
			MeetingSchedule schedule = meetingScheduleService.getById(meeting.getScheduleId());
			//如果排期存在，则更新排期，然后删除当前时间后已经排期的会议并重新生成会议
			if (null != schedule) {
				schedule.setStatus(2);
				meetingScheduleService.updateById(schedule);
				List<Meeting> meetingList = meetingService.list(new LambdaQueryWrapper<Meeting>().eq(Meeting::getScheduleId, meeting.getScheduleId()));
				//删除当前时间后已经排期的会议
				meetingService.remove(new LambdaQueryWrapper<Meeting>().eq(Meeting::getScheduleId, meeting.getScheduleId()).gt(Meeting::getMeetingDate, DateUtils.localDate2Date(DateUtils.getCurrentDateString())));
				if (CollectionUtils.isNotEmpty(meetingList)) {
					meetingInfoService.remove(new LambdaQueryWrapper<MeetingInfo>().in(MeetingInfo::getMeetingId, meetingList.stream().map(Meeting::getId).collect(Collectors.toList())));
				}
			}
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 * 添加
	 *
	 * @param meetingCheckDto
	 * @return
	 */
	@AutoLog(value = "会议签到-二维码")
	@Operation(summary="会议签到-二维码")
	@PostMapping(value = "/generateQr")
	public Result<?> generateQr(@RequestBody MeetingCheckDto meetingCheckDto) {
		return Result.OK("查询成功", meetingService.getMeetingCode(meetingCheckDto));
	}

	/**
	 * 通过id查询
	 *
	 * @param factoryCode
	 * @return
	 */
	@Operation(summary="会议信息-获取工厂地址")
	@GetMapping(value = "/queryFactoryPath")
		public Result<String> getPath(@RequestParam(name="factoryCode") String factoryCode) {
		if(StringUtils.isNotEmpty(factoryCode)){
			if(factoryCode.length() > 9){
				factoryCode = factoryCode.substring(0, 9);
			}
		}
		//获取对应工厂链接地址
		String finalFactoryCode = factoryCode;
		FactoryCodeEnum codeEnum = EnumUtil.getBy(FactoryCodeEnum.class, bean -> {
            return bean.getCode().equals(finalFactoryCode);
        });
		if(null != codeEnum) {
			return Result.OK(codeEnum.getNetWorkAddress());
		} else {
			return Result.error("工厂不存在！");
		}
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meeting
    */
    @RequiresPermissions("meeting:mes3_meeting:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Meeting meeting) {
        return super.exportXls(request, meeting, Meeting.class, "会议信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:mes3_meeting:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Meeting.class);
    }

	@Operation(summary="有效会议-运行统计")
	@PostMapping(value = "/meetingStatistics")
	public Result<List<MeetingTotalDTO>> meetingStatistics(@RequestBody MeetingVO meetingVO) {
		List<MeetingTotalDTO> list = meetingService.meetingStatistics(meetingVO);
		return Result.OK(list);
	}

}
