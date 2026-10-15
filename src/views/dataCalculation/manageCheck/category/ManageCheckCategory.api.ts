import {defHttp} from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/manage/check/manageCheckCategory/rootList',
  allList = '/manage/check/manageCheckCategory/allData',
  save='/manage/check/manageCheckCategory/add',
  edit='/manage/check/manageCheckCategory/edit',
  deleteManageCheckCategory = '/manage/check/manageCheckCategory/delete',
  importExcel = '/manage/check/manageCheckCategory/importExcel',
  exportXls = '/manage/check/manageCheckCategory/exportXls',
  loadTreeData = '/manage/check/manageCheckCategory/loadTreeRoot',
  queryAllTreeData = '/manage/check/manageCheckCategory/queryAllTreeData',
  getChildList = '/manage/check/manageCheckCategory/childList',
  getChildListBatch = '/manage/check/manageCheckCategory/getChildListBatch',
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 * @param params
 */
export const getImportUrl = Api.importExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});
/**
 * 获取所有数据
 * @param params
 */
export const allList = () =>
  defHttp.get({url: Api.allList});
/**
 * 删除
 */
export const deleteManageCheckCategory = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteManageCheckCategory, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteManageCheckCategory = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.deleteManageCheckCategory, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
/**
 * 查询全部树形节点数据
 * @param params
 */
export const loadTreeData = (params) =>
  defHttp.get({url: Api.loadTreeData,params});
/**
 * 查询子节点数据
 * @param params
 */
export const getChildList = (params) =>
  defHttp.get({url: Api.getChildList, params});
/**
 * 批量查询子节点数据
 * @param params
 */
export const getChildListBatch = (params) =>
  defHttp.get({url: Api.getChildListBatch, params},{isTransformResponse:false});
/**
 * 一次获取所有树形节点数据
 * @param params
 */
export const queryAllTreeData = (params) =>
  defHttp.get({url: Api.queryAllTreeData,params});
