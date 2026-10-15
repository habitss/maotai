package com.keytech.modules.maotai.hikvision.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.hikvision.client.HikvisionClient;
import com.keytech.modules.maotai.hikvision.config.HikvisionProperties;
import com.keytech.modules.maotai.hikvision.dto.CameraPageVO;
import com.keytech.modules.maotai.hikvision.dto.CameraVO;
import com.keytech.modules.maotai.hikvision.dto.HikvisionResponse;
import com.keytech.modules.maotai.hikvision.dto.PlaybackCommand;
import com.keytech.modules.maotai.hikvision.dto.PreviewUrlVO;
import com.keytech.modules.maotai.hikvision.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控中心·海康视频 业务实现(转发海康 iSecure Center,不落库)。
 * client 返回失败时透传 msg;成功时从 data(JsonNode) 按需转 VO。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorServiceImpl implements MonitorService {

    private final HikvisionClient hikvisionClient;
    private final HikvisionProperties hikvisionProperties;
    private final ObjectMapper objectMapper;

    /** 模拟相机清单(indexCode=前端 public/videos/ 下的 MP4 文件名;region=树形分组的区域名),host 未配置时启用 */
    private static final List<String[]> MOCK_CAMERAS = List.of(
            new String[]{"cam01", "1#卸车区", "收储区"}, new String[]{"cam02", "2#卸车区", "收储区"},
            new String[]{"cam08", "地磅房", "收储区"},
            new String[]{"cam03", "中转罐区", "酒库区"}, new String[]{"cam04", "勾调车间", "酒库区"},
            new String[]{"cam05", "成品罐区", "酒库区"},
            new String[]{"cam06", "灌装车间", "包装区"}, new String[]{"cam10", "包装车间", "包装区"},
            new String[]{"cam12", "制酒车间", "制酒区"}, new String[]{"cam09", "动力车间", "制酒区"},
            new String[]{"cam11", "机修车间", "生产检修区"}, new String[]{"cam13", "焊接车间", "生产检修区"},
            new String[]{"cam14", "装配车间", "生产检修区"}, new String[]{"cam15", "生产车间", "生产检修区"},
            new String[]{"cam07", "园区大门", "周界与出入口"}, new String[]{"cam16", "厂区夜览", "周界与出入口"});

    /** 海康未配置(host 空)自动进 mock:相机清单/取流地址走模拟,配置 host 后自然切回真机,前端零改动 */
    private boolean isMock() {
        return StrUtil.isBlank(hikvisionProperties.getHost());
    }

    @Override
    public Result<CameraPageVO> listCameras(Integer pageNo, Integer pageSize) {
        if (isMock()) {
            return mockCameras(pageNo, pageSize);
        }
        HikvisionResponse resp = hikvisionClient.getCameras(pageNo, pageSize);
        if (!resp.isSuccess()) {
            return Result.error("获取监控点列表失败: " + resp.getMsg());
        }
        JsonNode data = resp.getData();
        if (data == null) {
            return Result.error("海康返回数据为空");
        }
        CameraPageVO vo = new CameraPageVO();
        vo.setTotal(data.path("total").asLong(0L));
        vo.setPageNo(data.path("pageNo").asInt(pageNo == null ? 1 : pageNo));
        vo.setPageSize(data.path("pageSize").asInt(pageSize == null ? 20 : pageSize));
        List<CameraVO> list = new ArrayList<>();
        JsonNode arr = data.path("list");
        if (arr.isArray()) {
            for (JsonNode item : arr) {
                // convertValue 不抛 checked;转换单元素失败跳过该监控点,不影响整体
                try {
                    list.add(objectMapper.convertValue(item, CameraVO.class));
                } catch (Exception e) {
                    log.warn("[HIK] 监控点字段转换失败,跳过: {}", item.path("cameraIndexCode").asText(), e);
                }
            }
        }
        fillOnline(list);
        vo.setList(list);
        return Result.OK(vo);
    }

    /** 批量查在线状态回填到 CameraVO.online;查询失败降级为 null,不影响列表 */
    private void fillOnline(List<CameraVO> list) {
        if (list.isEmpty()) return;
        List<String> codes = new ArrayList<>();
        for (CameraVO c : list) {
            if (StrUtil.isNotBlank(c.getCameraIndexCode())) {
                codes.add(c.getCameraIndexCode());
            }
        }
        if (codes.isEmpty()) return;
        try {
            HikvisionResponse resp = hikvisionClient.getOnlineStatus(codes);
            if (!resp.isSuccess() || resp.getData() == null) return;
            Map<String, String> codeOnline = new HashMap<>();
            JsonNode arr = resp.getData().path("list");
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    codeOnline.put(n.path("cameraIndexCode").asText(),
                            n.path("online").asText(null));
                }
            }
            for (CameraVO c : list) {
                c.setOnline(codeOnline.get(c.getCameraIndexCode()));
            }
        } catch (Exception e) {
            log.warn("[HIK] 在线状态查询失败,跳过(不影响列表)", e);
        }
    }

    @Override
    public Result<PreviewUrlVO> getPreviewUrl(String cameraIndexCode) {
        if (isMock()) {
            return Result.OK(mockUrl(cameraIndexCode));
        }
        HikvisionResponse resp = hikvisionClient.getPreviewUrl(cameraIndexCode);
        if (!resp.isSuccess()) {
            return Result.error("获取取流URL失败: " + resp.getMsg());
        }
        return Result.OK(buildPreviewUrlVO(resp.getData()));
    }

    @Override
    public Result<PreviewUrlVO> getPlaybackUrl(PlaybackCommand cmd) {
        if (isMock()) {
            // 模拟回放:同一循环片(模拟数据无时间轴,时间范围仅接口透传)
            return Result.OK(mockUrl(cmd.getCameraIndexCode()));
        }
        HikvisionResponse resp = hikvisionClient.getPlaybackUrl(cmd);
        if (!resp.isSuccess()) {
            return Result.error("获取回放URL失败: " + resp.getMsg());
        }
        return Result.OK(buildPreviewUrlVO(resp.getData()));
    }

    private PreviewUrlVO buildPreviewUrlVO(JsonNode data) {
        PreviewUrlVO vo = new PreviewUrlVO();
        vo.setUrl(data == null ? null : data.path("url").asText(null));
        return vo;
    }

    // ===== mock(海康未配置时的模拟视频,MP4 由前端原生 video 播放) =====

    /** 相机清单:8 路模拟点,分页参数照常生效 */
    private Result<CameraPageVO> mockCameras(Integer pageNo, Integer pageSize) {
        log.info("[HIK] mock 模式(未配置 hikvision.host),返回 {} 路模拟相机", MOCK_CAMERAS.size());
        int page = pageNo == null ? 1 : pageNo;
        int size = pageSize == null || pageSize <= 0 ? 20 : pageSize;
        CameraPageVO vo = new CameraPageVO();
        vo.setTotal((long) MOCK_CAMERAS.size());
        vo.setPageNo(page);
        vo.setPageSize(size);
        List<CameraVO> list = new ArrayList<>();
        for (int i = (page - 1) * size; i < Math.min(page * size, MOCK_CAMERAS.size()); i++) {
            String[] m = MOCK_CAMERAS.get(i);
            CameraVO c = new CameraVO();
            c.setCameraIndexCode(m[0]);
            c.setCameraName(m[1]);
            // regionIndexCode 放区域中文名,前端按它分组成树(真海康时该字段是区域编码,树逻辑在前端同源)
            c.setRegionIndexCode(m[2]);
            c.setCameraTypeName("模拟摄像头");
            c.setOnline("1");
            list.add(c);
        }
        vo.setList(list);
        return Result.OK(vo);
    }

    /** 取流地址:相对路径(前端按页面 base 解析到 /mes3/videos/xx.mp4);indexCode 不在清单内也照给,前端 404 自见 */
    private PreviewUrlVO mockUrl(String cameraIndexCode) {
        PreviewUrlVO vo = new PreviewUrlVO();
        vo.setUrl("videos/" + cameraIndexCode + ".mp4");
        return vo;
    }
}
