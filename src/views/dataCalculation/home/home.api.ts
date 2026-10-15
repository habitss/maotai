import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  factoryUsePageHome = '/home/pageHome',
  factoryActionStatisticsC = '/home/factoryActionStatisticsC',
  getDeptName = '/home/getDeptName',
  factoryActionStatisticsPi = '/home/factoryActionStatisticsPi',
  getAnnountCement = '/home/getAnnountCement',
  getPayCloseAttentionPi = '/pi/consistencyIndex/getPayCloseAttentionPi'
  // userWorkShopPageHome = '/home/pageHome',
}


/**
 * 列表接口
 * @param params
 */
// export const list = (params) => {
//   return defHttp.get({ url: Api.list, params })
// };


/**
 * 首页统计
 * @param params
 * @param isUpdate
 */
export const factoryUsePageHome = (params) => {
  params.factory = userStore.factoryCode;
  return defHttp.post({ url: Api.factoryUsePageHome, params }, { isTransformResponse: false });
}

/**
 * 
 * @param params
 * @param isUpdate
 */
export const factoryActionStatisticsData = (params) => {
  return defHttp.post({ url: Api.factoryActionStatisticsC, params }, { isTransformResponse: false });
}

/**
 * 车间统计
 * @param params
 * @param isUpdate
 */
export const userWorkShopPageHome = (params) => {
  return defHttp.post({ url: Api.factoryUsePageHome, params }, { isTransformResponse: false });
}
/**
 * 车间
 */
export const workShopActionStatisticsData = (params) => {
  return defHttp.post({ url: Api.factoryActionStatisticsC, params }, { isTransformResponse: false });
}

/**
 * 获取部门
 */
export const getDeptName = (params) => {
  return defHttp.get({ url: Api.getDeptName, params }, { isTransformResponse: false })
};
/**
 * 获取PI数据
 * @param params
 * @returns 
 */
export const factoryActionStatisticsPi = (params) => {
  return defHttp.post({ url: Api.factoryActionStatisticsPi, params }, { isTransformResponse: false });
}

/**
 * 获取公告
 * @param params
 * @returns 
 */
export const getAnnountCement = (params) => {
  return defHttp.get({ url: Api.getAnnountCement, params }, { isTransformResponse: false });
}

/**
 * 重点关注pi
 * @param params
 * @returns 
 */
export const queryPayCloseAttentionPi = (params) => {
  return defHttp.get({ url: Api.getPayCloseAttentionPi, params }, { isTransformResponse: false });
}