package com.keytech.modules.meeting.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.modules.meeting.entity.MeetingRecordTemplate;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.meeting.service.IMeetingConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.meeting.service.IMeetingRecordService;
import com.keytech.modules.meeting.service.IMeetingRecordTemplateService;
import com.keytech.modules.meeting.service.IMeetingTemplateService;
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
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.keytech.base.controller.BaseController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 有效会议类型配置
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Tag(name="有效会议类型配置")
@RestController
@RequestMapping("/meeting/meetingConfig")
@Slf4j
public class MeetingConfigController extends BaseController<IMeetingConfigService, MeetingConfig> {
	@Autowired
	private IMeetingConfigService meetingConfigService;

	 @Autowired
	private ISysDepartService sysDepartService;

	 @Autowired
	 private IMeetingRecordTemplateService meetingRecordTemplateService;

	 @Autowired
	 private IMeetingTemplateService meetingTemplateService;
	
	/**
	 * 分页列表查询
	 *
	 * @param meetingConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="有效会议类型配置-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<MeetingConfig>> queryPageList(MeetingConfig meetingConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {

		if (StringUtils.isNotBlank(meetingConfig.getMeetingName())) {
			meetingConfig.setMeetingName("*" + meetingConfig.getMeetingName() + "*");
		}
		if (StringUtils.isNotBlank(meetingConfig.getMeetingType())) {
			meetingConfig.setId(meetingConfig.getMeetingType());
			meetingConfig.setMeetingType(null);
		}
		LambdaQueryWrapper<MeetingConfig> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.like(StringUtils.isNotBlank(meetingConfig.getMeetingName()),MeetingConfig::getMeetingName, meetingConfig.getMeetingName());

		if(StringUtils.isNotEmpty(meetingConfig.getDeptId())){
			queryWrapper.eq(StringUtils.isNotEmpty(meetingConfig.getDeptId()),MeetingConfig::getDeptId, meetingConfig.getDeptId());
		} else {
			if (StringUtils.isNotEmpty(meetingConfig.getFactoryCode()) && StringUtils.isNotEmpty(meetingConfig.getDeptCode())) {
				if (meetingConfig.getFactoryCode().equals(meetingConfig.getDeptCode())) {
					meetingConfig.setDeptCode(null);
					queryWrapper.eq(StringUtils.isNotEmpty(meetingConfig.getFactoryCode()), MeetingConfig::getFactoryCode, meetingConfig.getFactoryCode());
				} else {
					queryWrapper.eq(StringUtils.isNotEmpty(meetingConfig.getDeptCode()), MeetingConfig::getDeptCode, meetingConfig.getDeptCode());
					queryWrapper.or().eq(StringUtils.isNotEmpty(meetingConfig.getDeptCode()), MeetingConfig::getWorkshop, meetingConfig.getDeptCode());
					queryWrapper.or().eq(StringUtils.isNotEmpty(meetingConfig.getDeptCode()), MeetingConfig::getLineCode, meetingConfig.getDeptCode());
				}
			}

		}
		Page<MeetingConfig> page = new Page<MeetingConfig>(pageNo, pageSize);
		IPage<MeetingConfig> pageList = meetingConfigService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param meetingConfig
	 * @return
	 */
	@AutoLog(value = "有效会议类型配置-添加")
	@Operation(summary="有效会议类型配置-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody MeetingConfig meetingConfig) {
		if(StringUtils.isNotEmpty(meetingConfig.getDeptCode())){
			meetingConfig.setDeptId(meetingConfig.getDeptCode());
			SysDepart sysDepart = sysDepartService.getById(meetingConfig.getDeptCode());
			if(null != sysDepart){
				meetingConfigService.setMeetingConfigRecursively(sysDepart, Integer.parseInt(sysDepart.getOrgType()), meetingConfig);
			}
		}
		meetingConfigService.save(meetingConfig);
		//默认增加会议评估模版，老系统模版信息是在前端写死
		//评估模版不与会议类型绑定
		//setTemplate(meetingConfig);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	private void setTemplate(MeetingConfig meetingConfig) {
		for (int i = 1; i <= 10; i++) {
			MeetingRecordTemplate meetingRecordTemplate = new MeetingRecordTemplate();
			meetingRecordTemplate.setMeetingType(meetingConfig.getId());
			meetingRecordTemplate.setFactoryCode(meetingConfig.getFactoryCode());
			meetingRecordTemplate.setWorkshop(meetingConfig.getWorkshop());
			meetingRecordTemplate.setDeptCode(meetingConfig.getDeptCode());
			meetingRecordTemplate.setLineCode(meetingConfig.getLineCode());
			if (i == 1) {
				meetingRecordTemplate.setStandard("每推迟1分钟扣2分");
				meetingRecordTemplate.setContent("会议是否准时开始？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(1);
			} else if (i == 2) {
				meetingRecordTemplate.setStandard("按出席人员比例，如出席80%，则给8分");
				meetingRecordTemplate.setContent("所有要求的出席人是否都按时出席？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(2);
			} else if (i == 3) {
				meetingRecordTemplate.setStandard("扣掉无贡献的（一言不发）人员比例");
				meetingRecordTemplate.setContent("是否每个人都对会议有所贡献？（即有无参与讨论，在会议提供信息）");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(3);
			} else if (i == 4) {
				meetingRecordTemplate.setStandard("如无，得0分；如在会议期间回顾过，得5分；如在会议开始时回顾过，得10分");
				meetingRecordTemplate.setContent("是否回顾了之前的行动跟踪记录？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(4);
			} else if (i == 5) {
				meetingRecordTemplate.setStandard("按依次遵守的议程项目比例，若议程均涉及但没按顺序也要扣分");
				meetingRecordTemplate.setContent("有无标准议程且其是否被遵守？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(5);
			} else if (i == 6) {
				meetingRecordTemplate.setStandard("按回顾过的比例");
				meetingRecordTemplate.setContent("是否回顾了关键绩效指标（KPI）、日程表或计划？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(6);
			} else if (i == 7) {
				meetingRecordTemplate.setStandard("按符合标准的行动比例");
				meetingRecordTemplate.setContent("所采取的行动有无指定责任人和最终期限？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(7);
			} else if (i == 8) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或10分");
				meetingRecordTemplate.setContent("在会议结束时有无回顾行动跟踪记录？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(8);
			} else if (i == 9) {
				meetingRecordTemplate.setStandard("每推迟1分钟扣1分");
				meetingRecordTemplate.setContent("会议是否准时结束？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(9);
			} else if (i == 10) {
				meetingRecordTemplate.setStandard("有跑题现象但未阻止，为0分，有阻止为10分；会上没有出现跑题现象，为10分");
				meetingRecordTemplate.setContent("会议领导者有无控制和管理时间-有无引导人们坚守正题，杜绝跑题？");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(10);
			}
			meetingRecordTemplate.setRecordType(1);
			meetingRecordTemplateService.save(meetingRecordTemplate);
		}

		for (int i = 1; i <= 9; i++) {
			MeetingRecordTemplate meetingRecordTemplate = new MeetingRecordTemplate();
			meetingRecordTemplate.setMeetingType(meetingConfig.getId());
			meetingRecordTemplate.setFactoryCode(meetingConfig.getFactoryCode());
			meetingRecordTemplate.setWorkshop(meetingConfig.getWorkshop());
			meetingRecordTemplate.setDeptCode(meetingConfig.getDeptCode());
			meetingRecordTemplate.setLineCode(meetingConfig.getLineCode());
			if (i == 1) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或10分");
				meetingRecordTemplate.setContent("会议的输入资料在会议之前就已发出并经过回顾");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(1);
			} else if (i == 2) {
				meetingRecordTemplate.setStandard("全部就是全部（95%），是/否 得分--＞0或10分");
				meetingRecordTemplate.setContent("所有活动是否按时全部完成？（＞95%）");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(2);
			} else if (i == 3) {
				meetingRecordTemplate.setStandard("在出现差异时不断讨论差异的原因，包括行动是否能支撑想要的结果或绩效改进，酌情给分。");
				meetingRecordTemplate.setContent("对标准、目标和业务的实际结果进行了回顾，以促进持续改进。");
				meetingRecordTemplate.setWeight(20);
				meetingRecordTemplate.setSort(3);
			} else if (i == 4) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或10分");
				meetingRecordTemplate.setContent("新的行动被指定用于解决提出的问题");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(4);
			} else if (i == 5) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或5分，有主持人主持，为5分");
				meetingRecordTemplate.setContent("会议副主持人或制定的候补主持人在必要时有效主持会议");
				meetingRecordTemplate.setWeight(5);
				meetingRecordTemplate.setSort(5);
			} else if (i == 6) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或10分，没有涉及此情况，为10分");
				meetingRecordTemplate.setContent("需要详细的分组讨论、输入资料和/或决策的问题留待会议之外处理");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(6);
			} else if (i == 7) {
				meetingRecordTemplate.setStandard("正常工作方式");
				meetingRecordTemplate.setContent("执行过程中计划和实际的差异及其根本原因在会议之前就已通过正常的业务流程予以处理");
				meetingRecordTemplate.setWeight(20);
				meetingRecordTemplate.setSort(7);
			} else if (i == 8) {
				meetingRecordTemplate.setStandard("是/否 得分--＞0或5分");
				meetingRecordTemplate.setContent("出席人有无为会议作准备-带来记分卡、日程表等");
				meetingRecordTemplate.setWeight(5);
				meetingRecordTemplate.setSort(8);
			} else if (i == 9) {
				meetingRecordTemplate.setStandard("出席人提前考虑和采取主动，并且除了分配的行动外还采取额外行动");
				meetingRecordTemplate.setContent("出席人遵照会议行动行事并了解促进和实现持续改进的其他方法");
				meetingRecordTemplate.setWeight(10);
				meetingRecordTemplate.setSort(9);
			}
			meetingRecordTemplate.setRecordType(2);
			meetingRecordTemplateService.save(meetingRecordTemplate);
		}
	}
	
	/**
	 *  编辑
	 *
	 * @param meetingConfig
	 * @return
	 */
	@AutoLog(value = "有效会议类型配置-编辑")
	@Operation(summary="有效会议类型配置-编辑")
//	@RequiresPermissions("meeting:mes3_meeting_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody MeetingConfig meetingConfig) {
		if(StringUtils.isNotEmpty(meetingConfig.getDeptCode())
				&& !meetingConfig.getDeptCode().equals(meetingConfig.getDeptId())) {
			meetingConfig.setDeptId(meetingConfig.getDeptCode());
			SysDepart sysDepart = sysDepartService.getById(meetingConfig.getDeptCode());
			if(null != sysDepart){
				meetingConfigService.setMeetingConfigRecursively(sysDepart, Integer.parseInt(sysDepart.getOrgType()), meetingConfig);
			}
		}
		meetingConfigService.updateById(meetingConfig);
		//获取模板数据
		List<MeetingTemplate> meetingTemplateList = meetingTemplateService.lambdaQuery()
				.eq(MeetingTemplate::getMeetingType, meetingConfig.getId())
				.list();
		if(CollUtil.isNotEmpty(meetingTemplateList)){
			meetingTemplateList.forEach(item -> {
				item.setFactoryCode(meetingConfig.getFactoryCode());
				item.setDeptCode(meetingConfig.getDeptCode());
				item.setWorkshop(meetingConfig.getWorkshop());
				item.setLineCode(meetingConfig.getLineCode());
			});
			meetingTemplateService.updateBatchById(meetingTemplateList);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "有效会议类型配置-通过id删除")
	@Operation(summary="有效会议类型配置-通过id删除")
//	@RequiresPermissions("meeting:mes3_meeting_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id") String id) {
		meetingTemplateService.remove(new LambdaQueryWrapper<MeetingTemplate>().eq(MeetingTemplate::getMeetingType,id));
		//meetingRecordTemplateService.remove(new LambdaQueryWrapper<MeetingRecordTemplate>().eq(MeetingRecordTemplate::getMeetingType,id));
		meetingConfigService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "有效会议类型配置-批量删除")
	@Operation(summary="有效会议类型配置-批量删除")
//	@RequiresPermissions("meeting:mes3_meeting_config:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids") String ids) {
		this.meetingConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="有效会议类型配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<MeetingConfig> queryById(@RequestParam(name="id") String id) {
		MeetingConfig meetingConfig = meetingConfigService.getById(id);
		if(meetingConfig==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(meetingConfig);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param meetingConfig
    */
    @RequiresPermissions("meeting:mes3_meeting_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MeetingConfig meetingConfig) {
        return super.exportXls(request, meetingConfig, MeetingConfig.class, "有效会议类型配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("meeting:mes3_meeting_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, MeetingConfig.class);
    }

}
