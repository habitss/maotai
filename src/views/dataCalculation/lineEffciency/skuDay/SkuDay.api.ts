import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/line/efficiency/lineEfficiencySkuDay/queryPageData',
  save='/line/efficiency/lineEfficiencySkuDay/add',
  edit='/line/efficiency/lineEfficiencySkuDay/edit',
  deleteOne = '/line/efficiency/lineEfficiencySkuDay/delete',
  deleteBatch = '/line/efficiency/lineEfficiencySkuDay/deleteBatch',
  importExcel = '/line/efficiency/lineEfficiencySkuDay/importExcel',
  exportXls = '/line/efficiency/lineEfficiencySkuDay/exportXls',
  dataSplit = '/line/efficiency/lineEfficiencySkuDay/dataSplit',
  getOutputValue = '/line/efficiency/lineEfficiencySkuDay/getOutputValue',
  getSkuList = '/line/efficiency/lineEfficiencySkuDay/getSkuList',
  speedEdit='/line/efficiency/lineEfficiencySkuDay/speedEdit',
  queryById = '/line/efficiency/lineEfficiencySkuDay/queryById',
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
  params.column = "startTime";
  if(params.lineName){
    params.lineName = "*"+params.lineName+"*"
  }
  params.type = '1'
  if(params.dateDay){
    params.dateDay = params.dateDay + " 00:00:00"
  }
  return defHttp.get({ url: Api.list, params })
};

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
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 拆分
 * @param params
 * @param isUpdate
 */
export const dataSplit = (params) => {
  return defHttp.post({ url: Api.dataSplit, params }, { isTransformResponse: false });
}

/**
 * 获取产量
 * @param params
 */
export const getOutputValue = (params) => {
  return defHttp.post({ url: Api.getOutputValue, params }, { isTransformResponse: false });
}

/**
 * 获取SKU
 * @param params
 */
export const getSkuList = (params) => {
  return defHttp.post({ url: Api.getSkuList, params }, { isTransformResponse: false });
}

/**
 * 更新速度
 * @param params
 * @param isUpdate
 */
export const speedEdit = (params) => {
  return defHttp.post({ url: Api.speedEdit, params }, { isTransformResponse: false });
}

/**
 * 通过id查询
 * @param params 
 * @returns 
 */
export const queryById = (params) =>
  defHttp.get({url: Api.queryById, params}, { isTransformResponse: false });