import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/mdm3/mdmTaskItem/queryPageList',
  save = '/mdm3/mdmTaskItem/save',
  deletionBatch = '/mdm3/mdmTaskItem/deletionBatch',
  editBatch = '/mdm3/mdmTaskItem/edit',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 保存
 * @param params
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.save, params });
}

/**
 * 删除
 * @param params
 */
export const deleteData = (params, handleSuccess) => {
  return defHttp.post({ url: Api.deletionBatch, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}

/**
 * 保存排序
 * @param params
 */
export const editBatch = (params) => {
  return defHttp.post({ url: Api.editBatch, params });
}
