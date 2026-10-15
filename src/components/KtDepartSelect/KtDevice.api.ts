import { defHttp } from '/@/utils/http/axios';
enum Api {
  itemList = '/mdm/deviceType/queryPageList',
  // itemList = '/mdm/dict/queryPageList',
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
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });

/**
 * 字典配置列表
 * @param params
 */
// export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
