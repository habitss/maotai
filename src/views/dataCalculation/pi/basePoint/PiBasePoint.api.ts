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
  list = '/pi/piBasePoint/listData',
  save='/pi/piBasePoint/add',
  edit='/pi/piBasePoint/edit',
  deleteOne = '/pi/piBasePoint/deleteData',
  deleteBatch = '/pi/piBasePoint/deleteDataBatch',
  importExcel = '/pi/piBasePoint/importExcel',
  exportXls = '/pi/piBasePoint/exportXls',
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
export const list = (params) =>{
  if(params.isNewData === undefined){
    params.isNewData = true;
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
  let url = isUpdate ? Api.edit : Api.save;
  // return defHttp.post({url: url, params});
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}
