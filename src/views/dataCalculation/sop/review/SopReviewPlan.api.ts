import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/sop/sopReviewPlan/queryPageList',
  edit='/sop/sopReviewPlan/sopSubmit',
  fileSave='/sop/sopReviewPlanFile/addFile',
  getFil='/sop/sopReviewPlanFile/queryData',
  importExcel = '/sop/sopReviewPlan/importExcel',
  exportXls = '/sop/sopReviewPlan/exportXls',

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
 
 // 处理months数组参数
 if (params.months && Array.isArray(params.months)) {
   const months = params.months;
   delete params.months;
   return defHttp.get({
     url: Api.list,
     params: {
       ...params,
       months
     },
     paramsSerializer: function(params) {
       return Object.entries(params)
         .map(([key, value]) => {
           if (Array.isArray(value)) {
             return value.map(v => `${key}=${encodeURIComponent(v)}`).join('&');
           }
           return `${key}=${encodeURIComponent(value)}`;
         })
         .join('&');
     }
   });
 }
 
 return defHttp.get({ url: Api.list, params });
}


/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
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