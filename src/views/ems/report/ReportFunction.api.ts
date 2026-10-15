import { defHttp } from '/@/utils/http/axios';

enum Api {
  getManualClassUsageData = '/ems/manualClassUsage/report',
  getManualDayUsageData = '/ems/manualDayUsage/report',
  getManualWeekUsageData = '/ems/manualDayUsage/reportWeek',
  getManualMtdUsageData = '/ems/manualDayUsage/reportMYt',
  getEditManualClassUsageData = '/ems/manualClassUsage/editReport',
  getEditManualDayUsageData = '/ems/manualDayUsage/editReport',
  updateManualDayUsage = '/ems/manualDayUsage/batchUpdate',
  updateManualClassUsage = '/ems/manualClassUsage/batchUpdate',

  getThirdpartyHourUsageData = '/ems/thirdpartyHourUsage/report',
  getThirdpartyClassUsageData = '/ems/thirdpartyClassUsage/report',
  getThirdpartyDayUsageData = '/ems/thirdpartyDayUsage/report',
  getThirdpartyWeekUsageData = '/ems/thirdpartyDayUsage/reportWeek',
  getThirdpartyMtdUsageData = '/ems/thirdpartyDayUsage/reportMYt',
  getEditThirdpartyDayUsageData = '/ems/thirdpartyDayUsage/editReport',
  getEditThirdpartyClassUsageData = '/ems/thirdpartyClassUsage/editReport',
  updateThirdpartyDayUsage = '/ems/thirdpartyDayUsage/batchUpdate',
  updateThirdpartyClassUsage = '/ems/thirdpartyClassUsage/batchUpdate',

  getCollectClassUsageData = '/ems/collectClassUsage/report',
  getCollectDayUsageData = '/ems/collectDayUsage/report',
  getCollectHalfHourUsageData = '/ems/collectHalfhourUsage/report',
  getCollectHourUsageData = '/ems/collectHourUsage/report',
  getCollectWeekUsageData = '/ems/collectDayUsage/reportWeek',
  getCollectMtdUsageData = '/ems/collectDayUsage/reportMYt',
  getEditCollectClassUsageData = '/ems/collectClassUsage/editReport',
  getEditCollectDayUsageData = '/ems/collectDayUsage/editReport',
  updateCollectClassUsage = '/ems/collectClassUsage/batchUpdate',
  updateCollectDayUsage = '/ems/collectDayUsage/batchUpdate',

  getOrsdataHourUsageData = '/ems/orsdataHourUsage/report',
  getOrsdataClassUsageData = '/ems/orsdataClassUsage/report',
  getOrsdataDayUsageData = '/ems/orsdataDayUsage/report',
  getOrsdataWeekUsageData = '/ems/orsdataDayUsage/reportWeek',
  getOrsdataMtdUsageData = '/ems/orsdataDayUsage/reportMYt',

  updateBackfillPointStatus = '/ems/reportData/updateBackfill',
}

/**
 * 下载zip文件
 * @param url 接口地址
 */
const downloadUsageZip = (url: string) => {
  return defHttp.get(
    {
      url,
      responseType: 'blob',
      headers: { 'Content-Type': 'application/zip' },
    },
    { isReturnNativeResponse: true }
  );
};

/**
 * 获取手输表班数据
 */
export const getManualClassUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getManualClassUsageData}/${month}`);

/**
 * 获取手输表日数据
 */
export const getManualDayUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getManualDayUsageData}/${month}`);

/**
 * 获取手输表编辑天数据
 */
export const getEditManualDayUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getEditManualDayUsageData}/${month}`);

/**
 * 获取手输表编辑班数据
 */
export const getEditManualClassUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getEditManualClassUsageData}/${month}`);

/**
 * 获取手输表WEEK数据
 */
export const getManualWeekUsageDataList = async (year: string | undefined) => {
  return defHttp.get({ url: Api.getManualWeekUsageData + '/' + year }, { isTransformResponse: false });
};

/**
 * 获取手输表MTD数据
 */
export const getManualMtdUsageDataList = async (year: string | undefined) => {
  return defHttp.get({ url: Api.getManualMtdUsageData + '/' + year }, { isTransformResponse: false });
};

/**
 * 获取第三方表小时数据
 */
export const getThirdpartyHourUsageList = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  return defHttp.post(
    {
      url: Api.getThirdpartyHourUsageData,
      responseType: 'blob',
      data: { eTime: endDate + ' 00:00:00', tagIds: tagids, showHeadFlag: headFlag, showHeadType: headType },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};

/**
 * 获取第三方表班数据
 */
export const getThirdpartyClassUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getThirdpartyClassUsageData}/${month}`);

/**
 * 获取第三方表日数据
 */
export const getThirdpartyDayUsageDataList = async (month: string | undefined) => downloadUsageZip(`${Api.getThirdpartyDayUsageData}/${month}`);

/**
 * 获取第三方表编辑天数据
 */
export const getEditThirdpartyDayUsageDataList = async (month: string | undefined) =>
  downloadUsageZip(`${Api.getEditThirdpartyDayUsageData}/${month}`);

/**
 * 获取第三方表编辑班数据
 */
export const getEditThirdpartyClassUsageDataList = async (month: string | undefined) =>
  downloadUsageZip(`${Api.getEditThirdpartyClassUsageData}/${month}`);

/**
 * 获取第三方表WEEK数据
 */
export const getThirdpartyWeekUsageDataList = async (year: string | undefined) => {
  return defHttp.get({ url: Api.getThirdpartyWeekUsageData + '/' + year }, { isTransformResponse: false });
};

/**
 * 获取第三方表MTD数据
 */
export const getThirdpartyMtdUsageDataList = async (year: string | undefined) => {
  return defHttp.get({ url: Api.getThirdpartyMtdUsageData + '/' + year }, { isTransformResponse: false });
};

/**
 * 获取中间表小时数据
 */
export const getOrsdataHourUsageList = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  return defHttp.post(
    {
      url: Api.getOrsdataHourUsageData,
      responseType: 'blob',
      data: { eTime: endDate + ' 00:00:00', tagIds: tagids, showHeadFlag: headFlag, showHeadType: headType },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};

/**
 * 获取中间表班数据
 */
export const getOrsdataClassUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getOrsdataClassUsageData}/${month}`);

/**
 * 获取中间表日数据
 */
export const getOrsdataDayUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getOrsdataDayUsageData}/${month}`);

/**
 * 获取中间表周数据
 */
export const getOrsdataWeekUsageList = async (year: string | undefined) =>
  defHttp.get({ url: Api.getOrsdataWeekUsageData + '/' + year }, { isTransformResponse: false });

/**
 * 获取中间表MTD数据
 */
export const getOrsdataMtdUsageList = async (year: string | undefined) =>
  defHttp.get({ url: Api.getOrsdataMtdUsageData + '/' + year }, { isTransformResponse: false });

/**
 * 获取手输其他用量
 */
export const listManualOtherUsage = async (data) => {
  return defHttp.post({ url: '', data: data });
};

/**
 * 获取点位上下限值
 */
export const getTagLimitByPI = async (data) => {
  return defHttp.post({ url: '', data: data });
};

/**
 * 获取采集表班数据
 */
export const getCollectClassUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getCollectClassUsageData}/${month}`);

/**
 * 获取采集表日数据
 */
export const getCollectDayUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getCollectDayUsageData}/${month}`);

/**
 * 获取采集表半小时数据
 */
export const getCollectHalfHourUsageList = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  return defHttp.post(
    {
      url: Api.getCollectHalfHourUsageData,
      responseType: 'blob',
      data: { eTime: endDate + ' 00:00:00', tagIds: tagids, showHeadFlag: headFlag, showHeadType: headType },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};

/**
 * 获取采集表小时数据
 */
export const getCollectHourUsageList = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  return defHttp.post(
    {
      url: Api.getCollectHourUsageData,
      responseType: 'blob',
      data: { eTime: endDate + ' 00:00:00', tagIds: tagids, showHeadFlag: headFlag, showHeadType: headType },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};

/**
 * 获取采集表周数据
 */
export const getCollectWeekUsageList = async (year: string | undefined) =>
  defHttp.get({ url: Api.getCollectWeekUsageData + '/' + year }, { isTransformResponse: false });

/**
 * 获取采集表MTD数据
 */
export const getCollectMtdUsageList = async (year: string | undefined) =>
  defHttp.get({ url: Api.getCollectMtdUsageData + '/' + year }, { isTransformResponse: false });

/**
 * 获取采集表编辑日数据
 */
export const getEditCollectDayUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getEditCollectDayUsageData}/${month}`);

/**
 * 获取采集表编辑班数据
 */
export const getEditCollectClassUsageList = async (month: string | undefined) => downloadUsageZip(`${Api.getEditCollectClassUsageData}/${month}`);

/**
 * 保存采集天数据
 */
export const saveCollectDayUsage = async (data) => defHttp.post({ url: Api.updateCollectDayUsage, data: data }, { isTransformResponse: false });

/**
 * 保存采集班数据
 */
export const saveCollectClassUsage = async (data) => defHttp.post({ url: Api.updateCollectClassUsage, data: data }, { isTransformResponse: false });

/**
 * 保存手输日数据
 */
export const saveManualDayUsage = async (data) => defHttp.post({ url: Api.updateManualDayUsage, data: data }, { isTransformResponse: false });

/**
 * 保存手输班数据
 */
export const saveManualClassUsage = async (data) => defHttp.post({ url: Api.updateManualClassUsage, data: data }, { isTransformResponse: false });
/**
 * 保存第三方日数据
 */
export const saveThirdpartyDayUsage = async (data) => defHttp.post({ url: Api.updateThirdpartyDayUsage, data: data }, { isTransformResponse: false });

/**
 * 保存第三方班数据
 */
export const saveThirdpartyClassUsage = async (data) =>
  defHttp.post({ url: Api.updateThirdpartyClassUsage, data: data }, { isTransformResponse: false });

/**
 * 手动设置指定区域点位重算
 */
export const updateBackfillPointStatus = async (data) =>
  defHttp.post({ url: Api.updateBackfillPointStatus, data: data }, { isTransformResponse: false });
