import { defHttp } from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/cpk/cpkConfig/rootList',
  save = '/cpk/cpkConfig/add',
  edit = '/cpk/cpkConfig/edit',
  deleteCpkConfig = '/cpk/cpkConfig/delete',
  importExcel = '/cpk/cpkConfig/importExcel',
  exportXls = '/cpk/cpkConfig/exportXls',
  treeList = '/cpk/cpkConfig/treeList',
  queryById = '/cpk/cpkConfig/queryById',
  getChildList = '/cpk/cpkConfig/childList',
  getChildListBatch = '/cpk/cpkConfig/getChildListBatch',
  saveDetail = '/cpk/cpkConfig/saveDetail',
  allList = '/cpk/cpkConfig/allList',
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
  defHttp.get({ url: Api.list, params });
/**
 * 删除
 */
export const deleteCpkConfig = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteCpkConfig, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteCpkConfig = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteCpkConfig, data: params }, { joinParamsToUrl: true }).then(() => {
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
 * 查询全部树形节点数据
 * @param params
 */
export const loadTreeData = (params) =>
  defHttp.get({ url: Api.treeList, params });
/**
 * 查询子节点数据
 * @param params
 */
export const getChildList = (params) =>
  defHttp.get({ url: Api.getChildList, params });
/**
 * 批量查询子节点数据
 * @param params
 */
export const getChildListBatch = (params) =>
  defHttp.get({ url: Api.getChildListBatch, params }, { isTransformResponse: false });

/**
 * 保存详细配置
 * @param params
 */
export const saveDetail = (params) => {
  return defHttp.post({ url: Api.saveDetail, params });
}
/**
 * 根据id获取数据
 * @param params
 */
export const queryById = (params) =>
  defHttp.get({ url: Api.queryById+"?id="+params.id });
/**
 * 列表接口
 * @param params
 */
export const allList = (params) =>
  defHttp.get({ url: Api.allList, params });