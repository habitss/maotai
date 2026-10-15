import { defHttp } from '/@/utils/http/axios';


enum Api {
  //绿区统计接口
  list = '/pi/consistencyIndex/piGreenDataByDept',
  //查询PI日报列表接口
  piCustomizeTable = '/pi/piCustomizeTable/listData',
  //查询PI日报所有PI接口
  queryPiList = '/pi/piCustomizeTableField/listData',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 查询PI日报列表
 * @param params
 * @returns 
 */
export const allocationTableList = (params) =>{
  params.orderField = 'sort';
  params.orderType = 'asc';
  return defHttp.get({ url: Api.piCustomizeTable, params });
}

/**
 * 
 * @param params 查询PI日报所有PI接口
 * @returns 
 */
export const queryPiList = (params) =>{
  return defHttp.get({ url: Api.queryPiList, params });
}