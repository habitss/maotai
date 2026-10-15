import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/kpi/kpiExceptionSummary/queryPageData',
  kpiExceptionSummaryprecent = '/kpi/kpiExceptionSummary/percent',
  confirm = '/kpi/kpiExceptionSummary/confirm'
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });



/**
 * KPI异常清单统计
 * kpiExceptionSummaryprecent
 */
export const kpiExceptionSummaryprecent = (params) => defHttp.get({ url: Api.kpiExceptionSummaryprecent, params });


/**
 * ok/nok
 * @param params
 */
export const confirm = (params) => defHttp.get({ url: Api.confirm, params }, { isTransformResponse: false });