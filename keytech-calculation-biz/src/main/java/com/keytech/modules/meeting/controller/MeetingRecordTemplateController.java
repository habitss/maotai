package com.keytech.modules.meeting.controller;

import com.keytech.modules.meeting.entity.MeetingRecordTemplate;
import com.keytech.modules.meeting.service.IMeetingRecordTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.keytech.base.controller.BaseController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 会议评估模版
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
@Tag(name="会议评估模版")
@RestController
@RequestMapping("/meeting/meetingRecordTemplate")
@Slf4j
public class MeetingRecordTemplateController extends BaseController<IMeetingRecordTemplateService, MeetingRecordTemplate> {
	@Autowired
	private IMeetingRecordTemplateService meetingRecordTemplateService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingRecordTemplate
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="会议评估模版-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingRecordTemplate>> queryPageList(MeetingRecordTemplate meetingRecordTemplate,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="50") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
        QueryWrapper<MeetingRecordTemplate> queryWrapper = QueryGenerator.initQueryWrapper(meetingRecordTemplate, req.getParameterMap());
		Page<MeetingRecordTemplate> page = new Page<MeetingRecordTemplate>(pageNo, pageSize);
		IPage<MeetingRecordTemplate> pageList = meetingRecordTemplateService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param meetingRecordTemplate
	 * @return
	 */
	@AutoLog(value = "会议评估模版-添加")
	@Operation(summary="会议评估模版-添加")
//	@RequiresPermissions("meeting:mes3_meeting_record_template:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingRecordTemplate meetingRecordTemplate) {
		meetingRecordTemplateService.save(meetingRecordTemplate);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingRecordTemplate
	 * @return
	 */
	@AutoLog(value = "会议评估模版-编辑")
	@Operation(summary="会议评估模版-编辑")
//	@RequiresPermissions("meeting:mes3_meeting_record_template:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingRecordTemplate meetingRecordTemplate) {
		meetingRecordTemplateService.updateById(meetingRecordTemplate);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "会议评估模版-通过id删除")
	@Operation(summary="会议评估模版-通过id删除")
//	@RequiresPermissions("meeting:mes3_meeting_record_template:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		meetingRecordTemplateService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "会议评估模版-批量删除")
	@Operation(summary="会议评估模版-批量删除")
//	@RequiresPermissions("meeting:mes3_meeting_record_template:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.meetingRecordTemplateService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="会议评估模版-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingRecordTemplate> queryById(@RequestParam(name="id",required=true) String id) {
		MeetingRecordTemplate meetingRecordTemplate = meetingRecordTemplateService.getById(id);
		if(meetingRecordTemplate==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingRecordTemplate);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meetingRecordTemplate
    */
//    @RequiresPermissions("meeting:mes3_meeting_record_template:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingRecordTemplate meetingRecordTemplate) {
        return super.exportXls(request, meetingRecordTemplate, MeetingRecordTemplate.class, "会议评估模版");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("meeting:mes3_meeting_record_template:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            return meetingRecordTemplateService.importExcelBy(request, response, MeetingRecordTemplate.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	 @GetMapping(value = "/initTemplate")
	 public Result<?> initTemplate() {
		 List<MeetingRecordTemplate> meetings = meetingRecordTemplateService.list();
		 for(MeetingRecordTemplate meeting : meetings) {
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("会议是否准时开始？")) {
				 meeting.setStandard("每推迟/提前1分钟扣2分，直至扣除达到上限10分");
				 meeting.setContent("会议是否准时开始？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("所有要求的出席人是否都按时出席？")) {
				 meeting.setStandard("按出席人员比例，如应出席10人，实际8人，按时出席率80%，则给8分");
				 meeting.setContent("所有要求的出席人是否都按时出席？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("是否每个人都对会议有所贡献？（即有无参与讨论，在会议提供信息）")) {
				 meeting.setStandard("出席人在会议提供信息、或会上发言或承担会后输出行动，得分=扣掉无贡献的人员比例*权重");
				 meeting.setContent("是否每个人都对会议有所贡献？（即会前提供信息、会上发言，或会后承担行动）");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("是否回顾了之前的行动跟踪记录？")) {
				 meeting.setStandard("如无，得0分；如在会议期间回顾过，得5分；如在会议开始时回顾过，得10分");
				 meeting.setContent("是否回顾了之前的行动跟踪记录？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("有无标准议程且其是否被遵守？")) {
				 meeting.setStandard("完全遵守会议标准议程10分；有缺漏或议程均涉及但没按顺序，按比例给分");
				 meeting.setContent("有无标准议程且其是否被遵守？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("是否回顾了关键绩效指标（KPI）、日程表或计划？")) {
				 meeting.setStandard("按照MCRS会议逻辑图，聚焦于会议拟控制的目标，例如周月会回顾KPI与体系进度，日会回顾PI，项目月会回顾整体推进进度与重点项目进度，按回顾比例给分");
				 meeting.setContent("是否回顾了关键绩效指标（KPI）、日程表或计划？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("所采取的行动有无指定责任人和最终期限？")) {
				 meeting.setStandard("行动SMART中的RT：有指定责任人和最终期限，按符合标准的行动比例计算，例如符合率80%，则给8分");
				 meeting.setContent("所采取的行动有无指定责任人和最终期限？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("在会议结束时有无回顾行动跟踪记录？")) {
				 meeting.setStandard("会议结束时，针对报告中的异常进行行动回顾，输出新的改善行动。是得10分，否得0分");
				 meeting.setContent("在会议结束时有无回顾行动跟踪记录？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("会议是否准时结束？")) {
				 meeting.setStandard("每推迟结束1分钟扣1分，直至上限10分");
				 meeting.setContent("会议是否准时结束？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 1 && meeting.getContent().equals("会议领导者有无控制和管理时间-有无引导人们坚守正题，杜绝跑题？")) {
				 meeting.setStandard("有跑题现象但未阻止，为0分，有阻止且有效为10分，有阻止但无效为5分；会上没有出现跑题现象，为10分");
				 meeting.setContent("会议领导者有无控制和管理时间-有无引导人们坚守正题，杜绝跑题？");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("会议的输入资料在会议之前就已发出并经过回顾")) {
				 meeting.setStandard("参会人清楚会议主题与目标，并清楚目标实现的现状与自身行动完成现状，是得10分，否得0分。");
				 meeting.setContent("会议的输入资料在会议之前就已发出并经过回顾");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("所有活动是否按时全部完成？（＞95%）")) {
				 meeting.setStandard("≥95%的活动按时完成得10分；≥80的活动按时完成得5分，否则得0分");
				 meeting.setContent("所有活动是否按时全部完成？（＞95%）");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("对标准、目标和业务的实际结果进行了回顾，以促进持续改进。")) {
				 meeting.setStandard("异常问题、PI/KPI超标已进行根本原因分析，并输出有效的改进行动；存在问题-5分/处");
				 meeting.setContent("对标准、目标和业务的实际结果进行了回顾，以促进持续改进。");
				 meeting.setWeight(25);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("新的行动被指定用于解决提出的问题")) {
				 meeting.setStandard("针对异常输出新的行动，用于解决提出的问题；存在问题-5分/条");
				 meeting.setContent("新的行动被指定用于解决提出的问题");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("会议副主持人或制定的候补主持人在必要时有效主持会议")) {
				 meeting.setStandard("有代理人主持，为5分；没有，0分");
				 meeting.setContent("会议副主持人或制定的候补主持人在必要时有效主持会议");
				 meeting.setWeight(5);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("需要详细的分组讨论、输入资料和/或决策的问题留待会议之外处理")) {
				 meeting.setStandard("为分析根本原因、输出有效行动的需要详细的分组讨论、输入资料和/或决策的问题会议之外处理，未在会上讨论，10分；会上简单讨论，（0-10分钟）5分；会上讨论超过10分钟，0分");
				 meeting.setContent("需要详细的分组讨论、输入资料和/或决策的问题留待会议之外处理");
				 meeting.setWeight(10);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("执行过程中计划和实际的差异及其根本原因在会议之前就已通过正常的业务流程予以处理")) {
				 meeting.setStandard("执行过程中计划和实际的差异及其根本原因在会议之前就已通过正常的业务流程予以处理；存在未按既定流程处理的问题-5分/个。");
				 meeting.setContent("执行过程中计划和实际的差异及其根本原因在会议之前就已通过正常的业务流程予以处理");
				 meeting.setWeight(20);
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("出席人有无为会议作准备-带来记分卡、日程表等")) {
				 meetingRecordTemplateService.removeById(meeting.getId());
			 }
			 if (meeting.getRecordType() == 2 && meeting.getContent().equals("出席人遵照会议行动行事并了解促进和实现持续改进的其他方法")) {
				 meeting.setStandard("权重10分：出席人遵照会议行动行事并了解促进和实现持续改进的其他方法，是10分；否0分。");
				 meeting.setContent("出席人遵照会议行动行事并了解促进和实现持续改进的其他方法");
				 meeting.setWeight(10);
			 }
			 meetingRecordTemplateService.updateById(meeting);


		 }
		 return null;
	 }

}
