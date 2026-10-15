import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/post/check/postCheckRecordDatainfoMain/queryPageList',
  saveDataInfo = '/post/check/postCheckRecordDatainfoMain/saveDataInfo',
  save = '/post/check/postCheckRecordDatainfoMain/add',
  edit = '/post/check/postCheckRecordDatainfoMain/edit',
  deleteOne = '/post/check/postCheckRecordDatainfoMain/delete',
  deleteBatch = '/post/check/postCheckRecordDatainfoMain/deletionBatch',
  importExcel = '/post/check/postCheckRecordDatainfoMain/importExcel',
  exportXls = '/post/check/postCheckRecordDatainfoMain/exportXls',
  queryConfig = '/post/check/postCheckRecordDatainfoMain/queryConfig',
  userList = '/mes3_mdm/dictGroup/userList',
  queryDetailList = '/post/check/postCheckRecordDatainfoDetail/queryPageList',
}
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

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
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
}

/**
 * 保存或者更新
 * @param params
 */
export const saveDataInfo = (params) => {
  return defHttp.post({ url: Api.saveDataInfo, params });
}
  
/**
 * 获取所有用户
 * @param params
 */
export const userList = (params) =>
  defHttp.get({ url: Api.userList, params });

/**
 * 获取指定的数据和配置
 * @param params
 */
export const queryConfig = (params) =>
  defHttp.get({ url: Api.queryConfig, params });

/**
 * 填报明细列表接口
 * @param params
 */
export const queryDetailList = (params) =>
  defHttp.get({ url: Api.queryDetailList, params });