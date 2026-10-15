import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/grafana/grafanaMigrationLog/queryPageList',
  save = '/grafana/grafanaMigrationLog/add',
  edit = '/grafana/grafanaMigrationLog/edit',
  deleteOne = '/grafana/grafanaMigrationLog/delete',
  deleteBatch = '/grafana/grafanaMigrationLog/deleteBatch',
  importExcel = '/grafana/grafanaMigrationLog/importExcel',
  exportXls = '/grafana/grafanaMigrationLog/exportXls',

  deployDashboardUrl = '/grafana/grafanaMigrationLog/deploy',
}

/**
 * 发布指定报表到目标Grafana实例
 * @param params
 */
export const doDeployDashboard = (params) => {
  return defHttp.post({ url: Api.deployDashboardUrl, params }, { isTransformResponse: false });
};

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
