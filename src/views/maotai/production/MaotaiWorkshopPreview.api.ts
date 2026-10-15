import { defHttp } from '/@/utils/http/axios';

/** 4 车间完整数据(假数据) */
export const getWorkshopList = () => defHttp.get({ url: '/maotai/workshopPreview/list' });

/** 单个车间数据(假数据) */
export const getWorkshopData = (params: { workshopIndex: number }) =>
  defHttp.get({ url: '/maotai/workshopPreview/data', params });
