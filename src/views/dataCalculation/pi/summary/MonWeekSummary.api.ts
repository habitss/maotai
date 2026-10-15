import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/pi/consistencyIndex/getPiMonWeekSummary',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params });
};