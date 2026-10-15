import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { addFactoryDept } from '/@/utils/common/compUtils';

const { createConfirm } = useMessage();

enum Api {
  list = '/manage/check/managerChecklistEditMain/queryPageList',
  queryInspectData = '/manage/check/managerChecklistEditMain/queryInspectData',
  saveOrUpdate = '/manage/check/managerChecklistEditMain/add',
  confirmData = '/manage/check/managerChecklistEditMain/confirmData',
  deleteOne = '/manage/check/managerChecklistEditMain/delete',
  deleteBatch = '/manage/check/managerChecklistEditMain/deletionBatch',
  importExcel = '/manage/check/managerChecklistEditMain/importExcel',
  exportXls = '/manage/check/managerChecklistEditMain/exportXls',
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
  defHttp.get({ url: Api.list, params });

/**
 * 检查项数据
 * @param params
 */
export const queryInspectData = (params) =>
  defHttp.get({ url: Api.queryInspectData, params });

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
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
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
  if(!isUpdate){
    addFactoryDept(params);
  }
  return defHttp.post({ url: Api.saveOrUpdate, params });
}
/**
 * 编辑
 * @param params
 */
export const confirmData = (params) => {
  return defHttp.post({ url: Api.confirmData, params });
}
