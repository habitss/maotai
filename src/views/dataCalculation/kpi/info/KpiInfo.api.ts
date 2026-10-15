import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/kpi/kpiInfo/listTree',
  save='/kpi/kpiInfo/add',
  edit='/kpi/kpiInfo/edit',
  deleteOne = '/kpi/kpiInfo/delete',
  deleteBatch = '/kpi/kpiInfo/deleteBatch',
  importExcel = '/kpi/kpiInfo/importExcel',
  exportXls = '/kpi/kpiInfo/exportXls',
  queryDeptTreeList = '/kpi/kpiInfo/queryDeptTreeList',
  kpiControlConfiguration = '/kpi/kpiInfo/kpiControlConfiguration',
  batchAddOrEdit = '/kpi/kpiPlanData/batchAddOrEdit',
  copyData = '/kpi/kpiInfo/copyData'
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
  params.factory = userStore.factoryCode;
  // params.dept = userStore.departCode;
  params.dept = userStore?.userInfo?.orgCode;
  return defHttp.get({ url: Api.list, params })
};

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 批量删除
 * @param params
 * @param handleSuccess
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}


/**
 * 部门树接口
 * @param params
 */
export const queryDeptTreeList = (params) => defHttp.get({ url: Api.queryDeptTreeList, params });


/**
 * 获取KPI树-基础数据
 * @param params
 */
export const kpiControlConfiguration = (params) => {
  return defHttp.post({ url: Api.kpiControlConfiguration, params }, { isTransformResponse: false });
}

/**
 * 获取KPI树-基础数据-保存
 * @param params
 */
export const kpiControlConfigAddEdit = (params) => {
  return defHttp.post({ url: Api.batchAddOrEdit, params }, { isTransformResponse: false });
}

/**
 * KPI清单-复制
 */
export const copyData = (params) => {
  return defHttp.post({ url: Api.copyData, params }, { isTransformResponse: false });
}