package com.keytech.modules.manage.check.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import com.keytech.modules.manage.check.service.IManageCheckListService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055307 --user=孙金伟 23-管理者检查清单优化处理 https://www.tapd.cn/63554897/s/1380498 单元测试类
 */
class ManageCheckListControllerTest {

    @InjectMocks
    private ManageCheckListController manageCheckListController;

    @Mock
    private IManageCheckListService manageCheckListService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常流程：服务层返回正常数据
     */
    @Test
    void testQueryPageList_Success() {
        // 准备测试数据
        ManageCheckList manageCheckList = new ManageCheckList();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 创建模拟的分页结果
        @SuppressWarnings("unchecked")
        IPage<ManageCheckList> pageMock = mock(IPage.class);

        // 设置服务层方法的模拟行为
        when(manageCheckListService.queryPageList(eq(manageCheckList), eq(pageNo), eq(pageSize)))
                .thenReturn(pageMock);

        // 调用被测方法
        Result<IPage<ManageCheckList>> result = manageCheckListController.queryPageList(
                manageCheckList, pageNo, pageSize, null, null, null, httpServletRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(Result.OK().getCode(), result.getCode());
        assertEquals(pageMock, result.getResult());

        // 验证服务层方法是否被正确调用
        verify(manageCheckListService, times(1)).queryPageList(eq(manageCheckList), eq(pageNo), eq(pageSize));
    }

    /**
     * 测试异常流程：服务层抛出异常
     */
    @Test
    void testQueryPageList_Exception() {
        // 准备测试数据
        ManageCheckList manageCheckList = new ManageCheckList();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 设置服务层方法抛出异常
        when(manageCheckListService.queryPageList(eq(manageCheckList), eq(pageNo), eq(pageSize)))
                .thenThrow(new RuntimeException("数据库异常"));

        // 调用被测方法
        Result<IPage<ManageCheckList>> result = manageCheckListController.queryPageList(
                manageCheckList, pageNo, pageSize, null, null, null, httpServletRequest);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("数据库异常"));

        // 验证服务层方法是否被正确调用
        verify(manageCheckListService, times(1)).queryPageList(eq(manageCheckList), eq(pageNo), eq(pageSize));
    }
}
