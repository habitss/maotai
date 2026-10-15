package com.keytech.modules.plan.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.mdm3.entity.MdmPostDevice;
import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.mdm3.entity.MdmTaskAutoTriggerConfig;
import com.keytech.modules.mdm3.entity.MdmTaskItem;
import com.keytech.modules.mdm3.service.IMdmPostDeviceService;
import com.keytech.modules.mdm3.service.IMdmTaskItemService;
import com.keytech.modules.mdm3.service.IMdmTaskService;
import com.keytech.modules.meeting.dto.MeetingTotalDTO;
import com.keytech.modules.meeting.dto.MeetingVO;
import com.keytech.modules.plan.dto.PlanOrderRequestVO;
import com.keytech.modules.plan.dto.PlanOrderResultTotalDTO;
import com.keytech.modules.plan.dto.PlanOrderTotalDTO;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.modules.plan.entity.PlanOrder;
import com.keytech.modules.plan.enums.PlanAppSearchEnum;
import com.keytech.modules.plan.service.IPlanOrderService;
import com.keytech.modules.plan.service.IPlanService;
import com.keytech.modules.post.record.service.ITakeUpPostService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 计划工单-下发任务
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Tag(name="计划工单-下发任务")
@RestController
@RequestMapping("/plan/planOrder")
@Slf4j
public class PlanOrderController extends BaseController<IPlanOrderService, PlanOrder> {
	@Autowired
	private IPlanService planService;
	@Autowired
	private IPlanOrderService planOrderService;
	@Autowired
	private ITakeUpPostService postRecordService;
	@Autowired
	private IMdmPostDeviceService postDeviceService;
	@Autowired
	private IMdmTaskService mdmTaskService;
	@Autowired
	private IMdmTaskItemService mdmTaskItemService;
	
	/**
	 * 分页列表查询
	 *
	 * @param planOrder
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="计划工单-下发任务-分页列表查询")
	@GetMapping(value = "/queryPageList")
	public Result<IPage<PlanOrder>> queryPageList(PlanOrder planOrder,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   @RequestParam(name = "orderField",required = false) String orderField,
                                   @RequestParam(name = "orderType",required = false) String orderType,
                                   @RequestParam(name = "selectFields",required = false) String selectFields,
								   HttpServletRequest req) {
//		List<MdmPostDevice> mdmPostDeviceList = new ArrayList<>();
//		if(null != planOrder.getPostDevice() && planOrder.getPostDevice()){
//			mdmPostDeviceList = postDeviceService.lambdaQuery()
//					.in(MdmPostDevice::getPostId, Arrays.asList(planOrder.getPostId().split( ",")))
//					.list();
//			planOrder.setPostId(null);
//		}
        QueryWrapper<PlanOrder> queryWrapper = QueryGenerator.initQueryWrapper(planOrder, req.getParameterMap());
//		if(null != planOrder.getPostDevice() && planOrder.getPostDevice()){
//			queryWrapper.in("device_id", mdmPostDeviceList.stream()
//					.map(MdmPostDevice::getDeviceId)
//					.toList());
//		}
		if(StrUtil.isNotBlank(planOrder.getAppSearchType())){
			if(PlanAppSearchEnum.SEARCH_TYPE_1.code.equals(planOrder.getAppSearchType())){
				queryWrapper.eq("task_kind_code", 3);
			}else if(PlanAppSearchEnum.SEARCH_TYPE_2.code.equals(planOrder.getAppSearchType())){
				queryWrapper.eq("task_kind_code", 0);
				queryWrapper.isNull("special_type");
			}else if(PlanAppSearchEnum.SEARCH_TYPE_3.code.equals(planOrder.getAppSearchType())){
				queryWrapper.eq("task_kind_code", 0);
				queryWrapper.in("special_type", Arrays.asList(0, 1));
			}else if("0,10,20".equals(planOrder.getAppSearchType())){
				queryWrapper.in("task_kind_code", Arrays.asList(0,10,20));
				queryWrapper.isNull("special_type");
			}else{
				queryWrapper.eq("task_kind_code", planOrder.getAppSearchType());
			}
		}
		if(StrUtil.isNotBlank(planOrder.getTaskItemName())){
			//根据任务项名称查询任务
			List<MdmTaskItem> mdmTaskItemTaskList = mdmTaskItemService.lambdaQuery()
					.like(MdmTaskItem::getItemName, planOrder.getTaskItemName())
					.list();
			if(CollUtil.isEmpty(mdmTaskItemTaskList)){
				return null;
			}
			//获取任务中的所有id
			Set<String> uniqueTaskIds = mdmTaskItemTaskList.stream()
					.map(MdmTaskItem::getTaskId)
					.collect(Collectors.toSet());
			queryWrapper.in("task_id", uniqueTaskIds);
		}
		queryWrapper.orderByDesc("id");
		Page<PlanOrder> page = new Page<PlanOrder>(pageNo, pageSize);
		IPage<PlanOrder> pageList = planOrderService.page(page, queryWrapper);
		if(!pageList.getRecords().isEmpty()){
			List<String> taskIds = pageList.getRecords().stream()
					.map(PlanOrder::getTaskId)
					.toList();
			List<MdmTask> taskList = mdmTaskService.lambdaQuery().in(MdmTask::getId, taskIds).list();
			Map<String, MdmTask> taskMap = taskList.stream()
					.collect(Collectors.toMap(
							//组合key
							MdmTask::getId,
							config -> config,
							//如果key冲突，保留第一个
							(existing, replacement) -> existing
					));
			pageList.getRecords().forEach(item -> {
				MdmTask task = taskMap.get(item.getTaskId());
				if(null != task){
					item.setAutoTriggerFrequencyValue(task.getAutoTriggerFrequencyValue());
					item.setAutoTriggerFrequencyType(task.getAutoTriggerFrequencyType());
				}
			});
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param planOrder
	 * @return
	 */
	@AutoLog(value = "计划工单-下发任务-添加")
	@Operation(summary="计划工单-下发任务-添加")
	@RequiresPermissions("plan:mes3_plan_order:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PlanOrder planOrder) {
		planOrderService.save(planOrder);
		return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  编辑
	 *
	 * @param planOrder
	 * @return
	 */
	@AutoLog(value = "计划工单-下发任务-编辑")
	@Operation(summary="计划工单-下发任务-编辑")
	@RequiresPermissions("plan:mes3_plan_order:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PlanOrder planOrder) {
		planOrderService.updateById(planOrder);
		return Result.OK("编辑成功!");
	}

	/**
	 *  取消任务
	 *
	 * @param planOrder
	 * @return
	 */
	@AutoLog(value = "计划工单-取消任务-编辑")
	@Operation(summary="计划工单-取消任务-编辑")
	@RequestMapping(value = "/cancel", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> cancel(@RequestBody PlanOrder planOrder) {
		if(StrUtil.isNotBlank(planOrder.getIds())){
			// 批量取消
			List<String> idList = Arrays.asList(planOrder.getIds().split(","));
			planOrderService.lambdaUpdate()
					.set(PlanOrder::getActiveStatus, planOrder.getActiveStatus())
					.set(PlanOrder::getActiveUser, planOrder.getActiveUser())
					.set(PlanOrder::getRemark, planOrder.getRemark())
					.in(PlanOrder::getId, idList)
					.update();
		}else{
			planOrderService.updateById(planOrder);
		}
		return Result.OK("编辑成功!");
	}

	/**
	 *  任务2审核
	 * @param planOrder 工单参数
	 * @return 结果
	 */
	@AutoLog(value = "计划工单-任务取消审核")
	@Operation(summary="计划工单-任务取消审核")
	@RequiresPermissions("plan:mes3_plan_order:edit")
	@RequestMapping(value = "/review", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> review(@RequestBody PlanOrder planOrder) {
		//生产计划需要另外做处理
		if("3".equals(planOrder.getTaskKindCode())){
			//获取当前登录用户
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			//获取用户当前所上岗位
			List<TakeUpPost> takeUpPostList = postRecordService.lambdaQuery()
					.eq(TakeUpPost::getUsername, sysUser.getUsername())
					.eq(TakeUpPost::getStatus, 0)
					.list();
			if(CollUtil.isEmpty(takeUpPostList)){
				throw new JeecgBootException("您未上岗无法操作！");
			}
//			//获取所有岗位id
//			List<Integer> postIds = takeUpPostList.stream()
//					.map(TakeUpPost::getPostId)
//					.toList();
//			//查询计划中的计划操作岗是否有当前所上岗位
//			List<Plan> planList = planService.lambdaQuery()
//					.eq(Plan::getId, planOrder.getPlanId())
//					.in(Plan::getTriggerPostId, postIds)
//					.list();
//			if(CollUtil.isEmpty(planList)){
//				throw new JeecgBootException("您当前所上岗位无权限审核！");
//			}
		}
		if(StrUtil.isNotBlank(planOrder.getIds())){
			// 批量审核
			List<String> idList = Arrays.asList(planOrder.getIds().split(","));
			planOrderService.lambdaUpdate()
					.set(PlanOrder::getActiveStatus, planOrder.getActiveStatus())
					.in(PlanOrder::getId, idList)
					.update();
		}else{
			planOrderService.updateById(planOrder);
		}
		return Result.OK("审核成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "计划工单-下发任务-通过id删除")
	@Operation(summary="计划工单-下发任务-通过id删除")
	@RequiresPermissions("plan:mes3_plan_order:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		planOrderService.removeById(id);
		return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "计划工单-下发任务-批量删除")
	@Operation(summary="计划工单-下发任务-批量删除")
	@RequiresPermissions("plan:mes3_plan_order:deletionBatch")
	@DeleteMapping(value = "/deletionBatch")
	public Result<String> deletionBatch(@RequestParam(name="ids",required=true) String ids) {
		this.planOrderService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="计划工单-下发任务-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PlanOrder> queryById(@RequestParam(name="id",required=true) String id) {
		PlanOrder planOrder = planOrderService.getById(id);
		if(planOrder==null) {
			return Result.error(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode());
		}
		return Result.OK(planOrder);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param planOrder
    */
    @RequiresPermissions("plan:mes3_plan_order:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PlanOrder planOrder) {
        return super.exportXls(request, planOrder, PlanOrder.class, "计划工单-下发任务");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("plan:mes3_plan_order:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PlanOrder.class);
    }

	@Operation(summary="查询计划工单-统计")
	@PostMapping(value = "/planOrderStatistics")
	@AutoDict
	public Result<List<PlanOrderTotalDTO>> planOrderStatistics(@RequestBody PlanOrderRequestVO planOrder) {
		List<PlanOrderTotalDTO> list = planOrderService.planOrderTotal(planOrder);
		return Result.OK(list);
	}

	@Operation(summary="查询计划工单检查项-统计")
	@PostMapping(value = "/planOrderResultStatistics")
	@AutoDict
	public Result<List<PlanOrderResultTotalDTO>> planOrderResultStatistics(@RequestBody PlanOrderRequestVO planOrder) {
		List<PlanOrderResultTotalDTO> list = planOrderService.planOrderResultTotal(planOrder);
		return Result.OK(list);
	}
}
