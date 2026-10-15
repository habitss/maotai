package com.keytech.modules.practice.controller;

import com.keytech.modules.practice.entity.BestPracticeConfig;
import com.keytech.modules.practice.service.IBestPracticeConfigService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055312 --user=孙金伟 23-最佳实践优化处理 https://www.tapd.cn/63554897/s/1383726 单元测试
 */
class BestPracticeConfigControllerTest {

    @InjectMocks
    private BestPracticeConfigController controller; // 被测控制器

    @Mock
    private IBestPracticeConfigService bestPracticeConfigService; // 模拟服务层

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化 Mockito 注解
    }

    /**
     * 测试 add 方法：正常添加配置项
     */
    @Test
    void testAdd_Success() {
        // 准备数据
        BestPracticeConfig config = new BestPracticeConfig();
        config.setId("123456");
        config.setConfigName("测试配置");

        // 模拟 service 的 save 方法行为
        doNothing().when(bestPracticeConfigService).save(config);

        // 调用被测方法
        Result<String> result = controller.add(config);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        verify(bestPracticeConfigService, times(1)).save(config); // 验证 save 被调用一次
    }

    /**
     * 测试 add 方法：传入 null 参数
     */
    @Test
    void testAdd_NullInput() {
        // 模拟 service 的 save 方法行为（即使传入 null）
        doNothing().when(bestPracticeConfigService).save(null);

        // 调用被测方法
        Result<String> result = controller.add(null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        verify(bestPracticeConfigService, times(1)).save(null); // 验证 save 被调用一次
    }
}
