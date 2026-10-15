import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/meeting/meeting/meetingStatistics',

}

/**
 * 查询配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.post({ url: Api.list, params });
};


