import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/grafana/grafanaDashboardBackup/list/page',
  save = '/grafana/grafanaDashboardBackup/add',
  edit = '/grafana/grafanaDashboardBackup/edit',
  deleteOne = '/grafana/grafanaDashboardBackup/delete',
  deleteBatch = '/grafana/grafanaDashboardBackup/deleteBatch',
  importExcel = '/grafana/grafanaDashboardBackup/importExcel',
  exportXls = '/grafana/grafanaDashboardBackup/exportXls',

  crawlBackupUrl = '/grafana/grafanaDashboardBackup/crawl',
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
 * 获取GF实例的所有报表JSON数据及DS数据
 * @param params
 */
export const doCrawlDashboardData = (params) => {
  return defHttp.get({ url: Api.crawlBackupUrl, params }, { isTransformResponse: false });
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
