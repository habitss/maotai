import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  queryHeader = '/kpi/kpiReportActualDayValue/queryHeader',
  save='/kpi/kpiReportActualDayValue/saveData',
  edit='/kpi/kpiReportActualDayValue/edit',
  deleteOne = '/kpi/kpiReportActualDayValue/delete',
  deleteBatch = '/kpi/kpiReportActualDayValue/deleteBatch',
  importExcel = '/kpi/kpiReportActualDayValue/importExcel',
  exportXls = '/kpi/kpiReportActualDayValue/exportXls',
  queryData = '/kpi/kpiReportActualDayValue/queryData',
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
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 批量删除
 * @param params
 * @param handleSuccess
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
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 保存
 * @param params
 * @param isUpdate
 */
export const save = (params) => {
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}

/**
 * 获取表头
 * @param params
 * @param isUpdate
 */
export const queryHeader = (params) => {
  return defHttp.post({ url: Api.queryHeader, params }, { isTransformResponse: false });
}

/**
 * 获取数据
 * @param params
 * @param isUpdate
 */
export const queryData = (params) => {
  return defHttp.post({ url: Api.queryData, params }, { isTransformResponse: false });
}
