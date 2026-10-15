package com.keytech.modules.maotai.hikvision;

import com.keytech.modules.maotai.hikvision.client.HikvisionClient;
import com.keytech.modules.maotai.hikvision.config.HikvisionProperties;
import com.keytech.modules.maotai.hikvision.dto.CameraPageVO;
import com.keytech.modules.maotai.hikvision.dto.CameraVO;
import com.keytech.modules.maotai.hikvision.dto.HikvisionResponse;
import com.keytech.modules.maotai.hikvision.dto.PlaybackCommand;
import com.keytech.modules.maotai.hikvision.dto.PreviewUrlVO;
import com.keytech.modules.maotai.hikvision.service.impl.MonitorServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 监控中心服务测试:host 未配置自动降级 mock(相机清单/取流/回放),配置后走海康客户端。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("监控中心 MonitorServiceImpl")
class MonitorServiceImplTest {

    @Mock
    private HikvisionClient hikvisionClient;
    @Mock
    private HikvisionProperties properties;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MonitorServiceImpl service;

    @BeforeEach
    void hostNotConfigured() {
        // 默认场景:海康未配置 → mock 模式
        when(properties.getHost()).thenReturn("");
    }

    // ===== mock 降级 =====

    @Test
    @DisplayName("mock降级:host 空→返回 16 路模拟相机,区域字段带出,全部在线")
    void mockCameras_returns16Cameras() {
        Result<CameraPageVO> r = service.listCameras(1, 100);
        assertTrue(r.isSuccess());
        List<CameraVO> list = r.getResult().getList();
        assertEquals(16, list.size());
        assertEquals(16L, r.getResult().getTotal());
        assertTrue(list.stream().allMatch(c -> "1".equals(c.getOnline())), "模拟相机全部在线");
        assertTrue(list.stream().allMatch(c -> "模拟摄像头".equals(c.getCameraTypeName())));
        // 区域分组字段带出(树形导航依赖)
        assertNotNull(list.get(0).getRegionIndexCode());
    }

    @Test
    @DisplayName("mock降级:分页参数生效(第2页×10条=剩余6条)")
    void mockCameras_pagingWorks() {
        Result<CameraPageVO> r = service.listCameras(2, 10);
        assertEquals(6, r.getResult().getList().size());
        assertEquals(16L, r.getResult().getTotal());
        assertEquals(2, r.getResult().getPageNo());
    }

    @Test
    @DisplayName("mock降级:取流 URL 为前端相对路径 videos/camXX.mp4")
    void mockPreviewUrl() {
        Result<PreviewUrlVO> r = service.getPreviewUrl("cam03");
        assertEquals("videos/cam03.mp4", r.getResult().getUrl());
    }

    @Test
    @DisplayName("mock降级:回放取流同 mock(时间参数透传不报错)")
    void mockPlaybackUrl() {
        PlaybackCommand cmd = new PlaybackCommand();
        cmd.setCameraIndexCode("cam07");
        cmd.setBeginTime("2026-09-22T00:00:00.000+08:00");
        cmd.setEndTime("2026-09-22T23:59:00.000+08:00");
        Result<PreviewUrlVO> r = service.getPlaybackUrl(cmd);
        assertEquals("videos/cam07.mp4", r.getResult().getUrl());
    }

    // ===== 真机路径(配置了 host) =====

    @Test
    @DisplayName("真机:配置 host 后走海康客户端,客户端失败透传错误")
    void realClient_cameraListFailure() {
        when(properties.getHost()).thenReturn("10.1.1.1:443");
        HikvisionResponse resp = mock(HikvisionResponse.class);
        when(resp.isSuccess()).thenReturn(false);
        when(resp.getMsg()).thenReturn("artemis 不可达");
        when(hikvisionClient.getCameras(anyInt(), anyInt())).thenReturn(resp);

        Result<CameraPageVO> r = service.listCameras(1, 20);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("获取监控点列表失败"), r.getMessage());
    }

    @Test
    @DisplayName("真机:客户端成功时解析 data.url 返回")
    void realClient_previewUrlSuccess() {
        when(properties.getHost()).thenReturn("10.1.1.1:443");
        HikvisionResponse resp = mock(HikvisionResponse.class);
        when(resp.isSuccess()).thenReturn(true);
        com.fasterxml.jackson.databind.node.ObjectNode data = new ObjectMapper().createObjectNode();
        data.put("url", "wss://hik/stream/abc");
        when(resp.getData()).thenReturn(data);
        when(hikvisionClient.getPreviewUrl(anyString())).thenReturn(resp);

        Result<PreviewUrlVO> r = service.getPreviewUrl("cam01");
        assertTrue(r.isSuccess());
        assertEquals("wss://hik/stream/abc", r.getResult().getUrl());
    }

    @Test
    @DisplayName("mock降级分支不触达海康客户端(零外呼)")
    void mockMode_neverCallsClient() {
        service.listCameras(1, 20);
        service.getPreviewUrl("cam01");
        verifyNoInteractions(hikvisionClient);
    }
}
