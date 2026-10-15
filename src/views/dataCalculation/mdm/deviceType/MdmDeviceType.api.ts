import { defHttp } from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/mes3_mdm/deviceType/rootList',
  save = '/mes3_mdm/deviceType/add',
  edit = '/mes3_mdm/deviceType/edit',
  deleteMdmDeviceType = '/mes3_mdm/deviceType/delete',
  importExcel = '/mes3_mdm/deviceType/importExcel',
  exportXls = '/mes3_mdm/deviceType/exportXls',
  loadTreeData = '/mes3_mdm/deviceType/loadTreeRoot',
  getChildList = '/mes3_mdm/deviceType/childList',
  getChildListBatch = '/mes3_mdm/deviceType/getChildListBatch',
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
export const deleteMdmDeviceType = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteMdmDeviceType, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeleteMdmDeviceType = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteMdmDeviceType, data: params }, { joinParamsToUrl: true }).then(() => {
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
  return defHttp.post({ url: url, params });
}
/**
 * 查询全部树形节点数据
 * @param params
 */
export const loadTreeData = (params) =>
  defHttp.get({ url: Api.loadTreeData, params });
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
