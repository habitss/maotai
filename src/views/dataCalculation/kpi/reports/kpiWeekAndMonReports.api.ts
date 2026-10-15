import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  queryKpiReportHeader = '/kpi/kpiReportActualWeekValue/queryKpiReportHeader',
  listKpiReportData = '/kpi/kpiReportActualWeekValue/listKpiReportData',
  save='/kpi/kpiInfo/add',
  edit='/kpi/kpiInfo/edit',
  deleteOne = '/kpi/kpiInfo/delete',
  deleteBatch = '/kpi/kpiInfo/deleteBatch',
  importExcel = '/kpi/kpiInfo/importExcel',
  exportXls = '/kpi/kpiInfo/exportXls',
  queryDeptTreeList = '/kpi/kpiInfo/queryDeptTreeList',
  retryReport =  '/kpi/kpiReportActualWeekValue/retryReportDataJob',
  trendCurveHeader = '/kpi/kpiReportActualWeekValue/trendCurveHeader',
  trendCurveData = '/kpi/kpiReportActualWeekValue/trendCurveData'
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
 * 获取表头
 * @param params
 * @param isUpdate
 */
export const queryKpiReportHeader = (params) => {
  return defHttp.post({ url: Api.queryKpiReportHeader, params }, { isTransformResponse: false });
}

/**
 * 获取数据
 * @param params
 * @param isUpdate
 */
export const listKpiReportData = (params) => {
  params.factory = userStore.factoryCode;
  // params.dept = userStore.departCode;
  params.dept = userStore?.userInfo?.orgCode;
  return defHttp.post({ url: Api.listKpiReportData, params }, { isTransformResponse: false });
}

/**
 * 部门树接口
 * @param params
 */
export const queryDeptTreeList = (params) => defHttp.get({ url: Api.queryDeptTreeList, params });

/**
 * 周月报-重算
 * retryReport
 */
export const retryReport = (params) => {
  return defHttp.post({ url: Api.retryReport, params }, { isTransformResponse: false });
}

/**
 * KPI-趋势表头
 * retryReport
 */
export const trendCurveHeader = (params) => {
  return defHttp.post({ url: Api.trendCurveHeader, params }, { isTransformResponse: false });
}
/**
 * KPI-趋势数据
 * retryReport
 */
export const trendCurveData = (params) => {
  return defHttp.post({ url: Api.trendCurveData, params }, { isTransformResponse: false });
}