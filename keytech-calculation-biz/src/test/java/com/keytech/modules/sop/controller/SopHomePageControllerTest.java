package com.keytech.modules.sop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.service.ISopHomePageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055308 --user=孙金伟 23-标准制定与执行优化处理 https://www.tapd.cn/63554897/s/1382266 单元测试
 */
class SopHomePageControllerTest {

    @InjectMocks
    private SopHomePageController sopHomePageController;

    @Mock
    private ISopHomePageService sopHomePageService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * --story=1057859 --user=孙金伟 31-OPL清单开发 https://www.tapd.cn/63554897/s/1392112 单元测试
     * 测试默认分页参数：pageNo = null, pageSize = null
     */
    @Test
    void testQueryPageList_DefaultPageParams() {
        // Arrange
        SopHomePage sopHomePage = new SopHomePage();
        when(sopHomePageService.getQueryPageData(any(Page.class), eq(sopHomePage), eq(request)))
                .thenReturn(mock(IPage.class));

        // Act
        Result<IPage<SopHomePage>> result = sopHomePageController.queryPageList(
                sopHomePage, null, null, null, null, null, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(sopHomePageService, times(1)).getQueryPageData(argThat(page -> page.getCurrent() == 1 && page.getSize() == 10), eq(sopHomePage), eq(request));
    }

    /**
     * --story=1057860 --user=孙金伟 31-OPL诊断计划开发 https://www.tapd.cn/63554897/s/1392113 单元测试
     * 测试自定义分页参数：pageNo = 2, pageSize = 5
     */
    @Test
    void testQueryPageList_CustomPageParams() {
        // Arrange
        SopHomePage sopHomePage = new SopHomePage();
        IPage<SopHomePage> mockPage = mock(IPage.class);
        when(sopHomePageService.getQueryPageData(any(Page.class), eq(sopHomePage), eq(request))).thenReturn(mockPage);

        // Act
        Result<IPage<SopHomePage>> result = sopHomePageController.queryPageList(
                sopHomePage, 2, 5, null, null, null, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(sopHomePageService, times(1)).getQueryPageData(argThat(page -> page.getCurrent() == 2 && page.getSize() == 5), eq(sopHomePage), eq(request));
    }

    /**
     * 测试带排序参数的查询
     */
    @Test
    void testQueryPageList_WithOrderParams() {
        // Arrange
        SopHomePage sopHomePage = new SopHomePage();
        IPage<SopHomePage> mockPage = mock(IPage.class);
        when(sopHomePageService.getQueryPageData(any(Page.class), eq(sopHomePage), eq(request))).thenReturn(mockPage);

        // Act
        Result<IPage<SopHomePage>> result = sopHomePageController.queryPageList(
                sopHomePage, 1, 10, "sopType", "asc", null, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(sopHomePageService, times(1)).getQueryPageData(any(Page.class), eq(sopHomePage), eq(request));
    }

    /**
     * 测试带 selectFields 参数的查询
     */
    @Test
    void testQueryPageList_WithSelectFields() {
        // Arrange
        SopHomePage sopHomePage = new SopHomePage();
        IPage<SopHomePage> mockPage = mock(IPage.class);
        when(sopHomePageService.getQueryPageData(any(Page.class), eq(sopHomePage), eq(request))).thenReturn(mockPage);

        // Act
        Result<IPage<SopHomePage>> result = sopHomePageController.queryPageList(
                sopHomePage, 1, 10, null, null, "sopNumber,sopDesc", request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(sopHomePageService, times(1)).getQueryPageData(any(Page.class), eq(sopHomePage), eq(request));
    }

    /**
     * 测试无参数调用
     */
    @Test
    void testQueryPageList_NoParams() {
        // Arrange
        SopHomePage sopHomePage = new SopHomePage();
        IPage<SopHomePage> mockPage = mock(IPage.class);
        when(sopHomePageService.getQueryPageData(any(Page.class), eq(sopHomePage), eq(request))).thenReturn(mockPage);

        // Act
        Result<IPage<SopHomePage>> result = sopHomePageController.queryPageList(
                sopHomePage, null, null, null, null, null, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(sopHomePageService, times(1)).getQueryPageData(any(Page.class), eq(sopHomePage), eq(request));
    }
}
