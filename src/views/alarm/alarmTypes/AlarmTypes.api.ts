import { defHttp } from "/@/utils/http/axios";
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/alarm/alarmTypes/rootList',
  save='/alarm/alarmTypes/add',
  edit='/alarm/alarmTypes/edit',
  deleteAlarmTypes = '/alarm/alarmTypes/delete',
  importExcel = '/alarm/alarmTypes/importExcel',
  exportXls = '/alarm/alarmTypes/exportXls',
  loadTreeData = '/alarm/alarmTypes/loadTreeRoot',
  getChildList = '/alarm/alarmTypes/childList',
  getChildListBatch = '/alarm/alarmTypes/getChildListBatch',
  listOptions = '/alarm/alarmTypes/list/options',
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
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 删除
 * @param params
 * @param handleSuccess
 */
export const deleteAlarmTypes = (params,handleSuccess) => {
  return defHttp.delete({ url: Api.deleteAlarmTypes, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}

/**
 * 批量删除
 * @param params
 * @param handleSuccess
 */
export const batchDeleteAlarmTypes = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteAlarmTypes, data: params }, { joinParamsToUrl: true }).then(() => {
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
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params },{ isTransformResponse:false });
}

/**
 * 查询全部树形节点数据
 * @param params
 */
export const loadTreeData = (params) => defHttp.get({ url: Api.loadTreeData,params });

/**
 * 查询子节点数据
 * @param params
 */
export const getChildList = (params) => defHttp.get({ url: Api.getChildList, params });
  
/**
 * 批量查询子节点数据
 * @param params
 */
export const getChildListBatch = (params) => defHttp.get({ url: Api.getChildListBatch, params },{ isTransformResponse:false });

/**
 * 下拉选项列表
 * @param params
 */
export const listOptions = (params) => defHttp.get({ url: Api.listOptions, params });