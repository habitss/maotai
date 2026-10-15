package com.keytech.modules.meeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.modules.meeting.entity.MeetingInfo;
import com.keytech.modules.meeting.entity.MeetingLinks;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.meeting.service.IMeetingInfoService;
import com.keytech.modules.meeting.service.IMeetingLinksService;
import com.keytech.modules.meeting.service.IMeetingService;
import com.keytech.modules.meeting.service.IMeetingTemplateService;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 会议信息
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="会议信息")
@RestController
@RequestMapping("/meeting/meetingInfo")
@Slf4j
public class MeetingInfoController extends BaseController<IMeetingInfoService, MeetingInfo> {
	@Autowired
	private IMeetingInfoService meetingInfoService;

	@Autowired
	private IMeetingTemplateService meetingTemplateService;

	@Autowired
	private IMeetingService meetingService;

	@Autowired
	private IMeetingLinksService meetingLinksService;
	
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
	public Result<IPage<MeetingInfo>> queryPageList(MeetingInfo meeting,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="30") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		String[] type = req.getParameterMap().get("dataId");
		if(type!= null && type.length > 0){
			meeting.setMeetingId(type[0]);
		}
        QueryWrapper<MeetingInfo> queryWrapper = QueryGenerator.initQueryWrapper(meeting, req.getParameterMap());
		Page<MeetingInfo> page = new Page<>(pageNo, pageSize);
		IPage<MeetingInfo> pageList = meetingInfoService.page(page, queryWrapper);
		List<MeetingInfo> records = pageList.getRecords();
		if(CollectionUtils.isEmpty(records)){
			Meeting meetingInfo = meetingService.getById(meeting.getMeetingId());
			List<MeetingTemplate> templateList = meetingTemplateService.list(new LambdaQueryWrapper<MeetingTemplate>().eq(MeetingTemplate::getMeetingType,meetingInfo.getMeetingType()));
			if(CollectionUtils.isNotEmpty(templateList)){
				List<MeetingInfo> saveList = new ArrayList<>();
				for(MeetingTemplate template:templateList){
					MeetingInfo info = new MeetingInfo();
					BeanUtils.copyProperties(template,info);
					info.setMeetingId(meeting.getMeetingId());
					info.setTemplateId(template.getId());
					info.setId(null);
					saveList.add(info);
				}
				meetingInfoService.saveBatch(saveList);
			}
		}
		pageList = meetingInfoService.page(page, queryWrapper);
		if(CollectionUtils.isNotEmpty(pageList.getRecords())){
			for(MeetingInfo m : pageList.getRecords()){
				List<MeetingLinks> links = meetingLinksService.list(new LambdaQueryWrapper<MeetingLinks>()
						.eq(MeetingLinks::getTemplateId, m.getTemplateId()));
				MeetingLinks link = new MeetingLinks();
				link.setTemplateId(m.getTemplateId());
				link.setLink(m.getLink());
				link.setLinkAddress(m.getLinkAddress());
				links.add(link);
				if(CollectionUtils.isNotEmpty(links)){
					m.setLinks(links);
				}
			}
		}
		return Result.OK(pageList);
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
	public Result<String> add(@RequestBody MeetingInfo meeting) {
		meetingInfoService.save(meeting);
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
	public Result<String> edit(@RequestBody MeetingInfo meeting) {
		MeetingInfo	meetingInfo = meetingInfoService.getById(meeting.getId());
		if(null == meetingInfo){
			meetingInfoService.save(meeting);
		} else {
			meetingInfoService.updateById(meeting);
			if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(meeting.getLinks())){
				for (MeetingLinks link : meeting.getLinks()) {
					if(StringUtils.isNotEmpty(link.getLink()) && StringUtils.isNotEmpty(link.getLinkAddress())){
						link.setTemplateId(meeting.getTemplateId());
						meetingLinksService.saveOrUpdate(link);
					}
				}
			}
		}
		return Result.OK("编辑成功!");
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
		meetingInfoService.removeById(id);
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
//	@RequiresPermissions("meeting:mes3_meeting:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingInfoService.removeByIds(Arrays.asList(ids.split(",")));
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
	public Result<MeetingInfo> queryById(@RequestParam(name="id") String id) {
		MeetingInfo meeting = meetingInfoService.getById(id);
		if(meeting==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meeting);
	}

}
