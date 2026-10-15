import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/ems/kpiThreshold/queryPageList',
  listByKpiTagId = '/ems/kpiThreshold/listByKpiTagId',
  save = '/ems/kpiThreshold/add',
  saveBatch = '/ems/kpiThreshold/addBatch',
  edit = '/ems/kpiThreshold/edit',
  deleteOne = '/ems/kpiThreshold/delete',
  deleteBatch = '/ems/kpiThreshold/deleteBatch',
  importExcel = '/ems/kpiThreshold/importExcel',
  exportXls = '/ems/kpiThreshold/exportXls',
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
 * 根据KPI点ID查询列表
 * @param params
 */
export const listByKpiTagId = (params) => defHttp.get({ url: Api.listByKpiTagId, params });

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

/**
 * 批量保存并更新
 * @param params
 */
export const saveBatch = (params) => {
  return defHttp.post({ url: Api.saveBatch, params }, { isTransformResponse: false });
};
