import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  add='/pi/piAbnormalAnalysis/add',
  deleteOne = '/pi/piAbnormalAnalysis/delete',
  // importExcel = '/five/why/fiveWhyAnalyse/importExcel',
  // exportXls = '/five/why/fiveWhyAnalyse/exportXls',
  queryData = '/pi/piAbnormalAnalysis/queryAbnormalAnalysis',
  queryPiStandardData = '/pi/piAbnormalAnalysis/queryPiStandardData',
  queryTriggerTime = '/pi/piAbnormalAnalysis/queryTriggerTime',
  queryProcessAnalysis = '/pi/piAbnormalAnalysis/queryProcessAnalysis',
  evaluationList = '/pi/piAbnormalAnalysisEvaluation/queryPageList',
  saveEvaluation='/pi/piAbnormalAnalysisEvaluation/add',
  editEvaluation='/pi/piAbnormalAnalysisEvaluation/edit',
}
/**
 * 导出api
 * @param params
 */
// export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
// export const getImportUrl = Api.importExcel;

/**
 * 列表接口
 * @param params
 */
export const queryData = (params) =>
  defHttp.post({url: Api.queryData, params});

/**
 * 删除单个
 */
export const deleteOne = (id) => {
  return defHttp.delete({url: `/pi/piAbnormalAnalysis/delete?id=` + id}).then(() => {
  });
}

/**
 * 保存
 * @param params
 */
export const saveData = (params) => {
  return defHttp.post({url: Api.add, params});
}

/**
 * 排程记录头部表格接口
 * @param params
 */
export const queryPiStandardData = (params) =>
  defHttp.get({url: Api.queryPiStandardData, params});


/**
 * 排程记录更新时间接口
 * @param params
 */
export const queryTriggerTime = (params) =>
  defHttp.get({url: Api.queryTriggerTime, params});

/**
 * 排程记录散点图接口
 * @param params
 */
export const queryProcessAnalysis = (params) =>
defHttp.get({url: Api.queryProcessAnalysis, params});

/**
 * 保存或者更新评价
 * @param params
 */
export const saveOrUpdateEvaluation = (params, isUpdate) => {
  let url = isUpdate ? Api.editEvaluation : Api.saveEvaluation;
  return defHttp.post({url: url, params});
}
/**
 * 评价列表接口
 * @param params
 */
export const evaluationList = (params) =>
  defHttp.get({url: Api.evaluationList, params});