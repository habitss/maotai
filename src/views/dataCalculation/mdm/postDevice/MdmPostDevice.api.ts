import { defHttp } from '/@/utils/http/axios';
enum Api {
  list = '/mdm3/mdmPostDevice/queryPageList',
  save = '/mdm3/mdmPostDevice/add',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 保存或者更新
 * @param params
 */
export const add = (params) => {
  return defHttp.post({ url: Api.save, params });
}
