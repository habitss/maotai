import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/kpi/kpiPlatoOne/queryPageList',
  save='/kpi/kpiPlatoOne/add',
  edit='/kpi/kpiPlatoOne/edit',
  deleteOne = '/kpi/kpiPlatoOne/delete',
  deleteBatch = '/kpi/kpiPlatoOne/deletionBatch',
  importExcel = '/kpi/kpiPlatoOne/importExcel',
  exportXls = '/kpi/kpiPlatoOne/exportXls',
  listPlatoTree = '/kpi/kpiPlatoOne/listPlatoTree',
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
 * KPI周日报-一级柏拉图
 * @param params
 */
export const listPlatoTree = (params) => {
  return defHttp.post({url: Api.listPlatoTree, params});
}