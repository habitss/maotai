import { defHttp } from '/@/utils/http/axios';

/** 能源看板聚合数据(假数据,period: today/week/month/quarter/year) */
export const getDashboard = (params: { period?: string }) => defHttp.get({ url: '/maotai/energyDashboard/data', params });
