import { defHttp } from '/@/utils/http/axios';

enum Api {
  baseUrl = '/ems/kpiStruct',
  queryPageList = '/ems/kpiStruct/queryPageList',
  getKpiStrutTree = '/ems/kpiStruct/treeData',
  deleteOne = '/ems/kpiStruct/delete',
  save = '/ems/kpiStruct/add',
  edit = '/ems/kpiStruct/edit',
  deleteBatch = '/ems/kpiStruct/deleteBatch',
  importExcel = '/ems/kpiStruct/importExcel',
  exportXls = '/ems/kpiStruct/exportXls',
  queryAllCollectTag = '/ems/collectTag/listAll',
}

/**
 * KPI结构对节点列表接口
 * @param params
 */
export const getKpiStrutTree = (params) => defHttp.get({ url: Api.getKpiStrutTree, params });

/**
 * 全部采集点位列表接口
 * @param params
 */
export const listAllCollectTag = (params) => defHttp.get({ url: Api.queryAllCollectTag, params });

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
 * 查询单个KPI节点数据
 */
export const getOne = (id) => defHttp.get({ url: `${Api.baseUrl}/${id}` });

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.queryPageList, params });

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量删除
 * @param params
 * @param handleSuccess
 */
export const batchDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
};
