package com.keytech.modules.alarm.controller;

import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import com.keytech.modules.alarm.service.IAlarmApiAppService;
import com.keytech.modules.alarm.service.IAlarmApiPermissionService;
import com.keytech.modules.calculation.KeytechCalculationApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 报警接收类测试用例
 * @author yepeng
 * @since 2026.02.05
 */
@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class AlarmReceiveControllerAuthReceiveTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAlarmApiAppService alarmApiAppService;

    @MockBean
    private IAlarmApiPermissionService alarmApiPermissionService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试授权逻辑 - 请求头不完整
     */
    @Test
    void testAuthReceiveMissingHeaders() throws Exception {
        // 构造请求参数
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-20250305-181522-0001");
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().toString());

        // 构建请求
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/alarm/auth/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceSystem\":\"EMS\",\"requestId\":\"EMS-20250305-181522-0001\",\"alarmPiCode\":\"APAC-CN93223001-0801_AggreAlr24-16\",\"alarmTime\":\"" + LocalDateTime.now() + "\"}");

        // 执行请求并验证结果
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("40101"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("请求头不完整"));
    }

    /**
     * 测试授权逻辑 - appId 不存在
     */
    @Test
    void testAuthReceiveAppIdNotFound() throws Exception {
        // 构造请求参数
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-20250305-181522-0001");
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().toString());

        // 模拟 appId 不存在
        when(alarmApiAppService.getByAppId(anyString())).thenReturn(null);

        // 构建请求
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/alarm/auth/receive")
                .header("X-App-Id", "test-app")
                .header("X-Timestamp", System.currentTimeMillis())
                .header("X-Nonce", "test-nonce")
                .header("X-Sign", "test-sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceSystem\":\"EMS\",\"requestId\":\"EMS-20250305-181522-0001\",\"alarmPiCode\":\"APAC-CN93223001-0801_AggreAlr24-16\",\"alarmTime\":\"" + LocalDateTime.now() + "\"}");

        // 执行请求并验证结果
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("40101"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("appId不存在"));
    }

    /**
     * 测试授权逻辑 - app 已停用
     */
    @Test
    void testAuthReceiveAppDisabled() throws Exception {
        // 构造请求参数
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-20250305-181522-0001");
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().toString());

        // 模拟 app 已停用
        AlarmApiApp app = new AlarmApiApp();
        app.setAppId("test-app");
        app.setStatus(0); // 0 表示停用
        when(alarmApiAppService.getByAppId(anyString())).thenReturn(app);

        // 构建请求
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/alarm/auth/receive")
                .header("X-App-Id", "test-app")
                .header("X-Timestamp", System.currentTimeMillis())
                .header("X-Nonce", "test-nonce")
                .header("X-Sign", "test-sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceSystem\":\"EMS\",\"requestId\":\"EMS-20250305-181522-0001\",\"alarmPiCode\":\"APAC-CN93223001-0801_AggreAlr24-16\",\"alarmTime\":\"" + LocalDateTime.now() + "\"}");

        // 执行请求并验证结果
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("40102"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("app已停用"));
    }

    /**
     * 测试授权逻辑 - 重放请求
     */
    @Test
    void testAuthReceiveReplayRequest() throws Exception {
        // 构造请求参数
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-20250305-181522-0001");
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().toString());

        // 模拟 app 存在且可用
        AlarmApiApp app = new AlarmApiApp();
        app.setAppId("test-app");
        app.setStatus(1); // 1 表示启用
        app.setAppSecret("test-secret");
        when(alarmApiAppService.getByAppId(anyString())).thenReturn(app);

        // 模拟 nonce 已存在
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // 构建请求
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/alarm/auth/receive")
                .header("X-App-Id", "test-app")
                .header("X-Timestamp", System.currentTimeMillis())
                .header("X-Nonce", "test-nonce")
                .header("X-Sign", "test-sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceSystem\":\"EMS\",\"requestId\":\"EMS-20250305-181522-0001\",\"alarmPiCode\":\"APAC-CN93223001-0801_AggreAlr24-16\",\"alarmTime\":\"" + LocalDateTime.now() + "\"}");

        // 执行请求并验证结果
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("40105"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("重放请求"));
    }

    /**
     * 测试授权逻辑 - 无接口权限
     */
    @Test
    void testAuthReceiveNoPermission() throws Exception {
        // 构造请求参数
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-20250305-181522-0001");
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().toString());

        // 模拟 app 存在且可用
        AlarmApiApp app = new AlarmApiApp();
        app.setAppId("test-app");
        app.setStatus(1); // 1 表示启用
        app.setAppSecret("test-secret");
        when(alarmApiAppService.getByAppId(anyString())).thenReturn(app);

        // 模拟 nonce 不存在
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(Mockito.mock(org.springframework.data.redis.core.ValueOperations.class));

        // 模拟无接口权限
        when(alarmApiPermissionService.hasPermission(anyString(), anyString())).thenReturn(false);

        // 构建请求
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/alarm/auth/receive")
                .header("X-App-Id", "test-app")
                .header("X-Timestamp", System.currentTimeMillis())
                .header("X-Nonce", "test-nonce")
                .header("X-Sign", "test-sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceSystem\":\"EMS\",\"requestId\":\"EMS-20250305-181522-0001\",\"alarmPiCode\":\"APAC-CN93223001-0801_AggreAlr24-16\",\"alarmTime\":\"" + LocalDateTime.now() + "\"}");

        // 执行请求并验证结果
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("40301"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("无接口权限"));
    }

}

