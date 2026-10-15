package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.pi.entity.PiPost;
import com.keytech.modules.pi.service.IPiPostService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PiPostControllerTest {

    @InjectMocks
    private PiPostController piPostController;

    @Mock
    private IPiPostService piPostService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    //--story=1052552 --user=孙金伟 02-APP-PI日报填报模块-02-PI日报手机展示 https://www.tapd.cn/63554897/s/1381272 单元测试
    /**
     * TC01: 正常流程 - 查询到数据
     */
    @Test
    void testListAll_SuccessWithData() {
        // Arrange
        PiPost piPost = new PiPost();
        Map<String, String[]> parameterMap = new HashMap<>();
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        List<PiPost> piPostList = new ArrayList<>();
        piPostList.add(new PiPost());
        when(piPostService.list(any(QueryWrapper.class))).thenReturn(piPostList);

        // Act
        Result<List<PiPost>> result = piPostController.listAll(piPost, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getResult().size());
        verify(piPostService, times(1)).list(any(QueryWrapper.class));
    }

    /**
     * TC02: 正常流程 - 查询无数据
     */
    @Test
    void testListAll_EmptyData() {
        // Arrange
        PiPost piPost = new PiPost();
        Map<String, String[]> parameterMap = new HashMap<>();
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        List<PiPost> piPostList = Collections.emptyList();
        when(piPostService.list(any(QueryWrapper.class))).thenReturn(piPostList);

        // Act
        Result<List<PiPost>> result = piPostController.listAll(piPost, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getResult().isEmpty());
        verify(piPostService, times(1)).list(any(QueryWrapper.class));
    }

    /**
     * TC03: service 层抛出异常
     */
    @Test
    void testListAll_ServiceException() {
        // Arrange
        PiPost piPost = new PiPost();
        Map<String, String[]> parameterMap = new HashMap<>();
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        when(piPostService.list(any(QueryWrapper.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        Result<List<PiPost>> result = piPostController.listAll(piPost, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("系统异常，请稍后重试", result.getMessage());
        verify(piPostService, times(1)).list(any(QueryWrapper.class));
    }


    /**
     * --story=1052553 --user=孙金伟 02-APP-PI日报填报模块-03-PI日报手机填报 https://www.tapd.cn/63554897/s/1381840 单元测试
     * TC01: 正常流程 - 查询到数据
     */
    @Test
    void testQueryPageAppList_SuccessWithData() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String tableName = "test_table";

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("list", List.of(new PiPost()));
        mockData.put("total", 1L);

        when(piPostService.queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest)))
                .thenReturn(mockData);

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mockData, result.getResult());
        verify(piPostService, times(1)).queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest));
    }

    /**
     * --story=1052553 --user=孙金伟 02-APP-PI日报填报模块-03-PI日报手机填报 https://www.tapd.cn/63554897/s/1381840 单元测试
     * TC02: 正常流程 - 查询无数据
     */
    @Test
    void testQueryPageAppList_EmptyData() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String tableName = "test_table";

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("list", List.<PiPost>of());
        mockData.put("total", 0L);

        when(piPostService.queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest)))
                .thenReturn(mockData);

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mockData, result.getResult());
        verify(piPostService, times(1)).queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest));
    }

    /**
     * TC03: service 层抛出异常
     */
    @Test
    void testQueryPageAppList_ServiceException() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String tableName = "test_table";

        when(piPostService.queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("系统异常，请稍后重试", result.getMessage());
        verify(piPostService, times(1)).queryPageAppList(eq(piPost), eq(pageNo), eq(pageSize), eq(tableName), eq(httpServletRequest));
    }

    /**
     * TC04: pageNo <= 0 时自动修正为默认值 1
     */
    @Test
    void testQueryPageAppList_PageNoLessThanZero_UseDefault() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = -1;
        Integer pageSize = 10;
        String tableName = "test_table";

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("list", List.of(new PiPost()));
        mockData.put("total", 1L);

        when(piPostService.queryPageAppList(eq(piPost), eq(1), eq(pageSize), eq(tableName), eq(httpServletRequest)))
                .thenReturn(mockData);

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mockData, result.getResult());
        verify(piPostService, times(1)).queryPageAppList(eq(piPost), eq(1), eq(pageSize), eq(tableName), eq(httpServletRequest));
    }

    /**
     * TC05: pageSize <= 0 时自动修正为默认值 10
     */
    @Test
    void testQueryPageAppList_PageSizeLessThanZero_UseDefault() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = -5;
        String tableName = "test_table";

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("list", List.of(new PiPost()));
        mockData.put("total", 1L);

        when(piPostService.queryPageAppList(eq(piPost), eq(pageNo), eq(10), eq(tableName), eq(httpServletRequest)))
                .thenReturn(mockData);

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mockData, result.getResult());
        verify(piPostService, times(1)).queryPageAppList(eq(piPost), eq(pageNo), eq(10), eq(tableName), eq(httpServletRequest));
    }

    /**
     * TC06: tableName 为空时返回参数缺失错误
     */
    @Test
    void testQueryPageAppList_TableNameEmpty_ReturnError() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String tableName = "";

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("关联表名不能为空", result.getMessage());
        verify(piPostService, never()).queryPageAppList(any(), anyInt(), anyInt(), anyString(), any(HttpServletRequest.class));
    }

    /**
     * TC07: tableName 为 null 时返回参数缺失错误
     */
    @Test
    void testQueryPageAppList_TableNameNull_ReturnError() {
        // Arrange
        PiPost piPost = new PiPost();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String tableName = null;

        // Act
        Result<?> result = piPostController.queryPageAppList(piPost, pageNo, pageSize, tableName, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("关联表名不能为空", result.getMessage());
        verify(piPostService, never()).queryPageAppList(any(), anyInt(), anyInt(), anyString(), any(HttpServletRequest.class));
    }
}
