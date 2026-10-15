import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/pi/piCustomizeTable/getBoxDiagramData',
  piFieldList = '/pi/piCustomizeTableField/listAll',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * pi字段列表接口
 * @param params
 */
export const getPiFieldList = (params) =>
  defHttp.get({ url: Api.piFieldList, params });