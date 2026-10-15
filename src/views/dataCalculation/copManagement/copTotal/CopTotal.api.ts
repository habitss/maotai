import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/cop/copSchedule/copStatistics',
  exportXls = '/cop/copSchedule/exportXls',
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  let url = Api.list ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}
