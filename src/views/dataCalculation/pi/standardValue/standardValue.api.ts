import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/pi/piCustomizeTable/listData',
  getStandardFieldByTableId = '/pi/piCustomizeTableField/getStandardFieldByTableId',
  copyStandard = '/pi/standardMaintenance/copyStandard'
}

/**
 * 查询PI标准值配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};


/**
 * 获取报表拥有的字段(标记是否配置标准)
 * @param params
 */
export const getStandardFieldByTableId = (params) => {
  return defHttp.get({ url: Api.getStandardFieldByTableId, params });
};

/**
 * 保存或者更新
 * @param params
 */
export const copyStandard = (params) => {
  return defHttp.post({ url: Api.copyStandard, params }, { isTransformResponse: false });
}