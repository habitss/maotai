import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/grafana/pointMapping/queryPageList',
  save = '/grafana/pointMapping/add',
  edit = '/grafana/pointMapping/edit',
  deleteOne = '/grafana/pointMapping/delete',
  deleteBatch = '/grafana/pointMapping/deleteBatch',
  importExcel = '/grafana/pointMapping/importExcel',
  exportXls = '/grafana/pointMapping/exportXls',

  dashboardPointMatchUrl = '/grafana/pointMapping/match',
  confirmPointMappingUrl = '/grafana/pointMapping/confirm',
  loadPointOptionsUrl = '/grafana/pointMapping/pointOptions',
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
 * 重新解析报表数据
 * @param params
 */
export const doDashboardPointMatch = (params) => {
  return defHttp.post({ url: Api.dashboardPointMatchUrl, params }, { isTransformResponse: false });
};

/**
 * 确认匹配点位(包括自动点位的确认与手动点位的提交)
 * @param params
 */
export const doConfirmPointMapping = (params) => {
  return defHttp.post({ url: Api.confirmPointMappingUrl, params }, { isTransformResponse: false });
};

/**
 * 查询点位选项列表
 * @param params
 */
export const listPointOptions = (params) => {
  return defHttp.get({ url: Api.loadPointOptionsUrl, params }, { isTransformResponse: false });
};

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
