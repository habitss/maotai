import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/allocationTableField/listData',
  editBatch='/pi/allocationTableField/editBatch',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * 保存或者更新
 * @param params
 */
export const editBatch = (params) => {
  return defHttp.post({ url: Api.editBatch, params });
}
