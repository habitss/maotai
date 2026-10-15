package com.keytech.modules.meeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.modules.meeting.entity.MeetingInfo;
import com.keytech.modules.meeting.entity.MeetingLinks;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.meeting.service.IMeetingInfoService;
import com.keytech.modules.meeting.service.IMeetingLinksService;
import com.keytech.modules.meeting.service.IMeetingService;
import com.keytech.modules.meeting.service.IMeetingTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 会议模版
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Tag(name="会议模版")
@RestController
@RequestMapping("/meeting/meetingTemplate")
@Slf4j
public class MeetingTemplateController extends BaseController<IMeetingTemplateService, MeetingTemplate> {
	@Autowired
	private IMeetingTemplateService meetingTemplateService;

	@Autowired
	private IMeetingInfoService meetingInfoService;

	@Autowired
	private IMeetingService meetingService;

	@Autowired
	private IMeetingLinksService meetingLinksService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingTemplate
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议模版-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingTemplate>> queryPageList(MeetingTemplate meetingTemplate,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="50") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		String[] type = req.getParameterMap().get("dataId");
		if(type!= null && type.length > 0){
			meetingTemplate.setMeetingType(type[0]);
		}
        QueryWrapper<MeetingTemplate> queryWrapper = QueryGenerator.initQueryWrapper(meetingTemplate, req.getParameterMap());
		Page<MeetingTemplate> page = new Page<>(pageNo, pageSize);
		IPage<MeetingTemplate> pageList = meetingTemplateService.page(page, queryWrapper);
		if(CollectionUtils.isNotEmpty(pageList.getRecords())){
			for(MeetingTemplate m : pageList.getRecords()){
				List<MeetingLinks> links = meetingLinksService.list(new LambdaQueryWrapper<MeetingLinks>()
						.eq(MeetingLinks::getTemplateId, m.getId()));
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
	 * @param meetingTemplate
	 * @return
	 */
	@AutoLog(value = "会议模版-添加")
	@Operation(summary="会议模版-添加")
//	@RequiresPermissions("meeting:mes3_meeting_template:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingTemplate meetingTemplate) {
		meetingTemplateService.save(meetingTemplate);
		if(CollectionUtils.isNotEmpty(meetingTemplate.getLinks())){
			for (MeetingLinks link : meetingTemplate.getLinks()) {
				if(StringUtils.isNotEmpty(link.getLink()) && StringUtils.isNotEmpty(link.getLinkAddress())){
					link.setTemplateId(meetingTemplate.getId());
					meetingLinksService.saveOrUpdate(link);
				}
			}
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingTemplate
	 * @return
	 */
	@AutoLog(value = "会议模版-编辑")
	@Operation(summary="会议模版-编辑")
//	@RequiresPermissions("meeting:mes3_meeting_template:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingTemplate meetingTemplate) {
		meetingTemplateService.updateById(meetingTemplate);
		if(CollectionUtils.isNotEmpty(meetingTemplate.getLinks())){
			//先删除原有关联链接
			meetingLinksService.remove(new LambdaQueryWrapper<MeetingLinks>()
					.eq(MeetingLinks::getTemplateId, meetingTemplate.getId()));
			for (MeetingLinks link : meetingTemplate.getLinks()) {
				if(StringUtils.isNotEmpty(link.getLink()) && StringUtils.isNotEmpty(link.getLinkAddress())){
					link.setTemplateId(meetingTemplate.getId());
					meetingLinksService.saveOrUpdate(link);
				}
			}
		}
		List<MeetingInfo> infoList = meetingInfoService.list(new LambdaQueryWrapper<MeetingInfo>()
				.eq(MeetingInfo::getTemplateId, meetingTemplate.getId()));
		for (MeetingInfo info : infoList) {
			Meeting meeting =meetingService.getById(info.getMeetingId());
			if(meeting!= null && meeting.getMeetingDate().after(DateUtils.getCurrentDate())){
				info.setLink(meetingTemplate.getLink());
				info.setLinkAddress(meetingTemplate.getLinkAddress());
				info.setContent(meetingTemplate.getContent());
				info.setContentTitle(meetingTemplate.getContentTitle());
				info.setTime(meetingTemplate.getTime());
				info.setReporter(meetingTemplate.getReporter());
				info.setAgent(meetingTemplate.getAgent());
				info.setSort(meetingTemplate.getSort());
				meetingInfoService.saveOrUpdate(info);
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
	@AutoLog(value = "会议模版-通过id删除")
	@Operation(summary="会议模版-通过id删除")
//	@RequiresPermissions("meeting:mes3_meeting_template:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		meetingTemplateService.removeById(id);
		List<MeetingInfo> infoList = meetingInfoService.list(new LambdaQueryWrapper<MeetingInfo>()
				.eq(MeetingInfo::getTemplateId, id));
		for (MeetingInfo info : infoList) {
			Meeting meeting =meetingService.getById(info.getMeetingId());
			if(meeting!= null && meeting.getMeetingDate().after(DateUtils.getCurrentDate())){
				meetingInfoService.removeById(info);
			}
		}
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议模版-批量删除")
	@Operation(summary="会议模版-批量删除")
//	@RequiresPermissions("meeting:mes3_meeting_template:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingTemplateService.removeByIds(Arrays.asList(ids.split(",")));
		List<MeetingInfo> infoList = meetingInfoService.list(new LambdaQueryWrapper<MeetingInfo>()
				.in(MeetingInfo::getTemplateId, Arrays.asList(ids.split(","))));
		for (MeetingInfo info : infoList) {
			Meeting meeting =meetingService.getById(info.getMeetingId());
			if(meeting!= null && meeting.getMeetingDate().after(DateUtils.getCurrentDate())){
				meetingInfoService.removeById(info);
			}
		}
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议模版-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingTemplate> queryById(@RequestParam(name="id") String id) {
		MeetingTemplate meetingTemplate = meetingTemplateService.getById(id);
		if(meetingTemplate==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingTemplate);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meetingTemplate
    */
//    @RequiresPermissions("meeting:mes3_meeting_template:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingTemplate meetingTemplate) {
        return super.exportXls(request, meetingTemplate, MeetingTemplate.class, "会议模版");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("meeting:mes3_meeting_template:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)  throws IOException {
		return meetingTemplateService.importExcelBy(request, response, MeetingTemplate.class);
    }

}
