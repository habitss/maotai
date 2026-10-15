import { defHttp } from '/@/utils/http/axios';

enum Api {
  headerList = '/customReport/customReportField/queryHeaderByTemplateId',
  list = '/customReport/customReportField/queryData',
  addData = '/customReport/fuillIn/add',
  updateData = '/customReport/fuillIn/edit',
  deleteData = '/customReport/fuillIn/deleteData',
}

/**
 * 查询模板关联数据列表
 * @param params
 */
export const getHeaderList = (params) => {
  return defHttp.get({ url: Api.headerList, params });
};

/**
 * 查询模板关联数据列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 添加模板填报数据
 * @param params
 */
export const addData = (params) => {
  return defHttp.post({ url: Api.addData, params });
};

/**
 * 修改模板填报数据
 * @param params
 */
export const updateData = (params) => {
  return defHttp.put({ url: Api.updateData, params });
};

/**
 * 通过主键删除模板填报数据
 */
export const deleteData = (params) => {
  return defHttp.put({url: Api.deleteData, params});
}