import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum BaseInfoApi {
  list = '/pi/piDfAlgorithmBaseinfo/queryPageList',
  save='/pi/piDfAlgorithmBaseinfo/add',
  edit='/pi/piDfAlgorithmBaseinfo/edit',
  deleteOne = '/pi/piDfAlgorithmBaseinfo/delete',
  deleteBatch = '/pi/piDfAlgorithmBaseinfo/deletionBatch',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: BaseInfoApi.list, params});

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: BaseInfoApi.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
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
      return defHttp.delete({url: BaseInfoApi.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
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
  let url = isUpdate ? BaseInfoApi.edit : BaseInfoApi.save;
  return defHttp.post({url: url, params});
}


enum UniqueApi {
  list = '/pi/piDfAlgorithmUnique/queryPageList',
  save='/pi/piDfAlgorithmUnique/add',
  edit='/pi/piDfAlgorithmUnique/edit',
  deleteOne = '/pi/piDfAlgorithmUnique/delete',
  deleteBatch = '/pi/piDfAlgorithmUnique/deletionBatch',
}
/**
 * 列表接口
 * @param params
 */
export const listUnique = (params) =>
  defHttp.get({url: UniqueApi.list, params});

/**
 * 删除单个
 */
export const deleteOneUnique = (params,handleSuccess) => {
  return defHttp.delete({url: UniqueApi.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteUnique = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: UniqueApi.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateUnique = (params, isUpdate) => {
  let url = isUpdate ? UniqueApi.edit : UniqueApi.save;
  return defHttp.post({url: url, params});
}


enum CalcModelApi {
  list = '/pi/piDfAlgorithmModel/queryPageList',
  listAll = '/pi/piDfAlgorithmModel/queryAllList',
  queryMdmTableName = '/pi/piDfAlgorithmModel/queryMdmTableName',
  queryMdmTableFieldName = '/pi/piDfAlgorithmModel/queryMdmTableFieldName',
  save='/pi/piDfAlgorithmModel/add',
  edit='/pi/piDfAlgorithmModel/edit',
  deleteOne = '/pi/piDfAlgorithmModel/delete',
  deleteBatch = '/pi/piDfAlgorithmModel/deletionBatch',
}
/**
 * 列表接口
 * @param params
 */
export const listCalcModel = (params) =>
  defHttp.get({url: CalcModelApi.list, params});
/**
 * 列表接口
 * @param params
 */
export const listAllCalcModel = (params) =>
  defHttp.get({url: CalcModelApi.listAll, params});
/**
 * 查询所有主数据表名
 * @param params
 */
export const queryMdmTableName = (params) =>
  defHttp.get({url: CalcModelApi.queryMdmTableName, params});
/**
 * 查询主数据字段
 * @param params
 */
export const queryMdmTableFieldName = (params) =>
  defHttp.get({url: CalcModelApi.queryMdmTableFieldName, params});

/**
 * 删除单个
 */
export const deleteCalcModel = (params,handleSuccess) => {
  return defHttp.delete({url: CalcModelApi.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteCalcModel = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: CalcModelApi.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateCalcModel = (params, isUpdate) => {
  let url = isUpdate ? CalcModelApi.edit : CalcModelApi.save;
  return defHttp.post({url: url, params});
}