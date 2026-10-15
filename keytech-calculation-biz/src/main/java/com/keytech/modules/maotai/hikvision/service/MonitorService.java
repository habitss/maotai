package com.keytech.modules.maotai.hikvision.service;

import com.keytech.modules.maotai.hikvision.dto.CameraPageVO;
import com.keytech.modules.maotai.hikvision.dto.PlaybackCommand;
import com.keytech.modules.maotai.hikvision.dto.PreviewUrlVO;
import org.jeecg.common.api.vo.Result;

/**
 * 监控中心·海康视频 业务接口(转发海康 iSecure Center,不落库)。
 * 调 HikvisionClient 取海康响应,把 data(JsonNode) 转成业务 VO,成败统一包装成 Result。
 *
 * @author maotai
 * @since 2026-08-10
 */
public interface MonitorService {

    /** 监控点分页列表(转发海康 /artemis/api/resource/v1/cameras) */
    Result<CameraPageVO> listCameras(Integer pageNo, Integer pageSize);

    /** 实时取流 URL(转发海康 previewURLs,protocol=ws) */
    Result<PreviewUrlVO> getPreviewUrl(String cameraIndexCode);

    /** 录像回放取流 URL(转发海康 playbackURLs) */
    Result<PreviewUrlVO> getPlaybackUrl(PlaybackCommand cmd);
}
