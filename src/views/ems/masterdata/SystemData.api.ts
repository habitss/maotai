import { defHttp } from '/@/utils/http/axios';

enum Api {
  systDictUserList = '/ems/sys/dictUser',
}

/**
 * 获取系统用户下拉列表
 * @param params
 */
export const getSysDictUserList = (params) => defHttp.get({ url: Api.systDictUserList, params });
