import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { changeTableSortParams } from '/@/utils/ktTool';
import { useUserStore } from '/@/store/modules/user';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
const { createConfirm } = useMessage();

const userStore = useUserStore();
// 工厂code
const factoryCode = userStore.factoryCode
// 部门code
const deptCode = userStore.departCode
// 用户账号权限层级 - 3:工厂 4:部门
const orgCategory = userStore.orgCategory


enum Api {
  list = '/pi/allocationTableField/queryData',
  edit='/pi/allocationTableField/editVal',
  batchEditVal='/pi/allocationTableField/batchEditVal',
  deleteOne = '/pi/allocationTable/deleteData',
  deleteBatch = '/pi/allocationTable/deletePiDataBatch',
  exportXls = '/pi/allocationTableField/exportXls',
  head = '/pi/allocationTableField/getHead',
  allocationTableList = '/pi/allocationTable/listData',
  tableFieldsList = '/pi/allocationTableField/listData',
  rerun = '/pi/allocationTable/rerun',
  redYellowRegion = '/pi/allocationTable/redYellowRegion',
  getTableHead = '/pi/allocationTableField/tableHead',
  getAlgorithmField = '/pi/allocationTableField/getAlgorithmField',
  getTableFieldData = '/pi/allocationTableField/getTableFieldData'
}
/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
// export const getImportUrl = Api.importExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>{
  changeTableSortParams(params);
  params.orderField = 'sort';
  params.orderType = 'asc';
  if (orgCategory === '3') {
    // 匹配工厂code
    params.factoryCode = factoryCode
  } else {
    // 匹配部门code
    params.deptCode = deptCode
  }
  return defHttp.get({ url: Api.list, params });
}
 

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
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
 */
export const saveOrUpdate = (params, isUpdate) => {
  // let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: Api.edit , params }, { isTransformResponse: false });
}

export const head = (params) =>
  defHttp.get({url: Api.head, params});


export const allocationTableList = (params) =>{
  params.orderField = 'sort';
  params.orderType = 'asc';
  return defHttp.get({ url: Api.allocationTableList, params });
}

export const tableFieldsList = (params) =>{
  params.orderField = 'sort';
  params.orderType = 'asc';
  return defHttp.get({ url: Api.tableFieldsList, params });
}

/**
 * 算法重算/红黄区计算
 * @param params
 */
export const rerunFun = (params,rerunType) => {
  let url = rerunType===1 ? Api.rerun : Api.redYellowRegion;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 批量更新
 * @param params
 */
export const batchEditVal = (params) => {
  return defHttp.post({ url: Api.batchEditVal , params }, { isTransformResponse: false });
}
/**
 * 获取动态字段
 * @param params 
 */
export const tableHead = (params) => {
  return defHttp.get({url: Api.getTableHead, params});
}
  

/**
 * 获取配置算法的字段
 * @param params
 */
export const getAlgorithmField = (params) =>
  defHttp.get({url: Api.getAlgorithmField, params});

/**
 * 获取字段最新10条数据
 * @param params 
 * @returns 
 */
export const getTableFieldData = (params) =>{
  return defHttp.get({ url: Api.getTableFieldData, params });
}