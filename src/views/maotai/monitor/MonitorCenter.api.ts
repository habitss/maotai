import { defHttp } from '/@/utils/http/axios';

/** 取流 URL 返回(实时/回放通用) */
export interface PreviewUrlVO {
  url: string;
}

/** 监控点(摄像头通道) */
export interface CameraVO {
  cameraIndexCode: string;
  cameraName: string;
  cameraTypeName?: string;
  channelTypeName?: string;
  regionIndexCode?: string;
  encodeDevIndexCode?: string;
  transTypeName?: string;
  status?: string;
  isRecorded?: string;
  /** 在线状态(独立接口回填:1在线 0离线;为空时回退 status) */
  online?: string;
}

/** 监控点分页结果 */
export interface CameraPageVO {
  total: number;
  pageNo: number;
  pageSize: number;
  list: CameraVO[];
}

/** 监控点分页列表(转发海康 /artemis/api/resource/v1/cameras) */
export const listCameras = (params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get<CameraPageVO>({ url: '/maotai/hikvision/cameras', params });

/** 实时取流 URL(转发海康 previewURLs;ws 协议,配合 h5player,5 分钟有效) */
export const getPreviewUrl = (cameraIndexCode: string) =>
  defHttp.get<PreviewUrlVO>({ url: `/maotai/hikvision/cameras/${cameraIndexCode}/previewUrl` });

/** 录像回放取流 URL(转发海康 playbackURLs) */
export const getPlaybackUrl = (data: { cameraIndexCode: string; beginTime: string; endTime: string }) =>
  defHttp.post<PreviewUrlVO>({ url: '/maotai/hikvision/cameras/playbackUrl', data });
