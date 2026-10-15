import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/alarm/apiApp/queryPageList',
  save = '/alarm/apiApp/add',
  edit = '/alarm/apiApp/edit',
  deleteOne = '/alarm/apiApp/delete',
  deleteBatch = '/alarm/apiApp/deleteBatch',
  importExcel = '/alarm/apiApp/importExcel',
  exportXls = '/alarm/apiApp/exportXls',
  resetSecret = '/alarm/apiApp/resetSecret',
  cleanOldSecret = '/alarm/apiApp/cleanOldSecret',
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
 * 重置应用密钥
 * @param appId
 */
export const resetSecret = (appId) => {
  return defHttp.get({ url: `${Api.resetSecret}/${appId}` }, { isTransformResponse: false });
};

/**
 * 清理过期旧密钥
 * @param appId
 */
export const cleanOldSecret = (appId) => {
  return defHttp.get({ url: `${Api.cleanOldSecret}/${appId}` }, { isTransformResponse: false });
};
