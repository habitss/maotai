package com.keytech.modules.manage.check.service.impl;

import cn.hutool.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --bug=1025037 --user=孙金伟 燕京-中京-管理者检查清单-矩阵配置-检查频次月度检查，增加检查周期时间 https://www.tapd.cn/63554897/s/1379531 单元测试类
 * --story=1057426 --user=孙金伟 24-管理者检查清单新增触发条件 https://www.tapd.cn/63554897/s/1388337 单元测试
 */
class ManagerChecklistEditMainServiceImplTest {

    private ManagerChecklistEditMainServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManagerChecklistEditMainServiceImpl();
    }

    /**
     * getMothYear 方法测试
     */
    @Test
    void testGetMothYear() {
        JSONObject mockConfig = new JSONObject();
        
        // 设置当前日期为配置中的日期
        LocalDate today = LocalDate.now();
        String currentDay = today.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
        mockConfig.put("monDay1", currentDay);

        // case "90" 测试
        assertFalse(service.getMothYear("90", "6", null)); // 周六应返回false
        assertTrue(service.getMothYear("90", "1", null));  // 周一应返回true

        // case "91" 测试
        assertTrue(service.getMothYear("91", null, mockConfig)); // 配置匹配应返回true
        assertFalse(service.getMothYear("91", null, new JSONObject())); // 配置不匹配应返回false

        // case "95" 或其他无效类型
        assertFalse(service.getMothYear("95", null, null)); // 默认返回false
    }

    /**
     * compare 方法测试
     */
    @Test
    void testCompare() {
        JSONObject mockConfig = new JSONObject();
        
        // 设置当前日期
        LocalDate today = LocalDate.now();
        String currentDay = today.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));

        // 测试配置中包含当前日期的情况
        mockConfig.put("monDay1", currentDay);
        assertTrue(service.compare(mockConfig));

        // 测试配置中不包含当前日期的情况
        mockConfig.put("monDay1", "01-01");
        assertFalse(service.compare(mockConfig));

        // 测试所有monDay字段都不存在的情况
        JSONObject emptyConfig = new JSONObject();
        assertFalse(service.compare(emptyConfig));
    }
}
