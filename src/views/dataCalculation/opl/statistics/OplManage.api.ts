import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();

enum Api {
  list = '/opl/oplHomePage/getOplManage',
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  params.dept = userStore?.userInfo?.orgCode;
  return defHttp.get({ url: Api.list, params });
};
