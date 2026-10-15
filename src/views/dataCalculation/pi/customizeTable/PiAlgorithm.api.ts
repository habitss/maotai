import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { changeTableSortParams } from '/@/utils/ktTool';

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piCalculateSignal/listData',
  save='/pi/piCustomizeTableField/algorithmAdd',
  edit='/pi/piCustomizeTable/algorithmEdit',
  deleteOne = '/pi/piCalculateSignal/deleteData',
  deleteBatch = '/pi/piCalculateSignal/deleteDataBatch',
  importExcel = '/pi/piCustomizeTable/importExcel',
  exportXls = '/pi/piCustomizeTable/exportXls',
  getPoint = '/pi/piBasePoint/getPointData',
  getFieldByTableId = '/pi/piCustomizeTableField/getFieldByTableId',
  getPointStep = '/pi/piStep/getPointStep',
  algorithmData = '/pi/piCalculateSignal/getAlgorithmData',
  dictionary = '/pi/piCustomizeTableField/getDictionary',
  algorithmCopy = '/pi/piCustomizeTableField/algorithmCopy',
  updateEnable = '/pi/piCalculateSignal/updateEnable',
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
// export const list = (params) =>
//   defHttp.get({url: Api.list, params});
export const list = (params) =>{
  changeTableSortParams(params);
  params.orderField = 'isEnabled';
  params.orderType = 'desc';
  return defHttp.get({ url: Api.list, params });
}

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
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
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
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
  // return defHttp.post({url: url, params});
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 获取点位
 */
export const getPoint = (params) =>
  defHttp.get({url: Api.getPoint, params});

/**
 * 获取字段(唯一约束/全量)
 */
export const getFieldByTableId = (params) =>
  defHttp.get({url: Api.getFieldByTableId, params});

/**
 * 获取点位对应的步骤
 */
export const pointStep = (params) =>
  defHttp.get({url: Api.getPointStep, params});

/**
 * 获取PI日报字段
 */
// export const getDailyField = (params) =>
//   defHttp.get({url: Api.getUnique, params});

/**
 * 获取算法数据
 */
export const algorithmData = (params) =>
  defHttp.get({url: Api.algorithmData, params});

/**
 * 获取算法数据
 */
export const getDictionary = (params) =>
  defHttp.get({url: Api.dictionary, params});

/**
 * 算法复制
 * @param params
 */
export const algorithmCopy = (params) => {
  return defHttp.post({ url: Api.algorithmCopy, params }, { isTransformResponse: false });
}
/**
 * 启用禁用
 * @param params
 */
export const updateEnable = (params,handleSuccess) => {
  //return defHttp.get({ url: Api.updateEnable, params }, { isTransformResponse: false });
  return defHttp.get({url: Api.updateEnable, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}