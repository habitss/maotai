import { defHttp } from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piAbnormalAnalysisPoint/rootList',
  save = '/pi/piAbnormalAnalysisPoint/add',
  edit = '/pi/piAbnormalAnalysisPoint/edit',
  deletePiAbnormalAnalysisPoint = '/pi/piAbnormalAnalysisPoint/delete',
  importExcel = '/pi/piAbnormalAnalysisPoint/importExcel',
  exportXls = '/pi/piAbnormalAnalysisPoint/exportXls',
  loadTreeData = '/pi/piAbnormalAnalysisPoint/loadTreeRoot',
  getChildList = '/pi/piAbnormalAnalysisPoint/childList',
  getChildListBatch = '/pi/piAbnormalAnalysisPoint/getChildListBatch',
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
export const deletePiAbnormalAnalysisPoint = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deletePiAbnormalAnalysisPoint, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDeletePiAbnormalAnalysisPoint = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deletePiAbnormalAnalysisPoint, data: params }, { joinParamsToUrl: true }).then(() => {
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
  let url = Api.edit;
  if(!isUpdate){
    params.factoryCode = params.deptCode.substr(0, 9);
    url = Api.save;
  }
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
