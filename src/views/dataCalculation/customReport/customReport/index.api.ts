import { defHttp } from '/@/utils/http/axios';

enum Api {
  dictList = '/mes3_mdm/dictGroup/queryPageList',
  dictItemList = '/mes3_mdm/dict/queryPageList',
  userList = '/customReport/reportTemplate/getUserAll',
}

/**
 * 查询字典数据列表
 * @param params
 */
export const getDictList = (params) => {
  return defHttp.get({ url: Api.dictList, params });
};

/**
 * 查询字典选项数据列表
 * @param params
 */
export const getDictItemList = (params) => {
  return defHttp.get({ url: Api.dictItemList, params });
};

/**
 * 查询人员数据列表
 * @param params
 */
export const getUserList = (params) => {
  return defHttp.get({ url: Api.userList, params });
};