package com.keytech.modules.post.record.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.MdmPostDevice;
import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.mdm3.service.impl.MdmPostDeviceServiceImpl;
import com.keytech.modules.mdm3.service.impl.MdmTaskServiceImpl;
import com.keytech.modules.plan.service.impl.PlanDeviceServiceImpl;
import com.keytech.modules.post.record.mapper.TakeUpPostMapper;
import com.keytech.modules.post.record.service.ITakeUpPostService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 上下岗记录
 * @Author: jeecg-boot
 * @Date:   2025-06-16
 * @Version: V1.0
 */
@Service
public class TakeUpPostServiceImpl extends BaseServiceImpl<TakeUpPostMapper, TakeUpPost> implements ITakeUpPostService {
    @Autowired
    private MdmTaskServiceImpl mdmTaskService;
    @Autowired
    private MdmPostDeviceServiceImpl mdmPostDevice;
    @Autowired
    private PlanDeviceServiceImpl planDeviceService;
    @Override
    public Result<String> startPost(TakeUpPost takeUpPost, HttpServletRequest request) {
        //获取当前登录用户信息
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //判断当前人员是否在当前岗位
        LambdaQueryWrapper<TakeUpPost> postRecordLambdaQueryWrapper = new LambdaQueryWrapper<>();
        postRecordLambdaQueryWrapper
                .eq(TakeUpPost::getUsername,sysUser.getUsername())
                .eq(TakeUpPost::getPostId,takeUpPost.getPostId())
                .eq(TakeUpPost::getStatus,0)
                .eq(TakeUpPost::getDelFlag,0);
        if (this.count(postRecordLambdaQueryWrapper)>0) {
            return Result.ok(GlobalErrorEnum.CURRENTLY_ON_DUTY.getCode());
        }
        sendPostRecord(takeUpPost);
        takeUpPost.setStatus(0);
        takeUpPost.setPostStartTime(new Date());
        takeUpPost.setUsername(sysUser.getUsername());
        takeUpPost.setRealname(sysUser.getRealname());
        this.save(takeUpPost);
        return Result.OK("上岗成功!");
    }

    @Override
    @Transactional
    public void endPostBatch(String ids) {
        //获取在岗数据
        List<TakeUpPost> list = this.lambdaQuery()
                .in(TakeUpPost::getId, Arrays.asList(ids.split(",")))
                .eq(TakeUpPost::getStatus, 0)
                .list();
        //处理离岗信息
        processingEndData(list, null);
        this.saveOrUpdateBatch(list);
    }

    @Override
    @Transactional
    public void endPostByTime() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 减去 23 小时
        LocalDateTime twentyFiveHoursAgo = now.minusHours(23);
        // 定义格式器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化为字符串
        String formattedTime = twentyFiveHoursAgo.format(formatter);
        //获取在岗数据
        List<TakeUpPost> list = this.lambdaQuery()
                .eq(TakeUpPost::getStatus, 0)
                .le(TakeUpPost::getPostStartTime, formattedTime)
                .list();
        //处理离岗信息
        processingEndData(list, "3");
        this.updateBatchById(list);
    }

    private void processingEndData(List<TakeUpPost> list, String autoEnd){
        //获取当前时间
        Date endDate = new Date();
        if (CollectionUtils.isNotEmpty(list)) {
            for (TakeUpPost takeUpPost : list) {
                //处理离岗数据
                takeUpPost.setPostEndTime(endDate);
                takeUpPost.setTotalTime(Math.round((float) (endDate.getTime() - takeUpPost.getPostStartTime().getTime()) / 1000 / 60));
                takeUpPost.setStatus(1);
                if(StrUtil.isNotBlank(autoEnd)){
                    takeUpPost.setField(autoEnd);
                }
            }
        }
    }

    /**
     * 推送交接班记录任务
     * @param takeUpPost 上岗信息
     */
    @Async
    protected void sendPostRecord(TakeUpPost takeUpPost){
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取当天的开始时间（00:00:00）
        LocalDateTime startOfDay = today.atStartOfDay();
        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化为字符串
        String formattedDate = startOfDay.format(formatter);
        //根据岗位和班组查询当日是否有上岗记录
        List<TakeUpPost> takeUpPostList = this.lambdaQuery()
                .eq(TakeUpPost::getPostId, takeUpPost.getPostId())
                .eq(TakeUpPost::getTeam, takeUpPost.getTeam())
                .gt(TakeUpPost::getPostStartTime, formattedDate)
                .list();
        if(CollectionUtils.isNotEmpty(takeUpPostList)){
            return;
        }
        //获取岗位关联机台设备
        List<MdmPostDevice> postDeviceList = mdmPostDevice.lambdaQuery()
                .eq(MdmPostDevice::getPostId, takeUpPost.getPostId()).list();
        if(CollectionUtils.isEmpty(postDeviceList)){
            return;
        }
        //获取所有设备id
        List<Integer> deviceIds = postDeviceList.stream().map(MdmPostDevice::getDeviceId).toList();
        //获取交接班任务
        List<MdmTask> taskList = mdmTaskService.lambdaQuery()
                .eq(MdmTask::getCheckTaskType, 2)
                .in(MdmTask::getDeviceId, deviceIds).list();
        if(CollectionUtils.isEmpty(taskList)){
            return;
        }
        //将岗位关联设备转换成map，设备id为key
        Map<Integer, Integer> devicePostMap = postDeviceList.stream()
                .collect(Collectors.toMap(
                        MdmPostDevice::getDeviceId,
                        MdmPostDevice::getPostId,
                        (existing, replacement) -> replacement
                ));
        takeUpPost.setDevicePostMap(devicePostMap);
        planDeviceService.takeUpPostJob(takeUpPost, taskList, new Date());
    }
}
