import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/grafana/grafanaDashboardExtract/queryPageList',
  save = '/grafana/grafanaDashboardExtract/add',
  edit = '/grafana/grafanaDashboardExtract/edit',
  deleteOne = '/grafana/grafanaDashboardExtract/delete',
  deleteBatch = '/grafana/grafanaDashboardExtract/deleteBatch',
  importExcel = '/grafana/grafanaDashboardExtract/importExcel',
  exportXls = '/grafana/grafanaDashboardExtract/exportXls',

  dashboardDataUrl = '/grafana/grafanaDashboardExtract/dashboardData',
  dashboardExtractUrl = '/grafana/grafanaDashboardExtract/extract',
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
export const list = (params) => defHttp.get({ url: Api.queryPageList, params });

/**
 * 获取报表所有相关数据
 * @param params
 */
export const getDashboardData = (params) => {
  return defHttp.get({ url: Api.dashboardDataUrl, params }, { isTransformResponse: false });
};

/**
 * 重新解析报表数据
 * @param params
 */
export const doDashboardExtract = (params) => {
  return defHttp.get({ url: Api.dashboardExtractUrl, params }, { isTransformResponse: false });
};

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

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
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
};
