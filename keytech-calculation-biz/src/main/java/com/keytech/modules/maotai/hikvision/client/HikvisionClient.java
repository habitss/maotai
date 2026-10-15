package com.keytech.modules.maotai.hikvision.client;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.keytech.modules.maotai.hikvision.config.HikvisionProperties;
import com.keytech.modules.maotai.hikvision.dto.HikvisionResponse;
import com.keytech.modules.maotai.hikvision.dto.PlaybackCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 海康 iSecure Center 客户端(artemis 网关,AK/SK HMAC 签名由官方 SDK 封装)。
 * 与勾储MES 的 MesClient(Hutool 直发)不同:海康要签名,故底层用 ArtemisHttpUtil.doPostStringArtemis。
 * 容错同出站标准:SDK 异常/空响应→构造失败响应返回(不抛、不挂主流程)。
 * 注:artemis SDK 默认超时 10s(Constants.DEFAULT_TIMEOUT),connectTimeoutMs/readTimeoutMs 预留待 SDK 版本支持再传。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HikvisionClient {

    private final HikvisionProperties properties;
    private final ObjectMapper objectMapper;

    private static final String CAMERAS_PATH = "/artemis/api/resource/v1/cameras";
    private static final String PREVIEW_URLS_PATH = "/artemis/api/video/v2/cameras/previewURLs";
    private static final String PLAYBACK_URLS_PATH = "/artemis/api/video/v2/cameras/playbackURLs";
    private static final String ONLINE_PATH = "/artemis/api/nms/v1/online/camera/get";

    /** 监控点分页列表(/artemis/api/resource/v1/cameras) */
    public HikvisionResponse getCameras(Integer pageNo, Integer pageSize) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pageNo", pageNo == null ? 1 : pageNo);
        body.put("pageSize", pageSize == null ? 20 : pageSize);
        return post(CAMERAS_PATH, body);
    }

    /** 实时取流 URL(/artemis/api/video/v2/cameras/previewURLs,protocol=ws 配合 H5 播放器) */
    public HikvisionResponse getPreviewUrl(String cameraIndexCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cameraIndexCode", cameraIndexCode);
        body.put("streamType", properties.getStreamType());
        body.put("protocol", properties.getStreamProtocol());
        body.put("transmode", properties.getTransmode());
        return post(PREVIEW_URLS_PATH, body);
    }

    /** 录像回放取流 URL(/artemis/api/video/v2/cameras/playbackURLs) */
    public HikvisionResponse getPlaybackUrl(PlaybackCommand cmd) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cameraIndexCode", cmd.getCameraIndexCode());
        body.put("beginTime", cmd.getBeginTime());
        body.put("endTime", cmd.getEndTime());
        body.put("protocol", properties.getStreamProtocol());
        body.put("transmode", properties.getTransmode());
        return post(PLAYBACK_URLS_PATH, body);
    }

    /** 批量查监控点在线状态(/artemis/api/nms/v1/online/camera/get,入参 cameraIndexCodes 数组,返回 online 1=在线 0=离线) */
    public HikvisionResponse getOnlineStatus(List<String> cameraIndexCodes) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cameraIndexCodes", cameraIndexCodes);
        return post(ONLINE_PATH, body);
    }

    private HikvisionResponse post(String artemisPath, Object body) {
        // artemis SDK 1.1.3:ArtemisConfig 用静态字段配 host/appKey/appSecret(全应用唯一,每次设幂等)
        ArtemisConfig.host = properties.getHost();
        ArtemisConfig.appKey = properties.getAppKey();
        ArtemisConfig.appSecret = properties.getAppSecret();
        // SDK path Map 的 key 为 scheme(需带 "://"),value 为 artemis 接口路径
        String schemeKey = properties.getScheme().endsWith("://")
                ? properties.getScheme() : properties.getScheme() + "://";
        Map<String, String> path = new HashMap<>(2);
        path.put(schemeKey, artemisPath);
        String json = toJson(body);
        log.info("[HIK] POST {} | host={} | body={}", artemisPath, properties.getHost(), json);
        try {
            String resp = ArtemisHttpUtil.doPostStringArtemis(path, json, null, "application/json", null);
            log.info("[HIK] resp body={}", resp);
            if (StrUtil.isBlank(resp)) {
                return HikvisionResponse.fail("海康响应为空");
            }
            return objectMapper.readValue(resp, HikvisionResponse.class);
        } catch (Exception e) {
            log.error("[HIK] 请求海康异常 path={}", artemisPath, e);
            return HikvisionResponse.fail("请求海康异常: " + e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("序列化海康请求失败: " + obj.getClass().getSimpleName(), e);
        }
    }
}
