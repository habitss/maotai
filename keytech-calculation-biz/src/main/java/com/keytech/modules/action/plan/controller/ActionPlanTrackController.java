package com.keytech.modules.action.plan.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryQueryDto;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.project.management.service.IProjectManagementActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 行动计划跟踪
 * @Author: jeecg-boot
 * @Date:   2025-01-08
 * @Version: V1.0
 */
@Tag(name="行动计划跟踪")
@RestController
@RequestMapping("/action/plan/actionPlanTrack")
@Slf4j
public class ActionPlanTrackController extends BaseController<IActionPlanTrackService, ActionPlanTrack> {
	@Autowired
	private IActionPlanTrackService actionPlanTrackService;

	@Autowired
	private IFiveWhyDetailService fiveWhyDetailService;

	@Autowired
	private IProjectManagementActionService projectManagementActionService;
	@Resource
	private JeecgBaseConfig jeecgBaseConfig;

	/**
	 * 分页列表查询
	 *
	 * @param actionPlanTrack
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="行动计划跟踪-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<ActionPlanTrack>> queryPageList(ActionPlanTrack actionPlanTrack,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
		Page<ActionPlanTrack> page = new Page<ActionPlanTrack>(pageNo, pageSize);
		IPage<ActionPlanTrack> pageList = actionPlanTrackService.page(page, getQuery(actionPlanTrack,  req));
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param actionPlanTrack
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-添加")
	@Operation(summary="行动计划跟踪-添加")
	@RequiresPermissions("action.plan:mes3_action_plan_track:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody ActionPlanTrack actionPlanTrack) {
		actionPlanTrackService.save(actionPlanTrack);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

	/**
	 *   批量添加
	 *
	 * @param list
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-批量添加")
	@Operation(summary="行动计划跟踪-批量添加")
	@PostMapping(value = "/addBatch")
	public Result<String> addBatch(@RequestBody List<ActionPlanTrack> list) {
		//修改行动计划数据
		actionPlanTrackService.saveOrUpdateBatch(list);
		//修改5w分析数据
		List<FiveWhyDetail> fiveWhyDetailList = new ArrayList<>();
		for(ActionPlanTrack actionPlanTrack : list){
			//判断是否和5why分析关联，如果是则更新5why分析
			if(StrUtil.isNotBlank(actionPlanTrack.getFiveWhyMeasuresId())){
				FiveWhyDetail fiveWhyDetail = new FiveWhyDetail();
				BeanUtils.copyProperties(actionPlanTrack, fiveWhyDetail);
				fiveWhyDetail.setId(actionPlanTrack.getFiveWhyMeasuresId());
				fiveWhyDetailList.add(fiveWhyDetail);
			}
		}
		if(CollUtil.isNotEmpty(fiveWhyDetailList)){
			fiveWhyDetailService.updateBatchById(fiveWhyDetailList);
		}
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param actionPlanTrack
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-编辑")
	@Operation(summary="行动计划跟踪-编辑")
	@RequiresPermissions("action.plan:mes3_action_plan_track:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody ActionPlanTrack actionPlanTrack) {
		//判断并设置延期时间
		if(null != actionPlanTrack.getThreeTime()){
			actionPlanTrack.setDelayCount(3);
		}else if(null != actionPlanTrack.getTwoTime()){
			actionPlanTrack.setDelayCount(2);
		}else if(null != actionPlanTrack.getOneTime()){
			actionPlanTrack.setDelayCount(1);
		}
		//修改行动计划
		actionPlanTrackService.updateById(actionPlanTrack);

		//判断是否和5why分析关联，如果是则更新5why分析
		if(StrUtil.isNotBlank(actionPlanTrack.getFiveWhyMeasuresId())){
			FiveWhyDetail fiveWhyDetail = new FiveWhyDetail();
			BeanUtils.copyProperties(actionPlanTrack, fiveWhyDetail);
			fiveWhyDetail.setId(actionPlanTrack.getFiveWhyMeasuresId());
			fiveWhyDetailService.updateById(fiveWhyDetail);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-通过id删除")
	@Operation(summary="行动计划跟踪-通过id删除")
	@RequiresPermissions("action.plan:mes3_action_plan_track:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		actionPlanTrackService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-批量删除")
	@Operation(summary="行动计划跟踪-批量删除")
	@RequiresPermissions("action.plan:mes3_action_plan_track:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.actionPlanTrackService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="行动计划跟踪-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ActionPlanTrack> queryById(@RequestParam(name="id",required=true) String id) {
		ActionPlanTrack actionPlanTrack = actionPlanTrackService.getById(id);
		if(actionPlanTrack==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(actionPlanTrack);
	}

	/**
	 * 定时任务
	 * @param paramJson
	 * @return
	 */
	@AutoLog(value = "行动计划跟踪-定时任务")
	@Operation(summary="行动计划跟踪-定时任务")
	@PostMapping(value = "/updateDataByJob")
	public Result<String> updateDataByJob(@RequestBody String paramJson) {
		actionPlanTrackService.updateDataByJob(paramJson);
		projectManagementActionService.updateDataByJob();
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}

    /**
    * 导出excel
    *
    * @param request
    * @param actionPlanTrack
    */
    @RequiresPermissions("action.plan:mes3_action_plan_track:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ActionPlanTrack actionPlanTrack) {
		String title = "行动计划跟踪";
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		List<ActionPlanTrack> exportList = this.baseService.list(getQuery(actionPlanTrack,  request));
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject("fileName", "行动计划跟踪");
		mv.addObject("entity", ActionPlanTrack.class);
		ExportParams exportParams = new ExportParams(title + "报表", "导出人:" + sysUser.getRealname(), "行动计划跟踪");
		exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
		mv.addObject("params", exportParams);
		mv.addObject("data", exportList);
		return mv;
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("action.plan:mes3_action_plan_track:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return baseService.importExcelBy(request, response, ActionPlanTrack.class);
    }

	/**
	 * 行动计划汇总数据查询
	 * @return
	 */
	@Operation(summary="行动计划汇总数据查询")
	@GetMapping(value = "/querySummaryChartData")
	public Result<?> querySummaryChartData(ActionPlanSummaryQueryDto queryDto) {
		return Result.OK(actionPlanTrackService.getSummaryData(queryDto));
	}

	/**
	 * 行动计划汇总数据查询
	 * @return
	 */
	@Operation(summary="行动计划汇总数据查询")
	@GetMapping(value = "/queryIndexSummaryData")
	public Result<?> queryIndexSummaryData(WSActionStatisticsDto dto) {
		return Result.OK(actionPlanTrackService.queryIndexSummaryData(dto));
	}

	private QueryWrapper<ActionPlanTrack> getQuery(ActionPlanTrack actionPlanTrack, HttpServletRequest req){
		if(StrUtil.isNotBlank(actionPlanTrack.getSurplusDay())){
			actionPlanTrack.setSurplusDay("le "+actionPlanTrack.getSurplusDay());
		}
		QueryWrapper<ActionPlanTrack> queryWrapper = QueryGenerator.initQueryWrapper(actionPlanTrack, req.getParameterMap());
		queryWrapper.orderByDesc("id");
		//处理相关部门搜索
		if(StrUtil.isNotBlank(actionPlanTrack.getDeptIds())){
			List<String> list = Arrays.asList(actionPlanTrack.getDeptIds().split(","));
			queryWrapper.and(wrapper -> wrapper
					.in("duty_dept_id", list)
					.or().in("execute_dept_id", list)
					.or().in("verify_dept_id", list)
					.or().in("put_dept_id", list)
			);
		}
		//处理相关人员搜索
		if(StrUtil.isNotBlank(actionPlanTrack.getUserIds())){
			List<String> list = Arrays.asList(actionPlanTrack.getUserIds().split(","));
			queryWrapper.and(wrapper -> wrapper
					.in("duty_user_id", list)
					.or().in("execute_user_id", list)
					.or().in("verify_user_id", list)
					.or().in("put_user_id", list)
			);
		}
		String selections = req.getParameter("selections");
		if (StrUtil.isNotBlank(selections)) {
			List<String> selectionList = Arrays.asList(selections.split(","));
			queryWrapper.in("id", selectionList);
		}
		return queryWrapper;
	}
}
