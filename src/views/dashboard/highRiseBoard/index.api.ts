import { http } from '/@/utils/http/axios';

enum Api {
  PLAN_PLAN_QUERYPAGELIST = '/plan/plan/queryPageListV2',
  REPORT_EFFICIENCY_DAPINGEFFICIENCY = '/report/efficiency/daPingEfficiency',
  REPORT_HIGHRISE_YEARYIELD = '/report/highRise/yearYield',
  PLAN_PLAN_COUNTSAPYEARYIELD = '/plan/plan/countSapYearYield',
  REPORT_HIGHRISE_TODAYYIELD = '/report/highRise/todayYield',
  REPORT_HIGHRISE_BOTTLEINSPECTIONPERCENT = '/report/highRise/bottleInspectionPercent',
}

/**
 * 工单list查询
 * @param params
 */
export const PLAN_PLAN_QUERYPAGELIST = (params) => {
  return http.get({ url: Api.PLAN_PLAN_QUERYPAGELIST, params });
};

export const REPORT_EFFICIENCY_DAPINGEFFICIENCY = () => {
  return http.get({ url: Api.REPORT_EFFICIENCY_DAPINGEFFICIENCY });
};

export const REPORT_HIGHRISE_YEARYIELD = (params) => {
  return http.get({ url: Api.PLAN_PLAN_COUNTSAPYEARYIELD, params });
};

export const REPORT_HIGHRISE_TODAYYIELD = () => {
  return http.get({ url: Api.REPORT_HIGHRISE_TODAYYIELD });
};

export const REPORT_HIGHRISE_BOTTLEINSPECTIONPERCENT = () => {
  return http.get({ url: Api.REPORT_HIGHRISE_BOTTLEINSPECTIONPERCENT });
};
