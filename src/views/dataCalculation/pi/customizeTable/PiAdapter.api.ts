import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { changeTableSortParams } from '/@/utils/ktTool';

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/adapter/algorithm/queryPageList',
  addAlgorithm='/pi/adapter/algorithm/addAlgorithm',
  adapterData = '/pi/adapter/algorithm/adapterData',
  getUnit = '/pi/adapter/algorithm/getUnit',
  getEop = '/pi/adapter/algorithm/getEop',
  getEopParam = '/pi/adapter/algorithm/getEopParam',
  deleteOne = '/pi/adapter/algorithm/delete',
  deleteBatch = '/pi/adapter/algorithm/deletionBatch',
  getPcu = '/pi/adapter/algorithm/getPcu',
  getLine = '/pi/ra/piRaStep/getLine',
  getRaUnit = '/pi/ra/piRaStep/getUnit',
  getRaEop = '/pi/ra/piRaStep/getRaEop',
  getRaEopParam = '/pi/ra/piRaStep/getRaEopParam',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) =>{
  changeTableSortParams(params);
  params.orderField = 'createTime';
  params.orderType = 'asc';
  return defHttp.get({ url: Api.list, params });
}

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.addAlgorithm, params }, { isTransformResponse: false });
}

/**
 * 获取算法数据
 */
export const adapterData = (params) =>
  defHttp.get({url: Api.adapterData, params});

/**
 * 获取PCU
 */
export const getPcuList = (params) =>
  defHttp.get({url: Api.getPcu, params});
/**
 * 获取设备
 */
export const getUnitList = (params) =>
  defHttp.get({url: Api.getUnit, params});

/**
 * 获取EOP
 */
export const getEopList = (params) =>
  defHttp.get({url: Api.getEop, params});

/**
 * 获取参数
 */
export const getEopParamList = (params) =>
  defHttp.get({url: Api.getEopParam, params});


/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}


/**
 * 获取产线(RA)
 */
export const getLineList = (params) =>
  defHttp.get({url: Api.getLine, params});

/**
 * 获取设备(RA)
 */
export const getRaUnit = (params) =>
  defHttp.get({url: Api.getRaUnit, params});

/**
 * 获取步骤(RA)
 */
export const getRaEop = (params) =>
  defHttp.get({url: Api.getRaEop, params});

/**
 * 获取步骤参数(RA)
 */
export const getRaEopParam = (params) =>
  defHttp.get({url: Api.getRaEopParam, params});