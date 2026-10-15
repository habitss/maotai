import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/cop/copPoint/queryPageList',

  configList = '/cop/copConfig/queryPageList',
  deleteData = '/cop/copPoint/deleteData',
  save='/cop/copPoint/add',
  edit='/cop/copPoint/edit',

  find = '/cop/copPoint/queryById',

  importExcel = '/cop/copPoint/importExcel',

  exportXls = '/cop/copPoint/exportXls',
}

/**
 * 查询COP清洗点列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

export const getConfigList = (params) => {
  return defHttp.get({ url: Api.configList, params });
};

export const getById = (params) => {
  return defHttp.get({ url: Api.find, params });
};

/**
 * 通过主键删除COP清洗点数据
 */
export const deleteData = (id, handleSuccess) => {
    return defHttp.delete({url: Api.deleteData+'?id='+id}, {isTransformResponse: false}).then(() => {
      handleSuccess();
    });
}

/**
 * 保存或者更新COP清洗点
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;
