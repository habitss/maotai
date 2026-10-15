import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { addFactoryDept } from '/@/utils/common/compUtils';

const { createConfirm } = useMessage();

enum Api {
  list = '/project/management/projectManagement/queryPageList',
  save = '/project/management/projectManagement/add',
  edit = '/project/management/projectManagement/edit',
  deleteOne = '/project/management/projectManagement/delete',
  deleteBatch = '/project/management/projectManagement/deletionBatch',
  importExcel = '/project/management/projectManagementAction/importExcel',
  exportXls = '/project/management/projectManagementAction/exportXls',
  queryActionList = '/project/management/projectManagementAction/queryPageList',
  saveAction = '/project/management/projectManagementAction/add',
  deleteAction = '/project/management/projectManagementAction/delete',
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
  if(!isUpdate){
    addFactoryDept(params);
  }
  return defHttp.post({ url: url, params });
}

/**
 * 保存项目行动计划和节点
 * @param params
 */
export const saveAction = (params) => {
  return defHttp.post({ url: Api.saveAction, params });
}
/**
 * 项目行动计划和节点列表接口
 * @param params
 */
export const queryActionList = (params) =>
  defHttp.get({ url: Api.queryActionList, params });

/**
 * 删除单个项目行动计划
 */
export const deleteAction = (params) => {
  return defHttp.delete({ url: Api.deleteAction, params }, { joinParamsToUrl: true });
}