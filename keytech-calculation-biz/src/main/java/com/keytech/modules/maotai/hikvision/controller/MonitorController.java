package com.keytech.modules.maotai.hikvision.controller;

import com.keytech.modules.maotai.hikvision.dto.CameraPageVO;
import com.keytech.modules.maotai.hikvision.dto.PlaybackCommand;
import com.keytech.modules.maotai.hikvision.dto.PreviewUrlVO;
import com.keytech.modules.maotai.hikvision.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控中心·海康视频(对接 iSecure Center,出站取流,不落库)。
 * 平台内部端点 /maotai/hikvision/**(Shiro JWT);host/appKey/appSecret 联调时填配置后端到端。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Tag(name = "监控中心·海康视频")
@RestController
@RequestMapping("/maotai/hikvision")
@Slf4j
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    /**
     * 监控点分页列表(转发海康 /artemis/api/resource/v1/cameras)。
     * @param pageNo   页码(从 1 起,默认 1)
     * @param pageSize 每页(默认 20,海康上限 1000)
     * @return Result&lt;CameraPageVO&gt;(含 total/list)
     */
    @AutoLog(value = "监控中心-监控点列表")
    @Operation(summary = "监控点分页列表(转发海康)",
            description = "转发海康 /artemis/api/resource/v1/cameras;不落库,实时查海康")
    @GetMapping("/cameras")
    public Result<CameraPageVO> listCameras(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return monitorService.listCameras(pageNo, pageSize);
    }

    /**
     * 实时取流 URL(转发海康 previewURLs)。
     * <p>protocol 默认 ws,配合海康 H5 无插件播放器;URL 有效期 5 分钟,过期前端重取。
     * @param cameraIndexCode 监控点唯一标识(从列表接口取)
     * @return Result&lt;PreviewUrlVO&gt;(含 url)
     */
    @AutoLog(value = "监控中心-实时取流")
    @Operation(summary = "实时取流URL(转发海康,ws协议)",
            description = "转发海康 /artemis/api/video/v2/cameras/previewURLs;protocol=ws 配合 H5 播放器;URL 5 分钟有效")
    @GetMapping("/cameras/{cameraIndexCode}/previewUrl")
    public Result<PreviewUrlVO> getPreviewUrl(@PathVariable String cameraIndexCode) {
        return monitorService.getPreviewUrl(cameraIndexCode);
    }

    /**
     * 录像回放取流 URL(转发海康 playbackURLs)。
     * @param cmd cameraIndexCode + beginTime + endTime(ISO8601 含时区)
     * @return Result&lt;PreviewUrlVO&gt;(含 url)
     */
    @AutoLog(value = "监控中心-回放取流")
    @Operation(summary = "录像回放取流URL(转发海康)",
            description = "转发海康 /artemis/api/video/v2/cameras/playbackURLs;时间 ISO8601 含时区")
    @PostMapping("/cameras/playbackUrl")
    public Result<PreviewUrlVO> getPlaybackUrl(@Valid @RequestBody PlaybackCommand cmd) {
        return monitorService.getPlaybackUrl(cmd);
    }
}
