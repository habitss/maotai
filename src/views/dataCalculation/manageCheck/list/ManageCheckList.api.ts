import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { addFactoryDept } from '/@/utils/common/compUtils';

const { createConfirm } = useMessage();

enum Api {
  list = '/manage/check/manageCheckList/queryPageList',
  queryById = '/manage/check/manageCheckList/queryById',
  save = '/manage/check/manageCheckList/add',
  edit = '/manage/check/manageCheckList/edit',
  deleteOne = '/manage/check/manageCheckList/delete',
  deleteBatch = '/manage/check/manageCheckList/deletionBatch',
  importExcel = '/manage/check/manageCheckList/importExcel',
  exportXls = '/manage/check/manageCheckList/exportXls',
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
export const list = (params) =>
  defHttp.get({ url: Api.list, params }, { joinParamsToUrl: true });
/**
 * 列表接口
 * @param params
 */
export const queryById = (params) =>
  defHttp.get({ url: Api.queryById, params }, { joinParamsToUrl: true });

/**
 * 删除单个
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, params }).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  if(!isUpdate){
    addFactoryDept(params);
  }
  return defHttp.post({ url: url, params }, {isReturnNativeResponse: true});
}
