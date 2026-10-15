package com.keytech.modules.meeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.meeting.entity.MeetingSchedule;
import com.keytech.modules.meeting.service.IMeetingScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @Description: 会议排期
 * @Author: jeecg-boot
 * @Date:   2025-04-16
 * @Version: V1.0
 */
@Tag(name="会议排期")
@RestController
@RequestMapping("/meeting/meetingSchedule")
@Slf4j
public class MeetingScheduleController extends BaseController<IMeetingScheduleService, MeetingSchedule> {
	@Autowired
	private IMeetingScheduleService meetingScheduleService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingSchedule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议排期-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingSchedule>> queryPageList(MeetingSchedule meetingSchedule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<MeetingSchedule> queryWrapper = QueryGenerator.initQueryWrapper(meetingSchedule, req.getParameterMap());
		Page<MeetingSchedule> page = new Page<MeetingSchedule>(pageNo, pageSize);
		IPage<MeetingSchedule> pageList = meetingScheduleService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param meetingSchedule
	 * @return
	 */
	@AutoLog(value = "会议排期-添加")
	@Operation(summary="会议排期-添加")
	@RequiresPermissions("meeting:meeting_schedule:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingSchedule meetingSchedule) {
		meetingScheduleService.save(meetingSchedule);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingSchedule
	 * @return
	 */
	@AutoLog(value = "会议排期-编辑")
	@Operation(summary="会议排期-编辑")
	@RequiresPermissions("meeting:meeting_schedule:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingSchedule meetingSchedule) {
		meetingScheduleService.updateById(meetingSchedule);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "会议排期-通过id删除")
	@Operation(summary="会议排期-通过id删除")
	@RequiresPermissions("meeting:meeting_schedule:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		meetingScheduleService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议排期-批量删除")
	@Operation(summary="会议排期-批量删除")
	@RequiresPermissions("meeting:meeting_schedule:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.meetingScheduleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议排期-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingSchedule> queryById(@RequestParam(name="id",required=true) String id) {
		MeetingSchedule meetingSchedule = meetingScheduleService.getById(id);
		if(meetingSchedule==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingSchedule);
	}

	 /**
	  * 通过meetingType查询
	  *
	  * @param meetingType
	  * @return
	  */
	 @Operation(summary="会议排期-通过meetingType查询")
	 @GetMapping(value = "/queryByMeetingType")
	 public Result<MeetingSchedule> queryByMeetingType(@RequestParam(name="meetingType") String meetingType) {
		 if(StringUtils.isNotEmpty(meetingType)) {
			 MeetingSchedule meetingSchedule = meetingScheduleService.getOne(new LambdaQueryWrapper<MeetingSchedule>().eq(MeetingSchedule::getConfigId, meetingType).eq(MeetingSchedule::getStatus, 1));
			 if(null != meetingSchedule) {
				 return Result.OK(meetingSchedule);
			 } else {
			 	return Result.OK("无排期");
			 }
		 }
		 return Result.OK("无排期");
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param meetingSchedule
    */
    @RequiresPermissions("meeting:meeting_schedule:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingSchedule meetingSchedule) {
        return super.exportXls(request, meetingSchedule, MeetingSchedule.class, "会议排期");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:meeting_schedule:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MeetingSchedule.class);
    }

}
