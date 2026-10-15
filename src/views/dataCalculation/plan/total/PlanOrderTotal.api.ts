import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/plan/planOrder/planOrderStatistics',
  resultList = '/plan/planOrder/planOrderResultStatistics',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  return defHttp.post({ url: Api.list, params });
};

/**
 * 填报结果列表接口
 * @param params
 */
export const resultList = (params) => {
  return defHttp.post({ url: Api.resultList, params });
};
