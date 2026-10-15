import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { changeTableSortParams } from '/@/utils/ktTool';
import { useUserStore } from '/@/store/modules/user';
import { useI18n } from '/@/hooks/web/useI18n';



enum Api {
  
  
  // getReccat = '/pi/adapter/piSistarRtSteps/getReccat',
  // getMashingData = '/pi/adapter/piSistarRtSteps/getMashingData',

  getRtBatches = '/pi/piCurveConfig/getRtBatches',
  getDevice = '/pi/piCurveConfig/getDevice',
  getRecipe = '/pi/piCurveConfig/getRecipe',
  getOrder = '/pi/piCurveConfig/getOrder',
  getBatch = '/pi/piCurveConfig/getBatch',
  pointData = '/pi/piCurveConfig/pointData',
  getData = '/pi/piCurveConfig/getData',

  unitAndPoint = '/pi/piCurveConfig/unitAndPoint',
  getPointData = '/pi/piCurveConfig/getPointData',
}


/**
 * 获取配方品种
 * @param params
 */
// export const getReccat = (params) =>{
//   return defHttp.post({ url: Api.getReccat, params });
// }

/**
 * 获取配方
 * @param params
 */
export const getRecipe = (params) =>{
  return defHttp.post({ url: Api.getRecipe, params });
}

/**
 * 获取订单
 * @param params
 */
export const getOrder = (params) =>{
  return defHttp.post({ url: Api.getOrder, params });
}
/**
 * 获取批次(步骤)
 * @param params
 */
export const getBatch = (params) =>{
  return defHttp.post({ url: Api.getBatch, params });
}

/**
 * 获取批次表
 * @param params
 */
export const getRtBatches = (params) =>{
  return defHttp.post({ url: Api.getRtBatches, params });
}

/**
 * 获取设备和点位
 * @param params
 */
export const unitAndPoint = (params) =>{
  return defHttp.post({ url: Api.unitAndPoint, params });
}

/**
 * 获取点位数据
 * @param params
 */
export const getPointData = (params) =>{
  return defHttp.post({ url: Api.getPointData, params });
}

/**
 * 获取步骤时间数据
 * @param params
 */
export const getData = (params) =>{
  return defHttp.post({ url: Api.getData, params });
}

/**
 * 特定获取点位数
 */
export const pointData = (params) =>{
  return defHttp.post({ url: Api.pointData , params });
}

/**
 * 获取设备
 */
export const getDevice = (params) =>{
  return defHttp.post({ url: Api.getDevice , params });
}

/**
 * 获取糖化锅最新批次数据
 */
// export const getMashingData = (params) =>{
//   return defHttp.post({ url: Api.getMashingData , params });
// }