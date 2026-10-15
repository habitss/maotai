import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/action/plan/actionPlanTrack/queryPageList',
  queryById = '/action/plan/actionPlanTrack/queryById',
  addBatch = '/action/plan/actionPlanTrack/addBatch',
  save = '/action/plan/actionPlanTrack/add',
  edit = '/action/plan/actionPlanTrack/edit',
  deleteOne = '/action/plan/actionPlanTrack/delete',
  deleteBatch = '/action/plan/actionPlanTrack/deletionBatch',
  importExcel = '/action/plan/actionPlanTrack/importExcel',
  exportXls = '/action/plan/actionPlanTrack/exportXls',
  userList = '/mes3_mdm/dictGroup/userList',
  userDepartList = '/sys/user/userDepartList',
  departListAll = '/sys/sysDepart/listAll',
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
 * 根据id查询
 * @param params
*/
export const queryById = (id) => {
  let params = { id: id };
  return defHttp.get({ url: Api.queryById, params },{ isTransformResponse: false });
};
/**
 * 获取所有用户
 * @param params
 */
export const userList = (params) =>
  defHttp.get({ url: Api.userList, params });
/**
 * 获取用户部门
 * @param params
 */
export const userDepartList = (params) =>
  defHttp.get({ url: Api.userDepartList, params }, {isTransformResponse: false });

/**
 * 获取所有部门
 * @param params
 */
export const departListAll = (params) =>
  defHttp.get({ url: Api.departListAll, params });

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
 * 批量新增行动计划录入
 * @param params
 */
export const addBatch = (params) => {
  return defHttp.post({ url: Api.addBatch, params });
}
