import { defHttp } from '/@/utils/http/axios';

enum Api {
  baseUrl = '/ems/kpiData',
  kpiTreeDataList = '/ems/kpiData/treeList',
  kpiFilterDataList = '/ems/kpiData/filterList',
  getOrsHourUsageList = '/ems/orsdataHourUsage/list',
  getOrsDayUsageList = '/ems/orsdataDayUsage/list',
  getOrsMtdUsageList = '/ems/orsdataDayUsage/listMtd',
  getOrsYtdUsageList = '/ems/orsdataDayUsage/listYtd',
  getCollectTagByStructId = '/ems/kpiTag/getCollectTagByStructId'
}

/**
 * 获取树及点位数据
 * @param params
 */
export const getKpiTreeDataList = (params) => {
  return defHttp.post({ url: Api.kpiTreeDataList, params });
};

/**
 * 查询指定能源类型指定层级的所有点位数据
 * @param params
 */
export const getKpiFilterDataList = (params) => {
  return defHttp.post({ url: Api.kpiFilterDataList, params });
};

/**
 * 查询中间表小时数据
 * @param params
 */
export const getOrsHourUsageList = (params) => {
  return defHttp.post({ url: Api.getOrsHourUsageList, params });
};

/**
 * 查询中间表天数据
 * @param params
 */
export const getOrsDayUsageList = (params) => {
  return defHttp.post({ url: Api.getOrsDayUsageList, params });
};

/**
 * 查询中间表月数据
 * @param params
 */
export const getOrsMtdUsageList = (params) => {
  return defHttp.post({ url: Api.getOrsMtdUsageList, params });
};

/**
 * 查询中间表年数据
 * @param params
 */
export const getOrsYtdUsageList = (params) => {
  return defHttp.post({ url: Api.getOrsYtdUsageList, params });
};


/**
 * 查询结构绑定的点位ID
 * @param params
 */
export const getCollectTagByStructId = (params) => {
  return defHttp.get({ url: Api.getCollectTagByStructId, params });
};