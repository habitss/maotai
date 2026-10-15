import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/sop/sopHomePage/getSopDept',

}


/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
 params.dept = userStore?.userInfo?.orgCode;
 return defHttp.get({ url: Api.list, params });
};


