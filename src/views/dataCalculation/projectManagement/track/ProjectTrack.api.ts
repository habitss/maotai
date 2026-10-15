import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/project/management/projectManagementDocument/queryAllList',
  edit = '/project/management/projectManagementDocument/edit',
  exportXls = '/project/management/projectManagementDocument/exportXls',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 修改数据
 * @param params
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.edit, params });
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;