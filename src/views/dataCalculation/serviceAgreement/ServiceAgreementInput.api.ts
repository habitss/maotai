import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/serviceAgreement/serviceAgreementInput/queryPageList',
  save='/serviceAgreement/serviceAgreementInput/add',
  edit='/serviceAgreement/serviceAgreementInput/edit',
  deleteOne = '/serviceAgreement/serviceAgreementInput/delete',
  deleteBatch = '/serviceAgreement/serviceAgreementInput/deleteBatch',
  importExcel = '/serviceAgreement/serviceAgreementInput/importExcel',
  exportXls = '/serviceAgreement/serviceAgreementInput/exportXls',
  total = '/serviceAgreement/serviceAgreementInput/trackingList',

  exportTotalXls = '/serviceAgreement/serviceAgreementInput/exportTotalXls',
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * 导出api
 * @param params
 */
export const getExportTotalUrl = Api.exportTotalXls;

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
 * 统计接口
 * @param params
 */
export const total = (params) => defHttp.get({ url: Api.total, params });

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
  let url = isUpdate ? Api.save : Api.edit;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}
