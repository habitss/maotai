import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/manage/check/executionRate/list'
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

