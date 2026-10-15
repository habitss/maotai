import { defHttp } from "/@/utils/http/axios";

enum Api {
  queryCpkCalcReport = '/cpk/cpkConfigCalc/queryCpkCalcReport',
}

/**
 * 列表接口
 * @param params
 */
export const queryCpkCalcReport = (params) =>
  defHttp.get({ url: Api.queryCpkCalcReport, params });