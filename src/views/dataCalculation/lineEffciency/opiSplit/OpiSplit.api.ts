import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/line/efficiency/lineEfficiencyStateRecord/queryPageList',
  skuList = '/line/efficiency/lineEfficiencyStateRecordPieceSku/queryPageList',
  speedEdit ='/line/efficiency/lineEfficiencyStateRecordPieceSku/speedEdit',
  exportXls = '/line/efficiency/lineEfficiencyStateRecord/exportXls',
  skuExportXls = '/line/efficiency/lineEfficiencyStateRecordPieceSku/exportXls',
}

export const getExportUrl = Api.exportXls;
export const getSkuExportUrl = Api.skuExportXls;


/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  params.column = "startTime";
  params.type = "0";
  params.stateCode = "1";
  return defHttp.get({ url: Api.list, params })
};

/**
 * SKU列表接口
 * @param params
 */
export const skuList = (params) => {
  params.column = "startTime";
  params.queryType = "1"
  return defHttp.get({ url: Api.skuList, params })
};

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const speedEdit = (params) => {
  return defHttp.post({ url: Api.speedEdit, params }, { isTransformResponse: false });
}

