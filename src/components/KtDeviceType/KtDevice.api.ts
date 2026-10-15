import { defHttp } from '/@/utils/http/axios';
enum Api {
  deviceTypeList = '/mes3_mdm/deviceType/queryPageList',
  deviceList = '/mes3_mdm/device/queryPageList',
  // itemList = '/mes3_mdm/dict/queryPageList',
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
export const deviceTypeList = (params) => defHttp.get({ url: Api.deviceTypeList, params });

/**
 * 获取设备列表接口
 * @param params
 */
export const deviceList = (params) => defHttp.get({ url: Api.deviceList, params });

/**
 * 字典配置列表
 * @param params
 */
// export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
