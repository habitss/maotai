import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/customReport/reportTemplate/queryPageList',
  addData = '/customReport/reportTemplate/add',
  updateData = '/customReport/reportTemplate/edit',
  deleteData = '/customReport/reportTemplate/deleteData',
  getById = '/customReport/reportTemplate/getById',
}

/**
 * 查询模板配置数据列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 添加模板配置数据
 * @param params
 */
export const addData = (params) => {
  return defHttp.post({ url: Api.addData, params });
};

/**
 * 修改模板配置数据
 * @param params
 */
export const updateData = (params) => {
  return defHttp.put({ url: Api.updateData, params });
};

/**
 * 通过主键删除模板配置数据
 */

export const deleteData = (params, handleSuccess) => {
  return defHttp.delete({url: Api.deleteData, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 根据id查询
 * @param id
 */
export const getById = (id) => {
  return defHttp.get({ url: Api.getById+'?id='+id });
};