package com.keytech.modules.plan.service;

import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.base.service.BaseService;
import com.keytech.modules.post.take.entity.TakeUpPost;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Description: 工单管理-机台计划
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
public interface IPlanDeviceService extends BaseService<PlanDevice> {
    /**
     * 定时任务
     * @param paramJson 参数
     */
    void planJob(String paramJson);

    /**
     * 延迟关闭定时任务
     * @param paramJson 参数
     */
    void delayClosPlanJob(String paramJson);

    /**
     * 自动触发任务
     * @param paramJson 参数
     * @param currentDate Date类型当前时间
     * @param currentLocalDateTime LocalDateTime类型当前时间
     */
    void autoTriggerPlanJob(String paramJson, Date currentDate, LocalDateTime currentLocalDateTime);

    /**
     * 开启机台计划
     * @param plan 计划对象
     * @param now 当前时间
     */
    void startPlanDevice(Plan plan, Date now);

    /**
     * 推送交接班任务
     * @param takeUpPost 上岗信息
     * @param taskList 交接班任务对象
     * @param now 当前时间
     */
    void takeUpPostJob(TakeUpPost takeUpPost, List<MdmTask> taskList, Date now);

    /**
     * 延迟关闭计划
     * @param planId 计划主键
     * @param deviceIds 机台主键
     * @param realEndTime 实际关闭时间
     * @param delayTime 延迟关闭时间
     */
    void delayEndPlanDevice(String planId, String deviceIds, Date realEndTime, Date delayTime);

    /**
     * 关闭机台计划
     * @param planId 计划id
     * @param deviceIds 机台id字符串,逗号拼接
     * @param realEndTime 实际结束时间
     * @param isDelayClose 是否延迟关闭
     */
    void endPlanDevice(String planId, String deviceIds, Date realEndTime, Boolean isDelayClose);

    /**
     * 恢复或暂停机台计划
     * @param plan 计划对象
     * @param now 当前时间
     * @param status 状态
     */
    void pauseOrRenewPlanDevice(Plan plan, Integer status, Date now);

    /**
     * 手动触发任务
     * @param taskList 任务对象
     * @param now 当前时间
     */
    void sendManualTrigger(List<MdmTask> taskList, Date now);


    String planOrderTest(String id, String startTime);
}
