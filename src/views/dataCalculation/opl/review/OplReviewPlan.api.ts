import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
enum Api {
  list = '/opl/oplReviewPlan/queryPageList',
  edit = '/opl/oplReviewPlan/oplSubmit',
  fileSave = '/opl/oplReviewPlanFile/addFile',
  getFil = '/opl/oplReviewPlanFile/queryData',
  importExcel = '/opl/oplReviewPlan/importExcel',
  exportXls = '/opl/oplReviewPlan/exportXls',

}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;

/**
 * 列表接口
 * @param params
 */
export const list = (params) => {
  params.column = 'sort';
  params.order = 'asc';
  params.deptCode = userStore?.userInfo?.orgCode;
  return defHttp.get({ url: Api.list, params });
}

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.edit, params }, { isTransformResponse: false });
}

/**
 * 附件保存
 * @param params
 * @param isUpdate
 */
export const fileSave = (params) => {
  return defHttp.post({ url: Api.fileSave, params }, { isTransformResponse: false });
}

export const getFil = (params) => defHttp.get({ url: Api.getFil, params });