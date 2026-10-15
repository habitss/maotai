package com.keytech.modules.post.record.controller;

import com.keytech.modules.post.record.service.ITakeUpPostService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1057805 --user=孙金伟 22-交接班上岗规则调整 https://www.tapd.cn/63554897/s/1390231 单元测试
 */
@ExtendWith(MockitoExtension.class)
public class TakeUpPostControllerTest {

    @InjectMocks
    private TakeUpPostController takeUpPostController;

    @Mock
    private ITakeUpPostService postRecordService;

    @Mock
    private HttpServletRequest request;

    /**
     * 测试用例：正常情况 - 传入包含多个TakeUpPost对象的列表
     * 预期结果：正确调用服务方法并返回成功结果
     */
    @Test
    public void testStartPostBatch_NormalCase() {
        // 准备测试数据
        List<TakeUpPost> takeUpPostList = new ArrayList<>();
        TakeUpPost post1 = new TakeUpPost();
        post1.setId("1");
        takeUpPostList.add(post1);
        
        TakeUpPost post2 = new TakeUpPost();
        post2.setId("2");
        takeUpPostList.add(post2);

        // 配置mock行为
        when(postRecordService.startPost(any(TakeUpPost.class), any(HttpServletRequest.class)))
                .thenReturn(Result.OK("上岗成功"));

        // 执行被测方法
        Result<String> result = takeUpPostController.startPostBatch(takeUpPostList, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("上岗成功!", result.getMessage());
        
        // 验证服务方法被正确调用
        verify(postRecordService, times(takeUpPostList.size())).startPost(any(TakeUpPost.class), any(HttpServletRequest.class));
    }

    /**
     * 测试用例：边界情况 - 传入空列表
     * 预期结果：返回成功结果，不调用服务方法
     */
    @Test
    public void testStartPostBatch_EmptyList() {
        // 准备测试数据
        List<TakeUpPost> takeUpPostList = new ArrayList<>();

        // 执行被测方法
        Result<String> result = takeUpPostController.startPostBatch(takeUpPostList, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("上岗成功!", result.getMessage());
        
        // 验证服务方法没有被调用
        verify(postRecordService, never()).startPost(any(TakeUpPost.class), any(HttpServletRequest.class));
    }

    /**
     * 测试用例：边界情况 - 传入null列表
     * 预期结果：能正确处理null输入
     */
    @Test
    public void testStartPostBatch_NullList() {
        // 执行被测方法
        Result<String> result = takeUpPostController.startPostBatch(null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("上岗成功!", result.getMessage());
        
        // 验证服务方法没有被调用
        verify(postRecordService, never()).startPost(any(TakeUpPost.class), any(HttpServletRequest.class));
    }
}
