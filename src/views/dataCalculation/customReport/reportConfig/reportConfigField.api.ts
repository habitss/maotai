import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/customReport/customReportField/listData',
  save='/customReport/customReportField/add',
  edit='/customReport/customReportField/edit',
  deleteOne = '/customReport/customReportField/deleteData',
  deleteBatch = '/customReport/customReportField/deleteBatchData',
  importExcel = '/customReport/customReportField/importExcel',
  exportXls = '/customReport/customReportField/exportXls',
  dictItemList = '/mes3_mdm/dict/queryPageList',
  getMaxSort = '/customReport/customReportField/getMaxSort',
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
 * 获取最大排序
 * @param params
 */
export const getMaxSort = (params) =>
  defHttp.get({url: Api.getMaxSort, params});

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
 * 查询字典选项数据列表
 * @param params
 */
export const getDictItemList = (params) => {
  return defHttp.get({ url: Api.dictItemList, params });
};
