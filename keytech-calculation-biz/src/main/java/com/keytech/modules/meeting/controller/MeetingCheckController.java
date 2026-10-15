package com.keytech.modules.meeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.meeting.dto.MeetingTotalDTO;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.modules.meeting.entity.MeetingCheck;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.modules.meeting.service.IMeetingCheckService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.meeting.service.IMeetingConfigService;
import com.keytech.modules.meeting.service.IMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 会议签到
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="会议签到")
@RestController
@RequestMapping("/meeting/meetingCheck")
@Slf4j
public class MeetingCheckController extends BaseController<IMeetingCheckService, MeetingCheck> {
	@Autowired
	private IMeetingCheckService meetingCheckService;

	 @Autowired
	private IMeetingService meetingService;

	@Autowired
	 private IMeetingConfigService meetingConfigService;

	@Autowired
	 private ISysUserService sysUserService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingCheck
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议签到-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingCheck>> queryPageList(MeetingCheck meetingCheck,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="999") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<MeetingCheck> queryWrapper = QueryGenerator.initQueryWrapper(meetingCheck, req.getParameterMap());
		Page<MeetingCheck> page = new Page<MeetingCheck>(pageNo, pageSize);
		IPage<MeetingCheck> pageList = meetingCheckService.page(page, queryWrapper);
		Meeting meeting = meetingService.getById(meetingCheck.getMeetingId());
		if(null != meeting) {
			MeetingConfig meetingConfig = meetingConfigService.getById(meeting.getMeetingType());
			if(null != meetingConfig) {
				String userIds = meetingConfig.getParticipant();
				if(StringUtils.isNotEmpty(userIds)) {
					String[] userIdsArr = userIds.split(",");
					if(CollectionUtils.isNotEmpty(pageList.getRecords())) {
						for (MeetingCheck check : pageList.getRecords()) {
							if(Arrays.stream(userIdsArr).anyMatch(check.getCheckPerson()::equals)) {
								check.setCheckPersonType(0);
							} else {
								check.setCheckPersonType(1);
							}
						}
						for(String userId : userIdsArr) {
							//判断固定参会人是否已经签到
							boolean exist = pageList.getRecords().stream().anyMatch(
											check -> Objects.equals(check.getCheckPerson(),userId)
							);
							if(!exist) {
								MeetingCheck check = new MeetingCheck(); // 创建 MeetingCheck 对象
								check.setCheckPerson(userId); // 设置 userId
								check.setMeetingId(meeting.getId());
								check.setActive(0); // 设置默认状态为未签到
								check.setCheckPersonType(0);
								// 设置其他必要的字段
								pageList.getRecords().add(check); // 将 MeetingCheck 对象添加到列表
							}
						}
					} else {
						List<MeetingCheck> checkList = new ArrayList<>();
						for(String userId : userIdsArr) {
							MeetingCheck check = new MeetingCheck(); // 创建 MeetingCheck 对象
							check.setCheckPerson(userId); // 设置 userId
							check.setMeetingId(meeting.getId());
							check.setActive(0); // 设置默认状态为未签到
							check.setCheckPersonType(0);
							// 设置其他必要的字段
							checkList.add(check); // 将 MeetingCheck 对象添加到列表
							pageList.setRecords(checkList); // 设置列表
						}
					}
				} else {
					if(CollectionUtils.isNotEmpty(pageList.getRecords())) {
						for (MeetingCheck check : pageList.getRecords()) {
							check.setCheckPersonType(1);
						}
					}
				}
			} else {
				return Result.OK(null);
			}
		}
		pageList.getRecords().sort(Comparator
					.comparing(MeetingCheck::getCheckPersonType));

		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param meetingCheck
	 * @return
	 */
	@AutoLog(value = "会议签到-添加")
	@Operation(summary="会议签到-添加")
//	@RequiresPermissions("meeting:mes3_meeting_check:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingCheck meetingCheck) {
		Meeting meeting = meetingService.getById(meetingCheck.getMeetingId());
		//判断会议开始时间
		if(null != meeting) {
			Date meetingDate = meeting.getMeetingDate();
			if(!DateUtils.convertToLocalDate(new Date()).equals(DateUtils.convertToLocalDate(meetingDate))) {
				return Result.error("当前签到的会议非当天开始，请确认会议开始时间！");
			}
		}
		Date dateToCheck = new Date();
		meetingCheck.setCheckTime(dateToCheck);
		meetingCheck.setActive(1);
		meetingCheck.setFactoryCode(meeting.getFactoryCode());
		meetingCheck.setDeptCode(meeting.getDeptCode());
		meetingCheck.setWorkshop(meeting.getWorkshop());
		meetingCheck.setLineCode(meeting.getLineCode());
		if(StringUtils.isNotEmpty(meetingCheck.getCheckPerson())){
			SysUser sysUser = sysUserService.getUserByName(meetingCheck.getCheckPerson());
			if(null != sysUser) {
				//获取签到人是否是固定参会人
				MeetingConfig meetingConfig = meetingConfigService.getById(meeting.getMeetingType());
				if(null != meetingConfig) {
					String userIds = meetingConfig.getParticipant();
					if(StringUtils.isNotEmpty(userIds)) {
						String[] userIdsArr = userIds.split(",");
						if(Arrays.stream(userIdsArr).anyMatch(sysUser.getId()::equals)) {
							meetingCheck.setCheckPersonType(0);
						} else {
							meetingCheck.setCheckPersonType(1);
						}
					}
				}
				meetingCheck.setCheckPerson(sysUser.getId());
				long count = meetingCheckService.count(new LambdaQueryWrapper<MeetingCheck>().eq(MeetingCheck::getMeetingId,meeting.getId()).eq(MeetingCheck::getCheckPerson,meetingCheck.getCheckPerson()));
				if(count < 1){
					meetingCheckService.save(meetingCheck);
				}
			} else {
				return Result.error("工号不存在，请输入正确的工号！");
			}
		} else {
			return Result.error("请输入正确的工号！");
		}
		return Result.OK("签到成功");
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingCheck
	 * @return
	 */
	@AutoLog(value = "会议签到-编辑")
	@Operation(summary="会议签到-编辑")
//	@RequiresPermissions("meeting:mes3_meeting_check:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingCheck meetingCheck) {
		if(StringUtils.isEmpty(meetingCheck.getId())){
			Meeting meeting = meetingService.getById(meetingCheck.getMeetingId());
			if(null != meeting) {
				MeetingCheck check = new MeetingCheck();
				check.setMeetingId(meeting.getId());
				check.setCheckPerson(meetingCheck.getCheckPerson());
				check.setCheckTime(new Date());
				check.setActive(meetingCheck.getActive());
				check.setFactoryCode(meeting.getFactoryCode());
				check.setDeptCode(meeting.getDeptCode());
				check.setWorkshop(meeting.getWorkshop());
				check.setLineCode(meeting.getLineCode());
				check.setRemark(meetingCheck.getRemark());
				meetingCheckService.save(check);
			}
		} else {
			meetingCheckService.updateById(meetingCheck);
		}
		return Result.OK("编辑成功!");
	}

	/**
	 *  获取签到详情
	 *
	 * @param meetingCheck
	 * @return
	 */
	@AutoLog(value = "会议签到-获取签到详情")
	@Operation(summary="会议签到-获取签到详情")
	@RequestMapping(value = "/getCheck", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<MeetingCheck> getCheck(@RequestBody MeetingCheck meetingCheck) {
		if(StringUtils.isNotEmpty(meetingCheck.getMeetingId()) && StringUtils.isNotEmpty(meetingCheck.getCheckPerson())){
			MeetingCheck check = meetingCheckService.getOne(new LambdaQueryWrapper<MeetingCheck>()
							.eq(MeetingCheck::getMeetingId,meetingCheck.getMeetingId())
					.eq(MeetingCheck::getCheckPerson,meetingCheck.getCheckPerson()));
				return Result.OK(check);
		} else {
			return null;
		}
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "会议签到-通过id删除")
	@Operation(summary="会议签到-通过id删除")
	@RequiresPermissions("meeting:mes3_meeting_check:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		meetingCheckService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议签到-批量删除")
	@Operation(summary="会议签到-批量删除")
	@RequiresPermissions("meeting:mes3_meeting_check:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingCheckService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 查询固定参会人
	 *
	 * @param meetingId
	 * @return
	 */
	@Operation(summary="会议签到-查询固定参会人")
	@GetMapping(value = "/queryParticipant")
	public Result<String> queryParticipant(@RequestParam(name="meetingId") String meetingId) {
		Meeting meeting = meetingService.getById(meetingId);
		if(null != meeting) {
			MeetingConfig meetingConfig = meetingConfigService.queryByMeetingType(meeting.getMeetingType());
			return Result.OK(meetingConfig.getParticipant());
		}
		return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议签到-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingCheck> queryById(@RequestParam(name="id") String id) {
		MeetingCheck meetingCheck = meetingCheckService.getById(id);
		if(meetingCheck==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingCheck);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meetingCheck
    */
    @RequiresPermissions("meeting:mes3_meeting_check:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingCheck meetingCheck) {
        return super.exportXls(request, meetingCheck, MeetingCheck.class, "会议签到");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:mes3_meeting_check:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MeetingCheck.class);
    }

}
