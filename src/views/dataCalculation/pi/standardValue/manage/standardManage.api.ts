import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/pi/standardManager/queryPageList',
  addData = '/pi/standardManager/add',
  updateData = '/pi/standardManager/edit',
  deleteData = '/pi/standardManager/deleteData',
  fieldList = '/pi/piCustomizeTableField/listData',
  getMaxSort = '/pi/standardManager/getMaxSort',
  importExcel = '/pi/standardManager/importExcel',
  exportXls = '/pi/standardManager/exportXls',
  standardCopy = '/pi/standardManager/standardCopy'
}

/**
 * 查询PI标准值管理列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 添加PI标准值管理数据
 * @param params
 */
export const addData = (params) => {
  return defHttp.post({ url: Api.addData, params }, {isTransformResponse: false});
};

/**
 * 修改PI标准值管理数据
 * @param params
 */
export const updateData = (params) => {
  return defHttp.put({ url: Api.updateData, params }, {isTransformResponse: false});
};

/**
 * 通过主键删除PI标准值管理数据
 */
export const deleteData = (id) => {
  return defHttp.delete({url: Api.deleteData+'?id='+id}, {isTransformResponse: false});
}

/**
 * 查询基础数据字段
 * @param params
 */
export const getFieldList = (params) => {
  return defHttp.get({ url: Api.fieldList, params });
};

/**
 * 查询PI标准值管理最大排序值
 * @param params
 */
export const getMaxSort = (params) =>
  defHttp.get({url: Api.getMaxSort, params});

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;

/**
 * 标准复制
 */
export const standardCopy = (id) => {
  return defHttp.get({url: Api.standardCopy+'?id='+id}, {isTransformResponse: false});
}
