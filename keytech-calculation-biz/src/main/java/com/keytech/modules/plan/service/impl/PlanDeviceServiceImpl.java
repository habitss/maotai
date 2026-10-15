package com.keytech.modules.plan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.*;
import com.keytech.modules.mdm3.service.*;
import com.keytech.modules.pi.common.InfluxDBClientHelper;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.common.QueryCondition;
import com.keytech.modules.pi.common.QueryTagCondition;
import com.keytech.modules.pi.dto.CompositeKey;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.modules.plan.entity.PlanOrder;
import com.keytech.modules.plan.entity.PlanOrderResultConfig;
import com.keytech.modules.plan.enums.PlanStatusEnum;
import com.keytech.modules.plan.enums.PlanTriggersEnum;
import com.keytech.modules.plan.mapper.PlanDeviceMapper;
import com.keytech.modules.plan.service.IPlanDeviceService;
import com.keytech.modules.plan.service.IPlanOrderResultConfigService;
import com.keytech.modules.plan.service.IPlanOrderService;
import com.keytech.modules.plan.service.IPlanService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * &#064;Description:  工单管理-机台计划
 * &#064;Author:  jeecg-boot
 * &#064;Date:    2025-06-06
 * &#064;Version:  V1.0
 */
@Slf4j
@Service
public class PlanDeviceServiceImpl extends BaseServiceImpl<PlanDeviceMapper, PlanDevice> implements IPlanDeviceService {
    @Autowired
    private IPlanService planService;
    @Autowired
    private IMdmTaskService mdmTaskService;
    @Autowired
    private IMdmTaskAutoTriggerConfigService mdmTaskAutoTriggerConfigService;
    @Autowired
    private IMdmTaskAutoTriggerRecordService mdmTaskAutoTriggerRecordService;
    @Autowired
    private IMdmTaskItemService mdmTaskItemService;
    @Autowired
    private IMdmItemService mdmItemService;
    @Autowired
    private IMdmPostDeviceService postDeviceService;
    @Autowired
    private IPlanOrderService planOrderService;
    @Autowired
    private IPlanOrderResultConfigService iPlanOrderResultConfigService;
    @Resource
    private InfluxDBClientHelper influxDBClientHelper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "planBackfillExecutor")
    private ExecutorService planBackfillExecutor;
    //运行中的状态
    List<Integer> doingStatuList = Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.DELAY.code);
    //多少分钟前
    int minutesAgoNum = 10;
    //sendByLastTime分布式锁key
    private static final String SEND_BY_LAST_TIME_LOCK_KEY = "plan:auto_trigger:send_by_last_time_lock";
    //分布式锁超时时间（分钟），防止死锁
    private static final long LOCK_TIMEOUT_MINUTES = 30;
    //补跑断点Redis key前缀，key格式: plan:auto_trigger:backfill:{taskId}，value为时间戳（毫秒）
    private static final String BACKFILL_PREFIX = "plan:auto_trigger:backfill:";

    @Override
    public void planJob(String paramJson) {
        //获取当前时间
        Date now = new Date();
        //获取任务参数
        JSONObject params = JSONUtil.parseObj(paramJson);
        //获取任务id
        String jobId = params.getStr("jobId");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        //文件路径
        String path = "/home/mes3/planLog/plan-" + sdf.format(now) + ".log";
        String content = "\n\n\n\n=======================================定时任务开始执行jobId----"+jobId+"\n\n";
        writePlanDataToFile(content, null, path);
        //交接班循环任务
//        cycleTask(jobId, now);
        //一小时任务触发
        if(PlanTriggersEnum.TASK_CORN_HOUR_1.name.equals(jobId)){
//            //根据首次推送时间点调用循环任务
//            cycleTaskByFirstPushTime(now);
            //根据推送json调用循环任务
            cycleTaskByPushJson(now);
        }
        //一小时和两小时任务触发
        if(PlanTriggersEnum.TASK_CORN_HOUR_1.name.equals(jobId)
                || PlanTriggersEnum.TASK_CORN_HOUR_2.name.equals(jobId)){
            //调用生产任务
            productionTask(jobId, now);
        }
    }

    @Override
    @Transactional
    public void delayClosPlanJob(String paramJson) {
        //获取当前时间
        Date now = new Date();
        //查询小于当前时间的延迟关闭计划
        List<Plan> planList = planService.lambdaQuery()
                .eq(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)
                .le(Plan::getDelayCloseTime, now).list();
        //没有延迟关闭数据终止程序
        if(CollUtil.isEmpty(planList)){
            return;
        }
        //获取计划id
        List<String> planIdList = planList.stream()
                .map(Plan::getId)
                .collect(Collectors.toList());
        //设置机台计划为关闭状态
        this.lambdaUpdate()
                .set(PlanDevice::getPlanStatus, PlanStatusEnum.FINISHED.code)
                .in(PlanDevice::getPlanId, planIdList)
                .update();
        //设置计划为关闭状态
        planService.lambdaUpdate()
                .set(Plan::getPlanStatus, PlanStatusEnum.FINISHED.code)
                .in(Plan::getId, planIdList)
                .update();

        //根据机台计划删除大于当前时间的任务
        planOrderService.lambdaUpdate()
                .set(PlanOrder::getDelFlag, 1)
                .in(PlanOrder::getPlanId, planIdList)
                .and(wrapper -> wrapper
                        .ge(PlanOrder::getRealStartTime, now)
                        .or(
                                //任务有可能是直接从暂停状态直接结束计划，暂停状态未删除的任务也要删除掉
                                w -> w
                                        .eq(PlanOrder::getDelFlag, 0)
                                        .eq(PlanOrder::getProductionStatus, "5")
                                        .le(PlanOrder::getPlanStartTime, now)
                        )
                )
                .update();
    }

    //自动触发任务
    @Override
    public void autoTriggerPlanJob(String paramJson, Date currentDate, LocalDateTime currentLocalDateTime) {
        //获取十分钟前的小时值 (24小时制)
        int hour = currentLocalDateTime.minusMinutes(minutesAgoNum).getHour();

        //获取当天的开始时间：00:00:00
        Date beginOfDay = DateUtil.beginOfDay(currentDate);

        //获取本周第一天时间（周一算第一天）
        Date beginOfWeek = DateUtil.beginOfWeek(DateUtil.date(), true);

        //异步提交历史补跑任务，不阻塞正常流程
//        CompletableFuture.runAsync(() -> sendByLastTime(currentDate), planBackfillExecutor)
//                .exceptionally(ex -> {
//                    log.error("自动触发任务-历史补跑异步执行异常", ex);
//                    return null;
//                });

        //获取当前时间范围的自动触发任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getTaskKindCode, 10)
                .le(MdmTask::getAutoTriggerRangeStart, hour)
                .ge(MdmTask::getAutoTriggerRangeEnd, hour)
                .and(taskQuery ->
                        taskQuery
                                //实时触发
                                .and(
                                        taskQuery1 ->
                                                taskQuery1.eq(MdmTask::getAutoTriggerFrequencyType, 1)
                                )
                                //自定义频率
                                .or(
                                        taskQuery1 ->
                                                taskQuery1.eq(MdmTask::getAutoTriggerFrequencyType, 0)
                                )
                                //每天触发一次，且未推送过的
                                .or(
                                        taskQuery1 ->
                                                taskQuery1.eq(MdmTask::getAutoTriggerFrequencyType, 10)
                                                        .and(
                                                                taskQuery2 ->
                                                                        taskQuery2.notExists("select 1 from mes3_plan_order a where mes3_mdm_task.id = a.task_id and a.create_time >= '"+beginOfDay+"'")
                                                                                .or()
                                                                                .isNull(MdmTask::getAutoTriggerLastTime)
                                                        )
                                )
                                //每周触发一次，且未推送过的
                                .or(
                                        taskQuery1 ->
                                                taskQuery1.eq(MdmTask::getAutoTriggerFrequencyType, 20)
                                                        .and(
                                                                taskQuery2 ->
                                                                        taskQuery2.notExists("select 1 from mes3_plan_order a where mes3_mdm_task.id = a.task_id and a.create_time >= '"+beginOfWeek+"'")
                                                                                .or()
                                                                                .isNull(MdmTask::getAutoTriggerLastTime)
                                                        )
                                )
                )
                .list();

        //修改任务的最后触发时间
        mdmTaskService.lambdaUpdate()
                .set(MdmTask::getAutoTriggerLastTime, currentDate)
                .eq(MdmTask::getTaskKindCode, 10)
                .eq(MdmTask::getState, 1)
                .update();
        //没有任务停止后续操作
        if(CollUtil.isEmpty(taskList)){
            log.error("自动触发任务工单生成----没有自动触发任务");
            return;
        }
        Map<String, Object> resultMap = getConfigAndRecord(taskList);
        //获取具备触发条件的任务
        List<MdmTask> newTaskList = getAutoTriggerTaskList(taskList, currentLocalDateTime, currentDate,
                (Map<String, MdmTaskAutoTriggerConfig>) resultMap.get("configMap"),
                (Map<String, MdmTaskAutoTriggerRecord>) resultMap.get("recordMap")
        );
        //没有达到触发条件的任务停止后续操作
        if(CollUtil.isEmpty(newTaskList)){
            log.error("自动触发任务工单生成----没有具备触发条件的任务2");
            return;
        }
        //获取所有设备id
        Set<Integer> deviceIds = newTaskList.stream()
                .map(MdmTask::getDeviceId)
                .collect(Collectors.toSet());
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, newTaskList, deviceIds, null, currentDate, null);
    }

    @Override
    public void startPlanDevice(Plan plan, Date now) {
        //查找机台计划
        List<PlanDevice> planDeviceList = this.lambdaQuery()
                .eq(PlanDevice::getPlanId, plan.getId())
                .eq(PlanDevice::getPlanStatus, PlanStatusEnum.TODO.code)
                .in(StrUtil.isNotBlank(plan.getDeviceIds()), PlanDevice::getDeviceId, Arrays.asList(plan.getDeviceIds().split(",")))
                .list();
        //没有未开始的机台计划，终止操作
        if(CollUtil.isEmpty(planDeviceList)){
            return;
        }

        //获取并去除重复的设备id
        Set<Integer> uniqueDeviceIds = planDeviceList.stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toSet());

        //根据设备id获取指定类型的任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .in(MdmTask::getDeviceId, uniqueDeviceIds)
                .eq(MdmTask::getRecipeTypeCode, plan.getPlanType())
                .eq(null != plan.getPlanTypeSub(), MdmTask::getRecipeSubTypeCode, plan.getPlanTypeSub())
                .list();

        //未配置任务则停止操作
        if(CollUtil.isEmpty(taskList)){
            throw new JeecgBootException("该计划所关联的机台都未配置对应触发任务，无法开启计划！");
        }

        //将机台计划转为map，设备id为key
        Map<Integer, PlanDevice> planDeviceMap = planDeviceList.stream()
                .collect(Collectors.toMap(
                        PlanDevice::getDeviceId,
                        device -> device
                ));

        //调用创建工单方法
        createOrder(PlanStatusEnum.TASK_PRODUCTION.code, taskList, uniqueDeviceIds, planDeviceMap, now, null);
        //如果是开班任务，还需要查询例行任务不等于1小时、2小时的任务
        if(plan.getPlanType() == 1){
            String[] loopTriggerIds = {PlanTriggersEnum.TASK_CORN_HOUR_4.name, PlanTriggersEnum.TASK_CORN_HOUR_6.name,PlanTriggersEnum.TASK_CORN_HOUR_8.name,
                    PlanTriggersEnum.TASK_CORN_HOUR_10.name,PlanTriggersEnum.TASK_CORN_HOUR_12.name,
                    PlanTriggersEnum.TASK_CORN_HOUR_24.name};

            taskList = mdmTaskService.lambdaQuery()
                    .eq(MdmTask::getState, 1)
                    .in(MdmTask::getDeviceId, uniqueDeviceIds)
                    .in(MdmTask::getLoopTriggerId, (Object[]) loopTriggerIds)
                    .list();
            //调用创建工单方法
            createOrder(PlanStatusEnum.TASK_PRODUCTION.code, taskList, uniqueDeviceIds, planDeviceMap, now, null);
        }

        //设置机台计划状态为进行中
        this.lambdaUpdate()
                .set(PlanDevice::getPlanStatus, PlanStatusEnum.DOING.code)
                .set(PlanDevice::getRealStartTime, now)
                .set(PlanDevice::getRecoveryTime, now)
                .eq(PlanDevice::getPlanId, plan.getId())
                .in(StrUtil.isNotBlank(plan.getDeviceIds()), PlanDevice::getDeviceId, Arrays.asList(plan.getDeviceIds().split(",")))
                .update();
    }

    @Override
    public void takeUpPostJob(TakeUpPost takeUpPost, List<MdmTask> taskList, Date now) {
        //查询正在进行中的开班计划
        List<PlanDevice> planDeviceList = this.lambdaQuery()
                .exists(
                        "select 1 from mes3_plan as plan " +
                                " where plan.del_flag = 0 and plan.id = plan_id " +
                                " and plan.plan_status = "+ PlanStatusEnum.DOING.code+
                                " and plan.plan_type = 1 "
                ).list();
        Map<Integer, List<PlanDevice>> groupedByDeviceId = planDeviceList.stream()
                .collect(Collectors.groupingBy(PlanDevice::getDeviceId));

        Set<Integer> deviceIds = new HashSet<>();
        List<MdmTask> newTaskList = new ArrayList<>();
        //获取需要触发的任务
        for(MdmTask task : taskList){
            //判断当前需要触发的任务机台是否处于开班
            task.setIsStart(groupedByDeviceId.containsKey(task.getDeviceId()));
            //是否为交接班
            if(2 == task.getCheckTaskType()){
                //设置接班
                task.setSpecialType("0");
                newTaskList.add(task);
                //设置交班
                MdmTask tempTask = new MdmTask();
                BeanUtil.copyProperties(task, tempTask);
                tempTask.setSpecialType("1");
                newTaskList.add(tempTask);
            }else{
                newTaskList.add(task);
            }
            deviceIds.add(task.getDeviceId());
        }
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, newTaskList, deviceIds, null, now, takeUpPost);
    }

    @Override
    @Transactional
    public void delayEndPlanDevice(String planId, String deviceIds, Date realEndTime, Date delayTime) {
        List<PlanDevice> planDeviceList = getPlanDeviceList(planId, deviceIds);
        if(CollUtil.isEmpty(planDeviceList)){
            return;
        }
        //发送收班计划
        sendEndTask(planDeviceList, realEndTime);
        //设置机台计划为延迟关闭状态
        this.lambdaUpdate()
                .set(PlanDevice::getPlanStatus, PlanStatusEnum.DELAY.code)
                .set(PlanDevice::getRealEndTime, realEndTime)
                .set(PlanDevice::getDelayCloseTime, delayTime)
                .eq(PlanDevice::getPlanId, planId)
                .in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.PAUSE.code))
                .update();
        //设置计划为延迟关闭状态
        planService.lambdaUpdate()
                .set(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)
                .set(Plan::getRealEndTime, realEndTime)
                .set(Plan::getDelayCloseTime, delayTime)
                .in(Plan::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.PAUSE.code))
                .eq(Plan::getId, planId)
                .update();
    }

    @Override
    @Transactional
    public void endPlanDevice(String planId, String deviceIds, Date realEndTime, Boolean isDelayClose) {
        List<PlanDevice> planDeviceList = getPlanDeviceList(planId, deviceIds);
        if(CollUtil.isEmpty(planDeviceList)){
            return;
        }
        if(CollUtil.isNotEmpty(planDeviceList)){
            //彻底删除未开始计划，用于清理非1小时和2小时的例行任务
//            planOrderService.removeTodoOrder(planId, planDeviceList.stream().map(PlanDevice::getId).collect(Collectors.toList()), new Date());
            //按照修改的结束时间删除，不会彻底删除数据
            planOrderService.lambdaUpdate()
                    .set(PlanOrder::getDelFlag, 1)
                    .eq(PlanOrder::getPlanId, planId)
                    .in(PlanOrder::getPlanDeviceId, planDeviceList.stream().map(PlanDevice::getId).collect(Collectors.toList()))
                    .and(wrapper -> wrapper
                            .ge(PlanOrder::getRealStartTime, realEndTime)
                            .or(
                                    //任务有可能是直接从暂停状态直接结束计划，暂停状态未删除的任务也要删除掉
                                    w -> w
                                            .eq(PlanOrder::getDelFlag, 0)
                                            .eq(PlanOrder::getProductionStatus, "5")
                                            .le(PlanOrder::getPlanStartTime, realEndTime)
                            )
                    )
                    .update();
        }

        //查询是否开班计划，如果是开班计划触发收班任务
        List<Plan> planList = planService.lambdaQuery()
                .eq(Plan::getId, planId)
                .eq(Plan::getPlanType, 1)
                .list();
        if(CollUtil.isNotEmpty(planList)){
            sendEndTask(planDeviceList, realEndTime);
        }
        //关闭进行中的机台计划
        this.lambdaUpdate()
                .set(PlanDevice::getPlanStatus, PlanStatusEnum.FINISHED.code)
                .set(!isDelayClose, PlanDevice::getRealEndTime, realEndTime)
                .eq(PlanDevice::getPlanId, planId)
                .in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.PAUSE.code, PlanStatusEnum.DELAY.code))
                .update();
        //调用主计划关闭方法
        planService.endPLan(planId, realEndTime, isDelayClose);
    }

    @Override
    public void pauseOrRenewPlanDevice(Plan plan, Integer status, Date now) {
        List<PlanDevice> planDeviceList =this.lambdaQuery()
                .eq(PlanDevice::getPlanId, plan.getId())
                .list();

        //获取所有设备id
        Set<Integer> deviceIds = planDeviceList.stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toSet());

        //将机台计划转为map，设备id为key
        Map<Integer, PlanDevice> planDeviceMap = planDeviceList.stream()
                .collect(Collectors.toMap(
                        PlanDevice::getDeviceId,
                        device -> device
                ));

        int code = 0;
        //恢复计划任务
        if(status == PlanStatusEnum.DOING.code){
            code = 6;
            //删除暂停期间的工单任务
            planOrderService.removePauseOrder(plan.getId(), planDeviceList.stream()
                    .map(PlanDevice::getId)
                    .collect(Collectors.toList()), now);
            //恢复剩余工单任务
            planOrderService.lambdaUpdate()
                    .set(PlanOrder::getProductionStatus, PlanStatusEnum.DOING.code)
                    .eq(PlanOrder::getPlanId, plan.getId())
                    .gt(PlanOrder::getPlanStartTime, now)
                    .update();
        }
        //暂停计划任务
        else if(status == PlanStatusEnum.PAUSE.code){
            code = 5;
            //暂停工单任务
            planOrderService.lambdaUpdate()
                    .set(PlanOrder::getProductionStatus, PlanStatusEnum.PAUSE.code)
                    .eq(PlanOrder::getPlanId, plan.getId())
                    .gt(PlanOrder::getPlanStartTime, now)
                    .update();
        }

        //获取暂停或恢复任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getRecipeTypeCode, code)
                .in(MdmTask::getDeviceId, deviceIds)
                .list();
        if(CollUtil.isNotEmpty(taskList)){
            //调用生成工单方法
            this.createOrder(PlanStatusEnum.TASK_PRODUCTION.code, taskList, deviceIds, planDeviceMap, now, null);
        }
    }

    @Override
    public void sendManualTrigger(List<MdmTask> taskList, Date now) {
        //获取所有设备id
        Set<Integer> deviceIds = taskList.stream()
                .map(MdmTask::getDeviceId)
                .collect(Collectors.toSet());
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, taskList, deviceIds, null, now, null);
    }

    @Override
    public String planOrderTest(String id, String startTime) {
        MdmTask task = mdmTaskService.getById(id);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parse(startTime, "yyyy-MM-dd HH:mm:ss"));
        LocalDate currentDate = LocalDate.from(DateUtil.parseLocalDateTime(startTime, "yyyy-MM-dd HH:mm:ss"));
        JSONObject jsonObject = JSONUtil.parseObj(task.getPushJson());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(isInInterval(hour, task.getFirstPushTime(), jsonObject.getStr("pushDate_hour"))){
            return "触发了";
        }
        return "没触发";
    }

    //交接班循环任务
    void cycleTask(String jobId, Date now){
        //交接班循环任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getLoopTriggerId, jobId)
                .eq(MdmTask::getTaskKindCode, PlanStatusEnum.TASK_CYCLE.code)
                .ne(MdmTask::getCheckTaskType, 2)
                .isNull(MdmTask::getFirstPushTime)
                .isNull(MdmTask::getPushJson)
                .list();
        //没有任务终止操作
        if(CollUtil.isEmpty(taskList)){
            return;
        }
        //获取所有设备id
        Set<Integer> deviceIds = taskList.stream()
                .map(MdmTask::getDeviceId)
                .collect(Collectors.toSet());
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, taskList, deviceIds, null, now, null);
    }

    //生产任务
    void productionTask(String jobId, Date now){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        //文件路径
        String path = "/home/mes3/planLog/plan-" + sdf.format(now) + ".log";
        String content = "\n开始执行生产任务----时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)
                +"\njobid："+jobId
                +"\n55分钟前时间："+sdf.format(subtractMinutes(now, 55))
                +"\n115分钟前时间："+sdf.format(subtractMinutes(now, 115))
                +"\n";

        String statusStr = doingStatuList.stream()
                .map(String::valueOf) // 将Integer转为String
                .collect(Collectors.joining(","));
        //查询正在进行中的开班机台计划
        List<PlanDevice> planDeviceList = this.lambdaQuery()
                .exists(    //判断是否为正在进行中或者延迟取消的开班计划
                        "select 1 from mes3_plan as plan " +
                                " where plan.del_flag = 0 and plan.id = plan_id " +
                                " and plan.plan_status in ("+ statusStr+")"+
                                " and plan.plan_type = 1 "
                )
                .and(queryWrapper1 -> queryWrapper1
                        .and(queryWrapper2 -> queryWrapper2
                                //判断是否为前段设备，且实际开始时间>=当前时间55分钟前的数据
                                .exists(
                                        "select 1 from mes3_mdm_device as device " +
                                                " where device.del_flag = 0 and device.id = device_id " +
                                                " and device.is_after = 0 "
                                )
                                .le(PlanDevice::getRecoveryTime, subtractMinutes(now, 55))
                        ).or()
                        //判断时间开始时间>=当前时间115分钟前的数据
                        .le(PlanDevice::getRecoveryTime, subtractMinutes(now, 115))
                )
                .list();
        writePlanDataToFile(content+"\n\n机台列表：", planDeviceList, path);
        if(CollUtil.isEmpty(planDeviceList)){
            return;
        }
        //获取所有设备id
        Set<Integer> deviceIds = planDeviceList.stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toSet());

        //将机台计划转为map，设备id为key
        Map<Integer, PlanDevice> planDeviceMap = planDeviceList.stream()
                .collect(Collectors.toMap(
                        PlanDevice::getDeviceId,
                        device -> device,
                        (existing, replacement) -> existing
                ));

        //获取例行任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getLoopTriggerId, jobId)
                .eq(MdmTask::getRecipeTypeCode, 4)
                .eq(MdmTask::getTaskKindCode, PlanStatusEnum.TASK_PRODUCTION.code)
                .in(MdmTask::getDeviceId, deviceIds)
                .list();
        writePlanDataToFile("\n任务列表：", taskList, path);
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_PRODUCTION.code, taskList, deviceIds, planDeviceMap, now, null);
        writePlanDataToFile("\n结束："+jobId, null, path);
    }

    /**
     * 创建工单
     * @param createType 创建类型
     * @param taskList 任务数据
     * @param deviceIds 设备id集合
     * @param planDeviceMap 机台计划集合
     * @param now 当前时间
     * @param takeUpPost 交接班任务
     */
    private void createOrder(int createType, List<MdmTask> taskList, Set<Integer> deviceIds,
                             Map<Integer, PlanDevice> planDeviceMap, Date now,
                             TakeUpPost takeUpPost
    ){
        if(CollUtil.isEmpty(taskList)){
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        //获取所有任务id
        List<String> taskIds = taskList.stream()
                .map(MdmTask::getId)
                .collect(Collectors.toList());

        //获取任务关联条目
        List<MdmTaskItem> mdmTaskItemList = mdmTaskItemService.lambdaQuery()
                .in(MdmTaskItem::getTaskId, taskIds)
                .list();
        if(CollUtil.isEmpty(mdmTaskItemList)){
            log.error("任务创建----没有任务关联条目");
            return;
        }
        //将条目按taskId分组
        Map<String, List<MdmTaskItem>> groupedItemByTaskId = mdmTaskItemList.stream()
                .collect(Collectors.groupingBy(MdmTaskItem::getTaskId));
        //获取条目详细信息
        List<MdmItem> itemList = mdmItemService.lambdaQuery()
                .in(MdmItem::getId, mdmTaskItemList.stream()
                        .map(MdmTaskItem::getItemId)
                        .collect(Collectors.toList()))
                .list();
        //将条目转换成map
        Map<String, MdmItem> itemMap = itemList.stream()
                .collect(Collectors.toMap(
                        MdmItem::getId,
                        item -> item,
                        (existing, replacement) -> existing // 如果有重复 key，保留第一个
                ));
        //获取所有设备关联岗位
        Map<Integer, Integer> devicePostMap = new HashMap<>();
        if(null == takeUpPost){
            //获取岗位关联设备
            List<MdmPostDevice> mdmPostDeviceList = postDeviceService.lambdaQuery()
                    .in(MdmPostDevice::getDeviceId, deviceIds)
                    .list();
            if(CollUtil.isEmpty(mdmPostDeviceList)){
                log.error("任务创建----没有岗位设备关联数据");
                return;
            }
            //将岗位关联设备转换成map，设备id为key
            devicePostMap = mdmPostDeviceList.stream()
                    .collect(Collectors.toMap(
                            MdmPostDevice::getDeviceId,
                            MdmPostDevice::getPostId,
                            (existing, replacement) -> replacement  // 冲突时保留新的
                    ));
        } else {
            devicePostMap = takeUpPost.getDevicePostMap();
        }
        //需要保存的工单和工单条目
        List<PlanOrder> planOrderList = new ArrayList<>();
        //工单条目历史配置数据集合
        List<PlanOrderResultConfig> planOrderResultConfigList = new ArrayList<>();
        //生成工单
        for(MdmTask mdmTask : taskList){
            //设备、岗位、条目不能为空
            if(null == mdmTask.getDeviceId() || !devicePostMap.containsKey(mdmTask.getDeviceId())
                    || !groupedItemByTaskId.containsKey(mdmTask.getId())){
                continue;
            }
            //生成一个历史配置id
            String resultConfigId = IdUtil.getSnowflakeNextIdStr();
            PlanDevice planDevice = null;
            //拼接工单数据
            PlanOrder planOrder = new PlanOrder();
            BeanUtil.copyProperties(mdmTask, planOrder,"id", "createTime", "createBy", "updateTime", "updateBy", "remark");
            planOrder.setTaskId(mdmTask.getId());
            planOrder.setId(IdUtil.getSnowflakeNextIdStr());
            planOrder.setPostId(devicePostMap.get(mdmTask.getDeviceId()).toString());
            planOrder.setPlanStartTime(now);
            planOrder.setRealStartTime(now);
            planOrder.setRecipeTypeCode(mdmTask.getRecipeTypeCode());
            planOrder.setRecipeSubTypeCode(mdmTask.getRecipeSubTypeCode());
            planOrder.setResultConfigId(resultConfigId);
            planOrder.setAutoTriggerCalcTime(mdmTask.getAutoTriggerCalcTime());
            if(null != planDeviceMap && planDeviceMap.containsKey(mdmTask.getDeviceId())){
                planDevice = planDeviceMap.get(mdmTask.getDeviceId());
                planOrder.setPlanId(planDevice.getPlanId());
                planOrder.setPlanDeviceId(planDevice.getId());
            }
            if(null != takeUpPost){
                planOrder.setTeam(takeUpPost.getTeam());
                planOrder.setShiftNum(takeUpPost.getShiftId());
            }

            if(groupedItemByTaskId.containsKey(mdmTask.getId())){
                //生成工单条目
                for(MdmTaskItem taskItem : groupedItemByTaskId.get(mdmTask.getId())){
                    MdmItem tempMdmItem = itemMap.get(taskItem.getItemId().toString());
                    //是否交接班任务
                    if(2 == mdmTask.getCheckTaskType()){
                        //如果任务机台不是开班状态，但是条目是生产状态才下发，那么跳过当前条目配置
                        if(null != mdmTask.getIsStart() && !mdmTask.getIsStart() && null != tempMdmItem.getIsProduction() && 1 == tempMdmItem.getIsProduction()){
                            continue;
                        }
                        //如果不是对应的特殊触发类型跳过当前条目配置
                        if(!Objects.equals(tempMdmItem.getIsHandover(), Integer.valueOf(mdmTask.getSpecialType()))){
                            continue;
                        }
                    }

                    //拼接工单条目历史配置数据
                    PlanOrderResultConfig planOrderResultConfig = new PlanOrderResultConfig();
                    BeanUtil.copyProperties(tempMdmItem, planOrderResultConfig, "id", "create_time","create_by","update_time","update_by");
                    planOrderResultConfig.setTaskId(taskItem.getTaskId());
                    planOrderResultConfig.setItemId(taskItem.getItemId());
                    planOrderResultConfig.setResultConfigId(resultConfigId);
                    planOrderResultConfig.setSort(taskItem.getSort());
                    planOrderResultConfigList.add(planOrderResultConfig);
                }
            }

            if(CollUtil.isEmpty(planOrderResultConfigList)){
                continue;
            }

            //判断是否为生产任务
            if(createType == PlanStatusEnum.TASK_PRODUCTION.code && null != planDevice && null != mdmTask.getLoopTriggerId()
                    && !PlanTriggersEnum.TASK_CORN_HOUR_1.name.equals(mdmTask.getLoopTriggerId().toString())
                    && !PlanTriggersEnum.TASK_CORN_HOUR_2.name.equals(mdmTask.getLoopTriggerId().toString())){
                //判断生产任务是否为8、10、12、24小时任务
                processingProductionList(planOrder, planOrderList,
                        mdmTask.getLoopTriggerId(), planDevice.getPlanStartTime(), planDevice.getPlanEndTime());
            } else {
                //添加到工单集合
                planOrderList.add(planOrder);
            }
            if(planOrderList.size() >= 200){
                try {
                    planOrderService.saveBatch(planOrderList);
                } catch (Exception e) {
                    log.error("createOrder--循环中工单保存异常--：大小：{}, 数据：{}",
                            planOrderList.size(),
                            JSONUtil.toJsonStr(planOrderList),
                            e
                    );
                    throw new RuntimeException(e);
                }
                try {
                    iPlanOrderResultConfigService.saveBatch(planOrderResultConfigList);
                } catch (Exception e) {
                    log.error("createOrder--循环中配置保存异常--：大小：{}, 数据：{}",
                            planOrderResultConfigList.size(),
                            JSONUtil.toJsonStr(planOrderResultConfigList),
                            e
                    );
                    throw new RuntimeException(e);
                }
                planOrderList.clear();
                planOrderResultConfigList.clear();
            }
        }
        //将循环中未满足条件的数据补充插入
        if(CollUtil.isNotEmpty(planOrderList)){
            try {
                planOrderService.saveBatch(planOrderList);
            } catch (Exception e) {
                log.error("createOrder--工单保存异常--：大小：{}, 数据：{}",
                        planOrderList.size(),
                        JSONUtil.toJsonStr(planOrderList),
                        e
                );
                throw new RuntimeException(e);
            }
            try {
                iPlanOrderResultConfigService.saveBatch(planOrderResultConfigList);
            } catch (Exception e) {
                log.error("createOrder--配置保存异常--：大小：{}, 数据：{}",
                        planOrderResultConfigList.size(),
                        JSONUtil.toJsonStr(planOrderResultConfigList),
                        e
                );
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 添加生产任务工单
     * @param planOrder 原始工单
     * @param planOrderList 工单集合
     * @param loopTriggerId 触发器id
     * @param startDate 计划开始时间
     * @param endDate 计划结束时间
     */
    private void processingProductionList(PlanOrder planOrder, List<PlanOrder> planOrderList, int loopTriggerId, Date startDate, Date endDate){
        long baseHour = 48;
        //计算计划开始时间和结束时间相差的小时数据，默认多发送48小时数据
        long hour = DateUtil.between(startDate, endDate, DateUnit.HOUR) + baseHour;
        if(hour < baseHour){
            hour = baseHour;
        }
        //计划开始时间
        Date startTime = planOrder.getPlanStartTime();
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 加上一个小时并调整到下一个整点
        LocalDateTime nextHourStart = localDateTime.plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        startTime = Date.from(nextHourStart.atZone(ZoneId.systemDefault()).toInstant());

        //根据触发器id获取频次
        int frequency = PlanTriggersEnum.getCodeByName(loopTriggerId+"");
        //计算需要循环的次数
        int times = (int) (hour / frequency);
        //循环添加触发工单
        for(int i = 0; i <= times; i++){
            //根据当前循环次数*频次*60分钟，获取计划开始时间
            Date planStartDate = addMinutesToDate(startTime, i * frequency * 60);
//            Date planEndDate = addMinutesToDate(planOrder.getPlanEndTime(), i * frequency * 60);
            //复制原始工单数据往工单集合中添加数据
            PlanOrder newPlanOrder = new PlanOrder();
            BeanUtil.copyProperties(planOrder, newPlanOrder, "id");
            newPlanOrder.setId(IdUtil.getSnowflakeNextIdStr());
            newPlanOrder.setPlanStartTime(planStartDate);
            newPlanOrder.setRealStartTime(planStartDate);
//            newPlanOrder.setPlanEndTime(planEndDate);
            planOrderList.add(newPlanOrder);
        }
    }

    /**
     * 获取指定分钟后的时间
     * @param date 时间
     * @param minutesToAdd 需要增加的分钟数
     */
    private Date addMinutesToDate(Date date, int minutesToAdd) {
        // 1. 将 Date 转换为 LocalDateTime
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 2. 增加指定分钟数
        LocalDateTime newDateTime = localDateTime.plusMinutes(minutesToAdd);

        // 3. 将 LocalDateTime 转换回 Date
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    //减去指定分钟后的时间
    private Date subtractMinutes(Date date, int minutes) {
        return new Date(date.getTime() - ((long) minutes) * 60 * 1000);
    }

    //根据首次推送时间推送得循环任务
    void cycleTaskByFirstPushTime(Date now){
        //获取首次推送时间不为空的循环任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getTaskKindCode, PlanStatusEnum.TASK_CYCLE.code)
                .ne(MdmTask::getCheckTaskType, 2)
                .isNotNull(MdmTask::getFirstPushTime)
                .list();
        //没有任务终止操作
        if(CollUtil.isEmpty(taskList)){
            return;
        }
        //需要触发的机台设备
        Set<Integer> deviceIds = new HashSet<>();
        //需要触发的任务
        List<MdmTask> newTaskList = new ArrayList<>();
        //获取需要触发的任务
        for(MdmTask task : taskList){
            //获取当天的开始时间（00:00:00）
            Date beginOfDay = DateUtil.beginOfDay(now);
            //在当天的 00:00:00 基础上加上指定小时得到首次推送时间
            Date firstPushTime = DateUtil.offsetHour(beginOfDay, Integer.parseInt(task.getFirstPushTime()));
            //计算相差多少小时
            long hours = DateUtil.between(now, firstPushTime, DateUnit.HOUR);
            //获取循环触发小时
            int frequency = PlanTriggersEnum.getCodeByName(task.getLoopTriggerId()+"");
            //计算是否触发
            if(Math.toIntExact(hours % frequency) == 0){
                deviceIds.add(task.getDeviceId());
                newTaskList.add(task);
            }
        }

        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, newTaskList, deviceIds, null, now, null);
    }

    //根据推送配置推送的循环任务
    void cycleTaskByPushJson(Date now){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        //文件路径
        String path = "/home/mes3/planLog/plan-cycle-" + sdf.format(now) + ".log";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        //获取推送配置不为空的循环任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getTaskKindCode, PlanStatusEnum.TASK_CYCLE.code)
                .isNotNull(MdmTask::getPushJson)
                .and(q ->
                        q.eq(MdmTask::getFirstPushTime, calendar.get(Calendar.HOUR_OF_DAY))
                        .or()
                        .eq(MdmTask::getLoopTriggerId, 55)
                )
                .list();
        //没有任务终止操作
        if(CollUtil.isEmpty(taskList)){
            return;
        }
        LocalDate currentDate = LocalDate.now();
        //获取日
        int dayOfMonth = currentDate.getDayOfMonth();
        //获取星期的数字1 (周一) 到 7 (周日)
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        int dayNumber = dayOfWeek.getValue();
        //获取“月日”（MM-dd 格式，例如 08-25）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        String monthDay = currentDate.format(formatter);
        //获取当前小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //获取需要推送的任务数据
        List<MdmTask> newTaskList = new ArrayList<>();
        for(MdmTask task : taskList){
            if(task.getLoopTriggerId() == 56){
                newTaskList.add(task);
            }else{
                JSONObject jsonObject = JSONUtil.parseObj(task.getPushJson());
                if(task.getLoopTriggerId() == 55){
                    //小时任务
                    if(isInInterval(hour, task.getFirstPushTime(), jsonObject.getStr("pushDate_hour"))){
                        newTaskList.add(task);
                    }
                }else{
                    //如果是月推送，判断日期是否等于今天
                    if(null != jsonObject.getInt("pushDate_day") ){
                        List<String> days = Arrays.asList(jsonObject.getStr("pushDate_day").split( ","));
                        if (days.contains(dayOfMonth + "")) {
                            newTaskList.add(task);
                        }
                    } else if(null != jsonObject.getInt("pushDate_hour") ){
                        List<String> days = Arrays.asList(jsonObject.getStr("pushDate_day").split( ","));
                        if (days.contains(dayOfMonth + "")) {
                            newTaskList.add(task);
                        }
                    } else {
                        for (Map.Entry<String, Object> entry : jsonObject) {
                            if(entry.getKey().equals("pushDate_week") && entry.getValue().toString().contains(dayNumber + "")){
                                newTaskList.add(task);
                                break;
                            }else if(monthDay.equals(entry.getValue())){
                                newTaskList.add(task);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(CollUtil.isEmpty(newTaskList)){
            return;
        }
        //获取所有设备id
        Set<Integer> deviceIds = newTaskList.stream()
                .map(MdmTask::getDeviceId)
                .collect(Collectors.toSet());
        String content = "\n\n\n\n=======================================cycleTaskByPushJson推送的循环任务数据大小----\n\n" +
                "\ncycleTaskByPushJson大小："+newTaskList.size();
        writePlanDataToFile(content, newTaskList, path);
        //调用生成工单方法
        this.createOrder(PlanStatusEnum.TASK_CYCLE.code, newTaskList, deviceIds, null, now, null);
    }

    private List<PlanDevice> getPlanDeviceList(String planId, String deviceIds){
        if(StrUtil.isBlank(deviceIds)){
            return null;
        }
        //拆分设备id
        List<String> deviceIdList = Arrays.asList(deviceIds.split(","));
        //查找机台计划
        return this.lambdaQuery()
                .eq(PlanDevice::getPlanId, planId)
                .in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.PAUSE.code, PlanStatusEnum.DELAY.code))
                .in(PlanDevice::getDeviceId, deviceIdList)
                .list();
    }

    public void sendEndTask(List<PlanDevice> planDeviceList, Date realEndTime){
        //获取并去除重复的设备id
        Set<Integer> uniqueDeviceIds = planDeviceList.stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toSet());

        //将机台计划转为map，设备id为key
        Map<Integer, PlanDevice> planDeviceMap = planDeviceList.stream()
                .collect(Collectors.toMap(
                        PlanDevice::getDeviceId,
                        device -> device
                ));

        //获取收班任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getRecipeTypeCode, 3)
                .in(MdmTask::getDeviceId, uniqueDeviceIds)
                .list();
        if(CollUtil.isNotEmpty(taskList)){
            //调用生成工单方法
            this.createOrder(PlanStatusEnum.TASK_PRODUCTION.code, taskList, uniqueDeviceIds, planDeviceMap, realEndTime, null);
        }
    }

    /**
     * 单独写入日志
     */
    public static void writePlanDataToFile(String content, List<?> list, String filePath) {
//        Path path = Paths.get(filePath);
//        Path parentDir = path.getParent();
//        try {
//            // 检查目录是否存在，如果不存在则创建
//            if (parentDir != null && !Files.exists(parentDir)) {
//                Files.createDirectories(parentDir);
//            }
//            try (FileWriter fileWriter = new FileWriter(filePath,true)) {
//                fileWriter.write(content + "\n");
//                if(CollUtil.isNotEmpty(list)){
//                    for (Object data : list) {
//                        // 假设 MeasurementData 类有一个 toString 方法，可以将其内容转换为字符串
//                        fileWriter.write(data.toString() + "\n\n");
//                    }
//                }
//            }
//        }catch (IOException e) {
//            log.error("计划日志写入失败: {}", filePath, e);
//        }
    }

    Map<String, Object> getConfigAndRecord(List<MdmTask> taskList){
        Map<String, Object> resultMap = new HashMap<>();
        List<String> allTaskIdList = taskList.stream()
                .map(MdmTask::getId)
                .toList();
        //获取任务配置
        List<MdmTaskAutoTriggerConfig> configList = mdmTaskAutoTriggerConfigService.lambdaQuery()
                .in(MdmTaskAutoTriggerConfig::getTaskId, allTaskIdList)
                .isNotNull(MdmTaskAutoTriggerConfig::getTriggerPoint)
                .list();
        Map<String, MdmTaskAutoTriggerConfig> configMap = configList.stream()
                .collect(Collectors.toMap(
                        //组合key
                        config -> config.getTaskId() + "_" + config.getTriggerConfigType(),
                        config -> config,
                        //如果key冲突，保留第一个
                        (existing, replacement) -> existing
                ));
        //获取任务记录
        List<MdmTaskAutoTriggerRecord> recordList = mdmTaskAutoTriggerRecordService.lambdaQuery()
                .in(MdmTaskAutoTriggerRecord::getTaskId, allTaskIdList)
                .list();
        Map<String, MdmTaskAutoTriggerRecord> recordMap = recordList.stream()
                .collect(Collectors.toMap(
                        MdmTaskAutoTriggerRecord::getTaskId,
                        record -> record,
                        //如果key冲突，保留第一个
                        (existing, replacement) -> existing
                ));
        resultMap.put("configList", configList);
        resultMap.put("configMap", configMap);
        resultMap.put("recordList", recordList);
        resultMap.put("recordMap", recordMap);
        return resultMap;
    }

    /**
     * --story=1057425 --user=孙金伟 27-APP任务改造，新增糖化过程质量类别任务 https://www.tapd.cn/63554897/s/1395081 单元测试
     * 获取符合的自动触发任务
     * @param taskList 配置的任务
     * @param currentLocalDateTime LocalDateTime类型当前时间
     * @param currentDate Date类型当前时间
     * @return 符合的任务集合
     */
    List<MdmTask> getAutoTriggerTaskList(List<MdmTask> taskList,
                                         LocalDateTime currentLocalDateTime, Date currentDate,
                                         Map<String, MdmTaskAutoTriggerConfig> configMap, Map<String, MdmTaskAutoTriggerRecord> recordMap
    ){
        //只配置过开始步骤的任务
        List<MdmTask> startConfigTaskList = new ArrayList<>();
        //配置过开始步骤和结束步骤的任务
        List<MdmTask> allConfigTaskList = new ArrayList<>();
        taskList.forEach(task -> {
            //判断是否只配置过开始步骤
            if(1 == task.getAutoTriggerConfig()){
                MdmTaskAutoTriggerRecord mdmTaskAutoTriggerRecord = recordMap.get(task.getId());
                //判断是否为自定义频率，且发送过一次数据
                if(0 == task.getAutoTriggerFrequencyType() && null != mdmTaskAutoTriggerRecord){
                    //计算自定义频率的最后发送时间和当前时间是否满足间隔条件
                    long frequencyHour = Duration.between(mdmTaskAutoTriggerRecord.getStartTime(), currentLocalDateTime).toHours();
                    if(frequencyHour >= task.getAutoTriggerFrequencyValue()){
                        startConfigTaskList.add(task);
                    }
                }else{
                    startConfigTaskList.add(task);
                }
            }else{
                allConfigTaskList.add(task);
            }
        });

        List<MdmTask> resultTaskList = new ArrayList<>();
        //处理全配置任务数据
        if(CollUtil.isNotEmpty(allConfigTaskList)){
            resultTaskList.addAll(getAllConfigTask(allConfigTaskList, configMap, recordMap, currentLocalDateTime, currentDate));
        }

        if(CollUtil.isNotEmpty(startConfigTaskList)){
            //处理开始配置任务数据
            resultTaskList.addAll(getStartConfigTask(startConfigTaskList, configMap, recordMap, currentLocalDateTime, currentDate));
        }
        return resultTaskList;
    }

    /**
     * 获取只配置过开始步骤且取数正确的任务
     * @param startConfigTaskList 只配置过开始步骤的任务数据
     * @param configMap 任务配置数据
     * @param recordMap 任务推送记录数据
     * @param currentLocalDateTime LocalDateTime类型当前时间
     * @param currentDate Date类型当前时间
     * @return 符合的任务集合
     */
    List<MdmTask> getStartConfigTask(List<MdmTask> startConfigTaskList,
                                     Map<String, MdmTaskAutoTriggerConfig> configMap,
                                     Map<String, MdmTaskAutoTriggerRecord> recordMap,
                                     LocalDateTime currentLocalDateTime,
                                     Date currentDate){
        //需要保存的任务记录数据
        List<MdmTaskAutoTriggerRecord> saveRecordList = new ArrayList<>();
        //所有配置的触发点位code
        List<String> allPointList = new ArrayList<>();
        //key后缀
        String suffix = "_" + PlanTriggersEnum.AUTO_TRIGGER_CONFIG_TYPE_START.code;
        //处理数据
        startConfigTaskList.forEach(task -> {
            //拼接key
            String key = task.getId() + suffix;
            //获取数据
            MdmTaskAutoTriggerConfig triggerConfig = configMap.get(key);
            if (null != triggerConfig) {
                allPointList.add(triggerConfig.getTriggerPoint());
                task.setAutoTriggerConfigObj(triggerConfig);
            }
        });

        //10分钟前的时间,去掉秒
        LocalDateTime tenMinutesAgo = currentLocalDateTime.minusMinutes(minutesAgoNum).truncatedTo(ChronoUnit.MINUTES);
        //11分钟前的时间,去掉秒
        LocalDateTime elevenMinutesAgo = currentLocalDateTime.minusMinutes(minutesAgoNum+1).truncatedTo(ChronoUnit.MINUTES);
        //查询influxDB数据
        List<MeasurementData> pointDataList = getInfluxDBData(allPointList, elevenMinutesAgo, currentLocalDateTime);
        if(CollUtil.isEmpty(pointDataList)){
            log.error("自动触发任务工单生成----没有自动化点位数据");
        }

        //将任务按照触发点位、触发条件、触发值分组（过滤掉autoTriggerConfigObj为null的任务，防止NPE）
        Map<CompositeKey, List<MdmTask>> groupMap = startConfigTaskList.stream()
                .filter(task -> task.getAutoTriggerConfigObj() != null)
                .collect(Collectors.groupingBy(
                        task -> new CompositeKey(task.getAutoTriggerConfigObj().getTriggerPoint(), task.getAutoTriggerFrequencyType()+"_"+task.getAutoTriggerConfigObj().getTriggerConditionType().toString(), task.getAutoTriggerConfigObj().getTriggerConditionValue())
                ));
        String enter = PlanTriggersEnum.AUTO_TRIGGER_CONDITION_TYPE_ENTER.code+"";
        String over = PlanTriggersEnum.AUTO_TRIGGER_CONDITION_TYPE_OVER.code+"";
        //符合触发条件的任务集合
        List<MdmTask> resultTaskList = new ArrayList<>();
        for (Map.Entry<CompositeKey, List<MdmTask>> entry : groupMap.entrySet()) {
            //触发条件
            CompositeKey compositeKey = entry.getKey();
            //任务数据
            List<MdmTask> taskListTemp = entry.getValue();
            //触发点位
            String triggerPoint = compositeKey.pointCode;
            //类型
            String[] typeArr = compositeKey.calculateType.split("_");
            //任务触发类型,1:实时触发
            String triggerType = typeArr[0];
            //取数触发类型：7进入、8结束
            String triggerValueType = typeArr[1];
            //触发值
            Double triggerValue;
            try {
                triggerValue = Double.parseDouble(compositeKey.calculateValue);
            } catch (Exception e){
                log.error("自动触发任务工单生成--开始配置----触发值转换异常{}", taskListTemp.stream().toList());
                continue;
            }
            if(StrUtil.isNotBlank(triggerPoint)){
                //计算方式 进入1、结束2
                //初始化一个自动化不可能存在的值，防止第一次异常匹配上
                Double valueTemp = 31415926.53;
                for(MeasurementData measurementData : pointDataList){
                    if(measurementData.getCnlNum().equals(triggerPoint)){
                        //进入触发,赋值第一个值
                        if(valueTemp.equals(31415926.53)){
                            valueTemp = measurementData.getValue();
                        }
                        boolean isComming = false;
                        //进入
                        if(triggerValueType.equals(enter)){
                            if(triggerValue.equals(measurementData.getValue()) && !valueTemp.equals(triggerValue) ){
                                isComming = true;
                            }
                        }
                        //结束
                        else if(triggerValueType.equals(over)){
                            if(!triggerValue.equals(measurementData.getValue()) && valueTemp.equals(triggerValue)){
                                isComming = true;
                            }
                        }
                        //将本次的值给全局，为下次循环比较做准备
                        valueTemp = measurementData.getValue();
                        //将符合的数据添加到返回结果集合中
                        if(isComming && !measurementData.getTime().isBefore(tenMinutesAgo.atZone(ZoneId.systemDefault()).toInstant())){
                            // 指定时区（例如：系统默认时区）
                            ZoneId zoneId = ZoneId.systemDefault();
                            LocalDateTime localDateTime = measurementData.getTime().atZone(zoneId).toLocalDateTime();
                            taskListTemp.forEach(task -> {
                                MdmTask clonedTask = BeanUtil.copyProperties(task, MdmTask.class);
                                clonedTask.setAutoTriggerCalcTime(localDateTime);
                                clonedTask.setAutoTriggerLastTime(currentDate);
                                resultTaskList.add(clonedTask);
                                MdmTaskAutoTriggerRecord record = recordMap.get(task.getId());
                                if(null == record){
                                    record = new MdmTaskAutoTriggerRecord();
                                    record.setTaskId(task.getId());
                                    recordMap.put(task.getId(), record);
                                }
                                record.setState(0);
                                record.setStartTime(currentLocalDateTime);
                                record.setEndTime(currentLocalDateTime);
                                record.setLastTime(currentLocalDateTime);
                                saveRecordList.add(record);
                            });
                            //不是实时触发任务就触发一次就可以了
                            if(!"1".equals(triggerType)){
                                break;
                            }
                        }
                    }
                }
            }
        }
        mdmTaskAutoTriggerRecordService.saveOrUpdateBatch(saveRecordList);
        return resultTaskList;
    }

    /**
     * 获取配置过开始步骤、结束步骤且取数正确的任务
     * @param allConfigTaskList 配置过开始步骤、结束步骤的任务数据
     * @param configMap 任务配置数据
     * @param recordMap 任务推送记录数据
     * @param currentLocalDateTime LocalDateTime类型当前时间
     * @param currentDate Date类型当前时间
     * @return 符合的任务集合
     */
    List<MdmTask> getAllConfigTask(List<MdmTask> allConfigTaskList,
                                     Map<String, MdmTaskAutoTriggerConfig> configMap,
                                     Map<String, MdmTaskAutoTriggerRecord> recordMap,
                                     LocalDateTime currentLocalDateTime,
                                     Date currentDate){
        //符合触发条件的任务集合
        List<MdmTask> resultTaskList = new ArrayList<>();
        //符合触发条件的任务集合
        List<MdmTaskAutoTriggerRecord> triggerRecordList = new ArrayList<>();
        for(MdmTask task : allConfigTaskList){
            //获取任务对应的配置
            MdmTaskAutoTriggerConfig startConfig = configMap.get(task.getId()+ "_" + PlanTriggersEnum.AUTO_TRIGGER_CONFIG_TYPE_START.code);
            MdmTaskAutoTriggerConfig endConfig = configMap.get(task.getId()+ "_" + PlanTriggersEnum.AUTO_TRIGGER_CONFIG_TYPE_END.code);
            if(null == startConfig || null == endConfig){
                log.error("自动触发任务工单生成----自动触发任务配置数据不完整");
                continue;
            }

            //获取任务对应的推送记录
            MdmTaskAutoTriggerRecord record = recordMap.get(task.getId());
            //不存在记录，则新建一条记录
            if(null == record){
                //创建记录，设置状态0未运行
                record = new MdmTaskAutoTriggerRecord();
                record.setState(0);
                record.setTaskId(task.getId());
                recordMap.put(task.getId(), record);
            }

            //记录在运行中的逻辑步骤
            if(1 == record.getState()){
                //最后触发时间不为空，且在当前时间之前
                if(null != record.getLastTime() && record.getLastTime().isBefore(currentLocalDateTime)){
                    //获取间隔时间
                    long frequencyHour =Duration.between(record.getLastTime(), currentLocalDateTime).toHours();
                    //间隔时间大于等于任务触发间隔时间，则加入结果集
                    if(frequencyHour >= task.getAutoTriggerFrequencyValue()){
                        MdmTask clonedTask = BeanUtil.copyProperties(task, MdmTask.class);
                        clonedTask.setAutoTriggerCalcTime(currentLocalDateTime);
                        clonedTask.setAutoTriggerLastTime(currentDate);
                        resultTaskList.add(clonedTask);
                        record.setLastTime(currentLocalDateTime);
                    }
                }
            }
            //触发类型
            String triggerValueType;
            //触发点位
            Double triggerValue;
            //需要查询的点位
            List<String> poindCodeList = new ArrayList<>();
            //如果未运行取开始配置，否则取结束配置（0：未运行）
            if(0 == record.getState()){
                poindCodeList.add(startConfig.getTriggerPoint());
                triggerValueType = startConfig.getTriggerConditionType().toString();
                try {
                    triggerValue = Double.valueOf(startConfig.getTriggerConditionValue());
                } catch (Exception e){
                    log.error("自动触发任务工单生成--全部配置开始----触发值转换异常{}", task.getId());
                    continue;
                }
            }else{
                poindCodeList.add(endConfig.getTriggerPoint());
                triggerValueType = endConfig.getTriggerConditionType().toString();
                try {
                    triggerValue = Double.valueOf(endConfig.getTriggerConditionValue());
                } catch (Exception e){
                    log.error("自动触发任务工单生成--全部配置结束----触发值转换异常{}", task.getId());
                    continue;
                }
            }
            //获取开始时间，查询11分钟前的数据，避免刚好卡10分钟点触发不了任务
            LocalDateTime startTime = currentLocalDateTime.minusMinutes(minutesAgoNum+1);
            //查询influxDB数据
            List<MeasurementData> pointDataList = getInfluxDBData(poindCodeList, startTime, currentLocalDateTime);
            if(CollUtil.isEmpty(pointDataList)){
                log.error("自动触发任务工单生成----没有查询到influxDB数据");
                continue;
            }

            //计算方式 进入1、结束2
            //初始化一个自动化不可能存在的值，防止第一次异常匹配上
            Double valueTemp = 31415926.53;
            String enter = PlanTriggersEnum.AUTO_TRIGGER_CONDITION_TYPE_ENTER.code+"";
            String over = PlanTriggersEnum.AUTO_TRIGGER_CONDITION_TYPE_OVER.code+"";
            //数据时间
            LocalDateTime dataLocalDateTime;
            for(MeasurementData measurementData : pointDataList){
                // 指定时区（例如：系统默认时区）
                ZoneId zoneId = ZoneId.systemDefault();
                dataLocalDateTime = measurementData.getTime().atZone(zoneId).toLocalDateTime();
                if(dataLocalDateTime.isBefore(startTime)){
                    //如果数据的时间在当前时间之前，那么只记录一下值，避免卡点触发不了任务
                    valueTemp = measurementData.getValue();
                    continue;
                }
                boolean isComming = false;
                //判断是否为第一次触发，第一次触发将当前值设置为上一个值，避免重复触发
                if(valueTemp.equals(31415926.53)){
                    valueTemp = measurementData.getValue();
                }
                //进入:从不等于触发值（!valueTemp.equals(triggerValue)）到等于触发值(triggerValue.equals(measurementData.getValue()))
                if(triggerValueType.equals(enter)){
                    if(triggerValue.equals(measurementData.getValue()) && !valueTemp.equals(triggerValue) ){
                        isComming = true;
                    }
                }
                //结束：从等于（valueTemp.equals(triggerValue)）到不等于（!triggerValue.equals(measurementData.getValue())）
                else if(triggerValueType.equals(over)){
                    if(!triggerValue.equals(measurementData.getValue()) && valueTemp.equals(triggerValue)){
                        isComming = true;
                    }
                }
                //将本次的值给全局，为下次循环比较做准备
                valueTemp = measurementData.getValue();
                //如果数据触发了，且是未运行状态
                if(isComming){
                    if(0 == record.getState()){
                        //设置记录为运行中
                        record.setState(1);
                        record.setStartTime(dataLocalDateTime);
                        record.setEndTime(null);
                        record.setLastTime(dataLocalDateTime);
                        //获取任务
                        MdmTask clonedTask = BeanUtil.copyProperties(task, MdmTask.class);
                        clonedTask.setAutoTriggerCalcTime(dataLocalDateTime);
                        clonedTask.setAutoTriggerLastTime(currentDate);
                        resultTaskList.add(clonedTask);
                        //获取结束配置
                        triggerValueType = endConfig.getTriggerConditionType().toString();
                        triggerValue = Double.valueOf(endConfig.getTriggerConditionValue());
                    }else{
                        //设置记录为未运行
                        record.setState(0);
                        record.setEndTime(dataLocalDateTime);
                        //获取开始配置
                        triggerValueType = startConfig.getTriggerConditionType().toString();
                        triggerValue = Double.valueOf(startConfig.getTriggerConditionValue());
                    }
                }
            }
            triggerRecordList.add(record);
        }
        mdmTaskAutoTriggerRecordService.saveOrUpdateBatch(triggerRecordList);
        return resultTaskList;
    }

    /**
     * 获取指定时间段内指定点位的数据
     * @param allPointList 所有点位集合
     * @param startTime 时间段开始时间
     * @param endTime 时间段结束时间
     * @return 指定时间段内指定点位的数据
     */
    List<MeasurementData> getInfluxDBData(List<String> allPointList, LocalDateTime startTime, LocalDateTime endTime){
        if(allPointList.isEmpty()){
            log.error("自动触发任务工单生成----没有点位配置");
            return null;
        }

        //组装查询influxDB参数，返回对应的自动化值
        QueryCondition queryCondition = new QueryCondition();
        List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
        for(String tagCode : allPointList){
            if(StrUtil.isNotBlank(tagCode)){
                QueryTagCondition queryTagCondition = new QueryTagCondition();
                queryTagCondition.setTagId(tagCode);
                queryTagConditionList.add(queryTagCondition);
            }
        }
        queryCondition.setStart(startTime);
        queryCondition.setEnd(endTime);
        queryCondition.setQueryTagConditionList(queryTagConditionList);
        //没有点位查询条件停止后续操作
        if(CollUtil.isEmpty(queryTagConditionList)){
            log.error("自动触发任务工单生成----没有点位查询条件");
            return null;
        }
        try{
            return influxDBClientHelper.query(queryCondition);
        }catch (Exception e){
            log.error("自动触发任务工单生成----查询点位数据异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 用于处理服务器停电或者服务意外停止导致任务没下发的数据
     * @param currentDate 当前时间
     */
    void sendByLastTime(Date currentDate){
        //尝试获取分布式锁，防止定时任务并发执行导致重复生成工单
        String lockValue = String.valueOf(System.currentTimeMillis());
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(SEND_BY_LAST_TIME_LOCK_KEY, lockValue, LOCK_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("自动触发任务-历史补跑(sendByLastTime)正在执行中，跳过本次调度");
            return;
        }
        try {
            doSendByLastTime(currentDate);
        } finally {
            //完成后释放锁（比较value防止误删其他线程的锁）
            Object currentValue = redisTemplate.opsForValue().get(SEND_BY_LAST_TIME_LOCK_KEY);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(SEND_BY_LAST_TIME_LOCK_KEY);
            }
        }
    }

    /**
     * 历史补跑实际执行逻辑
     */
    private void doSendByLastTime(Date currentDate){
        //获取10分钟前的时间
        Date minutesAgoDate = subtractMinutes(currentDate, minutesAgoNum);

        //1. 查找需要补跑的任务（DB查询 + Redis断点恢复）
        Map<String, MdmTask> needBackfillMap = new LinkedHashMap<>();

        //1.1 检查Redis中是否有上次断电未完成的补跑断点
        Set<String> checkpointKeys = redisTemplate.keys(BACKFILL_PREFIX + "*");
        if(CollUtil.isNotEmpty(checkpointKeys)){
            for (String key : checkpointKeys) {
                String taskId = key.substring(BACKFILL_PREFIX.length());
                MdmTask task = mdmTaskService.getById(taskId);
                if (task != null && task.getState() == 1) {
                    needBackfillMap.put(taskId, task);
                }
            }
            if(!needBackfillMap.isEmpty()){
                log.info("自动触发任务-历史补跑----发现{}个断点恢复任务", needBackfillMap.size());
            }
        }

        //1.2 查询DB中新的需要补跑的任务
        List<MdmTask> dbTaskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getState, 1)
                .eq(MdmTask::getTaskKindCode, 10)
                .isNotNull(MdmTask::getAutoTriggerLastTime)
                .lt(MdmTask::getAutoTriggerLastTime, minutesAgoDate)
                .list();
        for (MdmTask task : dbTaskList) {
            needBackfillMap.putIfAbsent(task.getId(), task);
        }

        //没有任务停止后续操作
        if(needBackfillMap.isEmpty()){
            log.error("自动触发任务工单生成----根据最后触发时间查询----没有自动触发任务");
            return;
        }

        //获取任务配置和记录
        Map<String, Object> resultMap = getConfigAndRecord(new ArrayList<>(needBackfillMap.values()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

        Map<String, Date> map = new HashMap<>();

        //2. 循环处理数据
        for (MdmTask task : needBackfillMap.values()) {
            String checkpointKey = BACKFILL_PREFIX + task.getId();

            //2.1 确定补跑起始点：优先从Redis断点恢复，否则用DB原始值
            String checkpointStr = (String) redisTemplate.opsForValue().get(checkpointKey);
            Date startPoint;
            if(StrUtil.isNotBlank(checkpointStr)){
                startPoint = new Date(Long.parseLong(checkpointStr));
                log.info("自动触发任务-历史补跑----任务{}从Redis断点恢复，起始时间:{}", task.getId(), startPoint);
            } else {
                startPoint = task.getAutoTriggerLastTime();
            }

            //2.2 记录起始断点到Redis（7天过期，安全兜底）
            redisTemplate.opsForValue().set(checkpointKey, String.valueOf(startPoint.getTime()), 7, java.util.concurrent.TimeUnit.DAYS);

            //2.3 从起始点+10分钟开始补跑
            Date tempDate = addMinutesToDate(startPoint, minutesAgoNum);
            int tempHour = Integer.parseInt(dateFormat.format(tempDate));
            int autoTriggerRangeStart = Integer.parseInt(task.getAutoTriggerRangeStart());
            int autoTriggerRangeEnd = Integer.parseInt(task.getAutoTriggerRangeEnd());
            //每次循环再将最后触发时间添加十分钟，直到当前时间，并且触发时间的小时数要小于等于配置的结束范围
            while(tempDate.before(minutesAgoDate)){
                if(tempHour < autoTriggerRangeStart || tempHour > autoTriggerRangeEnd){
                    tempDate = addMinutesToDate(tempDate, minutesAgoNum);
                    tempHour = Integer.parseInt(dateFormat.format(tempDate));
                    continue;
                }
                LocalDateTime tempLocalDateTime = LocalDateTime.ofInstant(tempDate.toInstant(), ZoneId.systemDefault());
                Date beginDate = tempDate;
                //上一个日期
                Date prevDate = map.get(task.getId());
                if(task.getAutoTriggerFrequencyType() == 10){
                    //每天推送一次的处理
                    //获取指定日期的开始时间：00:00:00,结束时间：23:59:59
                    beginDate = DateUtil.beginOfDay(tempDate);
                    Date endOfDay = DateUtil.endOfDay(tempDate);
                    if(validPlanOrder(map, prevDate, beginDate, endOfDay, task)){
                        //如果有数据了，将日期改为后一天的开始触发时间
                        tempDate = DateUtil.offsetHour(
                                DateUtil.beginOfDay(DateUtil.offsetDay(tempDate, 1)),
                                autoTriggerRangeStart
                        );
                        tempHour = Integer.parseInt(dateFormat.format(tempDate));
                        continue;
                    }
                }else if(task.getAutoTriggerFrequencyType() == 20){
                    //每周推送一次的处理
                    //获取本周第一天时间（周一算第一天）,最后一天时间（周日算最后一天）
                    beginDate = DateUtil.beginOfWeek(tempDate, true);
                    Date endDate = DateUtil.endOfWeek(tempDate, true);
                    if(validPlanOrder(map, prevDate, beginDate, endDate, task)){
                        //如果有数据了，将日期改为下一周的开始触发时间
                        tempDate = DateUtil.offsetHour(
                                DateUtil.beginOfWeek(DateUtil.offsetWeek(tempDate, 1)),
                                autoTriggerRangeStart
                        );
                        tempHour = Integer.parseInt(dateFormat.format(tempDate));
                        continue;
                    }
                }

                //获取符合触发条件的任务
                List<MdmTask> newTaskList = getAutoTriggerTaskList(List.of(task), tempLocalDateTime, tempDate,
                        (Map<String, MdmTaskAutoTriggerConfig>) resultMap.get("configMap"),
                        (Map<String, MdmTaskAutoTriggerRecord>) resultMap.get("recordMap")
                );
                //有符合条件的数据则生成工单
                if(CollUtil.isNotEmpty(newTaskList)){
                    map.put(task.getId(), beginDate);
                    //获取所有设备id
                    Set<Integer> deviceIds = newTaskList.stream()
                            .map(MdmTask::getDeviceId)
                            .collect(Collectors.toSet());
                    //调用生成工单方法
                    this.createOrder(PlanStatusEnum.TASK_CYCLE.code, newTaskList, deviceIds, null, tempDate, null);
                }
                //每处理完一个10分钟段，将断点记录到Redis（不修改DB的autoTriggerLastTime，避免干扰正常流程）
                redisTemplate.opsForValue().set(checkpointKey, String.valueOf(tempDate.getTime()), 7, java.util.concurrent.TimeUnit.DAYS);
                tempDate = addMinutesToDate(tempDate, 10);
                tempHour = Integer.parseInt(dateFormat.format(tempDate));
            }
            //补跑完成，删除Redis断点
            redisTemplate.delete(checkpointKey);
        }
    }

    Boolean validPlanOrder(Map<String, Date> map, Date prevDate, Date beginDate, Date endDate, MdmTask task){
        if(null != prevDate && prevDate.equals(beginDate)){
            //如果有数据且时间一致，则跳过
            return true;
        } else {
            //如果时间不一致或者为空，则查询数据
            List<PlanOrder> planOrderList = planOrderService.lambdaQuery()
                    .eq(PlanOrder::getTaskId, task.getId())
                    .ge(PlanOrder::getAutoTriggerCalcTime, beginDate)
                    .le(PlanOrder::getAutoTriggerCalcTime, endDate)
                    .list();
            //如果有数据，则跳过
            if(CollUtil.isNotEmpty(planOrderList)){
                map.put(task.getId(), beginDate);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断小时间隔
     * @param curHour 当前小时
     * @param startHour 设定的开始小时
     * @param interval 设定的间隔数
     * @return true/false
     */
    private boolean isInInterval(int curHour, String startHour, String interval) {
        if(StrUtil.isBlank(startHour) || StrUtil.isBlank(interval)){
            return false;
        }
        //计算时间间隔
        int val = Integer.parseInt(startHour) - curHour;
        //如果为负数取绝对值
        if(val < 0){
            val = -val;
        }
        //取余为0说明需要触发
        return val == 0 || val % Integer.parseInt(interval) == 0;
    }
}
