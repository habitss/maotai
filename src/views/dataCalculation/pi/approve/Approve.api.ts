import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piApprove/queryPageList',
  save='/pi/piApprove/addData',
  edit='/pi/piApprove/edit',
  deleteOne = '/pi/piApprove/delete',
  deleteBatch = '/pi/piApprove/deleteBatch',
  importExcel = '/pi/piApprove/importExcel',
  exportXls = '/pi/piApprove/exportXls',
  approve = '/pi/piApprove/approve'
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
  params.column = 'state';
  params.order = 'asc';
  return defHttp.get({ url: Api.list, params });
}

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
  // params.factoryCode = userStore.factoryCode;
  // params.deptCode = userStore.departCode;
  // params.lineCode = "";
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 审批
 * @param params
 * @param isUpdate
 */
export const approve = (params) => {
  return defHttp.post({ url: Api.approve, params }, { isTransformResponse: false });
}