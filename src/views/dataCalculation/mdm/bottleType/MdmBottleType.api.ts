import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
const { createConfirm } = useMessage();

enum Api {
  list = '/mes3_mdm/bottleType/queryPageList',
  save = '/mes3_mdm/bottleType/saveOrUpdate',
  deleteOne = '/mes3_mdm/bottleType',
  deleteBatch = '/mes3_mdm/bottleType/deleteBatch',
  importExcel = '/mes3_mdm/bottleType/importExcel',
  exportXls = '/mes3_mdm/bottleType/exportXls',
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
  return defHttp.get({ url: Api.list, params });
};

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne + `/${params.id}` }, { joinParamsToUrl: true }).then(() => {
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
    title: t('language.deleteTitle'),
    content: t('language.isConfirmDelete'),
    okText: t('language.okText'),
    cancelText: t('language.cancel'),
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
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.save, params });
};
