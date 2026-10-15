import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/sop/sopStatisticData/queryPageList',
  save='/sop/sopStatisticData/sopSubmit',
  deleteOne = '/sop/sopStatisticData/delete',
  week = '/sop/sopStatisticData/getWeek',
  diagnosticPostSave = '/sop/sopStatisticData/diagnosticPostSave',
  getDiagnosticPost = '/sop/sopStatisticData/getDiagnosticPost',
  importExcel = '/sop/sopStatisticData/importExcel',
  exportXls = '/sop/sopStatisticData/exportXls',
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
export const list = (params) => {
  params.deptCode = userStore?.userInfo?.orgCode;
  return defHttp.get({ url: Api.list, params });
}


/**
 * 保存
 * @param params
 * @param isUpdate
 */
export const sopSubmit = (params) => {
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}

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
 * 列表接口
 * @param params
 */
export const getWeek = (params) => {
  // 处理months数组参数
  if (params.months && Array.isArray(params.months)) {
    const months = params.months;
    delete params.months;
    return defHttp.get({
      url: Api.week,
      params: {
        ...params,
        months
      },
      paramsSerializer: function(params) {
        return Object.entries(params)
          .map(([key, value]) => {
            if (Array.isArray(value)) {
              return value.map(v => `${key}=${encodeURIComponent(v)}`).join('&');
            }
            return `${key}=${encodeURIComponent(value)}`;
          })
          .join('&');
      }
    });
  }
  return defHttp.get({ url: Api.week, params });
}


/**
 * 操作性工作诊断表保存
 * @param params
 * @param isUpdate
 */
export const diagnosticPostSave = (params) => {
  return defHttp.post({ url: Api.diagnosticPostSave, params }, { isTransformResponse: false });
}

/**
 * 
 */
export const getDiagnosticPost = (params) => defHttp.get({ url: Api.getDiagnosticPost, params });