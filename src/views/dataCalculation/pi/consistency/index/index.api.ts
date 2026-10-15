import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/consistencyIndex/getConsistencyData',
  detailList = '/pi/consistencyIndex/getConsistencyDetailData',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });
/**
 * 列表接口
 * @param params
 */
export const detailList = (params) =>
  defHttp.get({ url: Api.detailList, params });