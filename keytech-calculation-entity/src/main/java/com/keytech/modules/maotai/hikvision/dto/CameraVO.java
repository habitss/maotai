package com.keytech.modules.maotai.hikvision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 监控点(海康对"摄像头通道"的叫法)视图对象。
 * 字段取自海康 /artemis/api/resource/v1/cameras 返回的 list 元素;service 层从 HikvisionResponse.data 装配。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Schema(description = "监控点")
public class CameraVO {

    @Schema(description = "监控点唯一标识(取流/回放用)")
    private String cameraIndexCode;

    @Schema(description = "监控点名称")
    private String cameraName;

    @Schema(description = "监控点类型说明")
    private String cameraTypeName;

    @Schema(description = "通道类型说明")
    private String channelTypeName;

    @Schema(description = "所属区域唯一标识")
    private String regionIndexCode;

    @Schema(description = "所属编码设备唯一标识")
    private String encodeDevIndexCode;

    @Schema(description = "接入协议类型说明(如 HIK/GB28181/EHOME)")
    private String transTypeName;

    @Schema(description = "在线状态(部分版本返回:1在线 0离线)")
    private String status;

    @Schema(description = "是否已配录像(部分版本返回:1是 0否)")
    private String isRecorded;

    @Schema(description = "在线状态(独立 online 接口回填:1在线 0离线,null未查到)")
    private String online;
}
