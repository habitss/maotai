import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piCurveStandard/queryPageList',
  save='/pi/piCurveStandard/add',
  edit='/pi/piCurveStandard/edit',
  deleteOne = '/pi/piCurveStandard/delete',
  deleteBatch = '/pi/piCurveStandard/deletionBatch',
  importExcel = '/pi/piCurveStandard/importExcel',
  exportXls = '/pi/piCurveStandard/exportXls',

  getDevice = '/pi/piCurveStandard/getDevice',
  getRecipe = '/pi/piCurveStandard/getRecipe',
  getStep = '/pi/piCurveStandard/getStep',
  //配方
  recipeList = '/pi/piCurveStandard/recipeQueryPageList',
  recipeDelete = '/pi/piCurveStandard/recipeDelete',
  recipeSaveOrUpdate = '/pi/piCurveStandard/recipeSaveOrUpdate',

  //设备分组
  loadDeviceGroupData = '/pi/piCurveStandard/loadDeviceGroupData',
  groupSaveOrUpdate = '/pi/piCurveStandard/groupSaveOrUpdate',
  groupDelete = '/pi/piCurveStandard/groupDelete',

  //配置
  configQueryPageList = '/pi/piCurveStandard/configQueryPageList',

  configSave='/pi/piCurveStandard/configAdd',
  configEdit='/pi/piCurveStandard/configEdit',
  configDeleteOne = '/pi/piCurveStandard/configDelete',
  configDeleteBatch = '/pi/piCurveStandard/configDeletionBatch',
  configBatchCopy = '/pi/piCurveStandard/configBatchCopy',
}
/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>{
  params.column = 'sort';
  params.order = 'asc';
  return defHttp.get({url: Api.list, params});
}
  

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
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}

//获取设备
export const getDevice = (params) =>
  defHttp.get({url: Api.getDevice, params});
//获取配方
export const getRecipe = (params) =>
  defHttp.get({url: Api.getRecipe, params});
//获取步骤
export const getStep = (params) =>
  defHttp.get({url: Api.getStep, params});
/**
 * 配方保存或者更新
 * @param params
 */
export const recipeSaveOrUpdate = (params) => {
  return defHttp.post({url: Api.recipeSaveOrUpdate, params});
}

/**
 * 配方列表接口
 * @param params
 */
export const recipeList = (params) =>{
  params.column = 'recipeNo';
  params.order = 'asc';
  return defHttp.get({url: Api.recipeList, params});
}

/**
 * 配方删除单个
 */
export const recipeDelete = (params,handleSuccess) => {
  return defHttp.delete({url: Api.recipeDelete, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 查询全部树形节点数据
 * @param params
 */
export const loadDeviceGroupData = (params) =>
  defHttp.get({ url: Api.loadDeviceGroupData, params });


/**
 * 设备分组-保存或者更新
 * @param params
 */
export const groupSaveOrUpdate = (params) => {
  return defHttp.post({url: Api.groupSaveOrUpdate, params});
}

/**
 * 设备分组-删除单个
 */
export const groupDelete = (params,handleSuccess) => {
  return defHttp.delete({url: Api.groupDelete, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess(params.recipeId);
  });
}


/**
 * 列表接口
 * @param params
 */
export const configQueryPageList = (params) =>{
  params.column = 'sort';
  params.order = 'asc';
  return defHttp.get({url: Api.configQueryPageList, params});
}



/**
 * 删除单个
 */
export const configDeleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.configDeleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const configBatchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.configDeleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const configSaveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.configEdit : Api.configSave;
  return defHttp.post({url: url, params});
}
/**
 * 批量复制
 * @param ids 逗号分隔的id
 */
export const configBatchCopyApi = (params) => {
  return defHttp.post({url: Api.configBatchCopy, params}, {joinParamsToUrl: true});
}