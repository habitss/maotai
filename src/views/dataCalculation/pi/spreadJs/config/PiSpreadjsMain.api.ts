import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piSpreadjsMain/queryPageList',
  save='/pi/piSpreadjsMain/add',
  edit='/pi/piSpreadjsMain/edit',
  queryById='/pi/piSpreadjsMain/queryById',
  deleteOne = '/pi/piSpreadjsMain/delete',
  deleteBatch = '/pi/piSpreadjsMain/deletionBatch',
  importExcel = '/pi/piSpreadjsMain/importExcel',
  exportXls = '/pi/piSpreadjsMain/exportXls',

  //getPiTableData = '/pi/piSpreadjsMain/getPiTableData',
  savePiConfig = '/pi/piSpreadjsMain/savePiConfig',
  getPiConfig = '/pi/piSpreadjsMain/getPiConfig',
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
  return defHttp.post({url: url, params});
}
/**
 * 根据id查询
 * @param params
*/
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params },{ isTransformResponse: false });
};
/**保存PI配置 */
export const savePiConfig = (params) => {
  return defHttp.post({url: Api.savePiConfig, params});
}

export const getPiConfig = (params) => {
  return defHttp.get({url: Api.getPiConfig, params});
}