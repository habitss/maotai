import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/allocationTableField/listData',
  save='/pi/allocationTableField/add',
  edit='/pi/allocationTableField/edit',
  deleteOne = '/pi/allocationTableField/deleteData',
  deleteBatch = '/pi/allocationTableField/deleteBatchData',
  importExcel = '/pi/allocationTableField/importExcel',
  exportXls = '/pi/allocationTableField/exportXls',
  fieldData = '/pi/allocationTableField/getAllocationField',
  openOrCloseEdit = '/pi/allocationTableField/openOrCloseEdit',
  getTimeFormat = '/pi/allocationTableField/getTimeFormat',
  getTableCondition = '/pi/allocationTableField/getTableCondition',
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
export const list = (params) =>
  defHttp.get({url: Api.list, params});

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
  // return defHttp.post({url: url, params});
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 列表接口
 * @param params
 */
export const fieldData = (params) =>
  defHttp.get({url: Api.fieldData, params});


/**
 * 批量打开/关闭PI编辑
 */
export const openOrCloseEdit = (params,handleSuccess) => {
  return defHttp.delete({url: Api.openOrCloseEdit, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 获取格式化数据
 */
export const getTimeFormat = (params) =>
  defHttp.get({url: Api.getTimeFormat, params});


/**
 * 获取多表对照关系
 * @param params
 */
export const getTableCondition = (params) =>
  defHttp.get({url: Api.getTableCondition, params});