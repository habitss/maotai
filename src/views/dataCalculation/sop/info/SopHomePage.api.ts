import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/sop/sopHomePage/queryPageList',
  save='/sop/sopHomePage/add',
  edit='/sop/sopHomePage/edit',
  deleteOne = '/sop/sopHomePage/delete',
  deleteBatch = '/sop/sopHomePage/deleteData',
  importExcel = '/sop/sopHomePage/importExcel',
  exportXls = '/sop/sopHomePage/exportXls',
  updateByWriteTwoRr = '/sop/sopHomePage/updateByWriteTwoRr'
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
  params.deptCode = userStore?.userInfo?.orgCode;
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
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
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}

/**
 * 保存编写职责
 * @param params
 */
export const updateByWriteTwoRr = (params) => {
  return defHttp.post({ url: Api.updateByWriteTwoRr, params }, { isTransformResponse: false });
}