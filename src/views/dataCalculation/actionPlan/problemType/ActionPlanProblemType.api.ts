import { defHttp } from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/actionPlan/problemType/rootList',
  getChildListBatch = '/actionPlan/problemType/rootList',
  save = '/actionPlan/problemType/add',
  edit = '/actionPlan/problemType/edit',
  deleteActionPlanProblemType = '/actionPlan/problemType/delete',
  importExcel = '/actionPlan/problemType/importExcel',
  exportXls = '/actionPlan/problemType/exportXls',
  loadTreeData = '/actionPlan/problemType/loadTreeRoot',
  queryAllTreeData = '/actionPlan/problemType/queryAllTreeData',
  userList = '/mes3_mdm/dictGroup/userList',
}

/**
 * 获取所有用户
 * @param params
 */
export const userList = (params) =>
  defHttp.get({ url: Api.userList, params });

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
export const deleteActionPlanProblemType = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteActionPlanProblemType, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteActionPlanProblemType = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteActionPlanProblemType, data: params }, { joinParamsToUrl: true }).then(() => {
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
  defHttp.get({ url: Api.loadTreeData, params });
/**
 * 查询全部树形节点数据
 * @param params
 */
export const queryAllTreeData = (params) =>
  defHttp.get({ url: Api.queryAllTreeData, params });
/**
 * 批量查询子节点数据
 * @param params
 */
export const getChildListBatch = (params) => {
  params.pageNo = 1;
  params.pageSize = 1000;
  return defHttp.get({ url: Api.getChildListBatch, params }, { isTransformResponse: false });
}
