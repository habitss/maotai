import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/plan/planOrder/queryPageList',
  resultList = '/plan/planOrderResult/queryByOrderId',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 填报结果列表接口
 * @param params
 */
export const resultList = (params) =>
  defHttp.get({ url: Api.resultList, params });