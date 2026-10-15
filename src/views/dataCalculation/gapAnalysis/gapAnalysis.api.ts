import { defHttp } from '/@/utils/http/axios';

enum Api {
  queryData = '/kpi/gapAnalysis/queryData',
  add = '/kpi/gapAnalysis/add',
  listKpi = '/kpi/gapAnalysis/monthGapValue',
}
/**
 * 列表接口
 * @param params
 */
export const queryData = (params) =>
  defHttp.get({url: Api.queryData, params});

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.add, params });
}

/* 差距分析页面，kpi列表  */
export const listKpi = (params) =>
  defHttp.post({url: Api.listKpi, params});