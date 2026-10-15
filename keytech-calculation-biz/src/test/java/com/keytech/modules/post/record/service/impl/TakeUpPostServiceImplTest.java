package com.keytech.modules.post.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.mdm3.entity.MdmPostDevice;
import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.mdm3.service.impl.MdmPostDeviceServiceImpl;
import com.keytech.modules.mdm3.service.impl.MdmTaskServiceImpl;
import com.keytech.modules.plan.service.impl.PlanDeviceServiceImpl;
import com.keytech.modules.post.record.mapper.TakeUpPostMapper;
import com.keytech.modules.post.take.entity.TakeUpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * --story=1057804 --user=孙金伟 22-交接班推送方式调整 https://www.tapd.cn/63554897/s/1390437 单元测试
 */
public class TakeUpPostServiceImplTest {

    @InjectMocks
    private TakeUpPostServiceImpl takeUpPostService;

    @Mock
    private MdmTaskServiceImpl mdmTaskService;

    @Mock
    private MdmPostDeviceServiceImpl mdmPostDevice;

    @Mock
    private PlanDeviceServiceImpl planDeviceService;

    @Mock
    private TakeUpPostMapper baseMapper;

    private TakeUpPost takeUpPost;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(takeUpPostService, "baseMapper", baseMapper);
        takeUpPost = new TakeUpPost();
        takeUpPost.setPostId(1);
        takeUpPost.setTeam("team1");
    }

    /**
     * TC01: 存在重复上岗记录，直接返回
     */
    @Test
    public void testSendPostRecord_WithDuplicateRecord_ShouldReturnEarly() {
        // 准备数据
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = startOfDay.format(formatter);

        List<TakeUpPost> takeUpPostList = Arrays.asList(new TakeUpPost());

        // Mock查询结果
        LambdaQueryWrapper<TakeUpPost> queryWrapper = Wrappers.lambdaQuery();
        when(baseMapper.selectList(any())).thenReturn(takeUpPostList);

        // 执行方法
        takeUpPostService.sendPostRecord(takeUpPost);

        // 验证后续逻辑未执行
        verify(mdmPostDevice, never()).lambdaQuery();
    }

    /**
     * TC02: 岗位无关联设备，直接返回
     */
    @Test
    public void testSendPostRecord_NoDevice_ShouldReturnEarly() {
        // 准备数据
        List<TakeUpPost> takeUpPostList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(takeUpPostList);

        List<MdmPostDevice> postDeviceList = new ArrayList<>();
        when(mdmPostDevice.lambdaQuery().eq(any(), any()).list()).thenReturn(postDeviceList);

        // 执行方法
        takeUpPostService.sendPostRecord(takeUpPost);

        // 验证后续逻辑未执行
        verify(mdmTaskService, never()).lambdaQuery();
    }

    /**
     * TC03: 无交接班任务，直接返回
     */
    @Test
    public void testSendPostRecord_NoTask_ShouldReturnEarly() {
        // 准备数据
        List<TakeUpPost> takeUpPostList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(takeUpPostList);

        List<MdmPostDevice> postDeviceList = Arrays.asList(new MdmPostDevice().setDeviceId(1).setPostId(1));
        when(mdmPostDevice.lambdaQuery().eq(any(), any()).list()).thenReturn(postDeviceList);

        List<MdmTask> taskList = new ArrayList<>();
        when(mdmTaskService.lambdaQuery().eq(any(), any()).in(any(), anyCollection()).list()).thenReturn(taskList);

        // 执行方法
        takeUpPostService.sendPostRecord(takeUpPost);

        // 验证后续逻辑未执行
        verify(planDeviceService, never()).takeUpPostJob(any(), any(), any());
    }

    /**
     * TC04: 所有条件满足，正常执行
     */
    @Test
    public void testSendPostRecord_AllConditionsMet_ShouldCallTakeUpPostJob() {
        // 准备数据
        List<TakeUpPost> takeUpPostList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(takeUpPostList);

        List<MdmPostDevice> postDeviceList = Arrays.asList(new MdmPostDevice().setDeviceId(1).setPostId(1));
        when(mdmPostDevice.lambdaQuery().eq(any(), any()).list()).thenReturn(postDeviceList);

        List<MdmTask> taskList = Arrays.asList(new MdmTask());
        when(mdmTaskService.lambdaQuery().eq(any(), any()).in(any(), anyCollection()).list()).thenReturn(taskList);

        doNothing().when(planDeviceService).takeUpPostJob(any(), any(), any());

        // 执行方法
        takeUpPostService.sendPostRecord(takeUpPost);

        // 验证 planDeviceService.takeUpPostJob 被调用
        verify(planDeviceService, times(1)).takeUpPostJob(any(), any(), any());
    }
}
