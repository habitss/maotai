import { defHttp } from '/@/utils/http/axios';
enum Api {
  list = '/mes3_mdm/dictGroup/queryPageList',
  itemList = '/mes3_mdm/dict/queryPageList',
}
/**
 * 导出api
 * @param params
 */
// export const getExportUrl = Api.exportXls;
/**
 * 导入api
 * @param params
 */
// export const getImportUrl = Api.importExcel;
/**
 * 字典列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 字典配置列表
 * @param params
 */
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
