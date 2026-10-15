import { defHttp } from '/@/utils/http/axios';

enum Api {
  rootPath = '/ems/reportData',
  getAnalysisTagData = '/ems/reportData/analysisTagData',
  queryTagHourData = '/ems/reportData/queryTagHourData',
}

/**
 * 查询指定点位及日期的点位相关数据
 */
export const getAnalysisTagData = (params) => {
  return defHttp.post({ url: Api.getAnalysisTagData, params });
};

/**
 * 查询指定点位大类指定时间段的小时/半小时数据
 */
export const queryTagHourData = (params) => {
  return defHttp.post({ url: Api.queryTagHourData, params });
};
