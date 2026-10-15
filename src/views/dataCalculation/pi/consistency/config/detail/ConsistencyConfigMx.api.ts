import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/consistencyConfigMx/queryPageList',
  save = '/pi/consistencyConfigMx/add',
  edit = '/pi/consistencyConfigMx/edit',
  deleteOne = '/pi/consistencyConfigMx/delete',
  deleteBatch = '/pi/consistencyConfigMx/deletionBatch',
  piList = '/pi/consistencyConfigPi/queryPageList',
  piSave = '/pi/consistencyConfigPi/add',
  piEdit = '/pi/consistencyConfigPi/edit',
  piDeleteOne = '/pi/consistencyConfigPi/delete',
  piDeleteBatch = '/pi/consistencyConfigPi/deletionBatch',
  piCustomizeList = '/pi/piCustomizeTable/listData',
  piCustomizeFieldList = '/pi/piCustomizeTableField/listData',
}
//===============明细===============
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

/**
 * 删除单个
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
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
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
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
  return defHttp.post({ url: url, params });
}


//================pi================
/**
 * 列表接口
 * @param params
 */
export const piList = (params) =>
  defHttp.get({ url: Api.piList, params });

/**
 * 删除单个
 */
export const piDeleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.piDeleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const piBatchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.piDeleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const piSaveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.piEdit : Api.piSave;
  return defHttp.post({ url: url, params });
}
/**
 * 自定义报表主表列表接口
 * @param params
 */
export const piCustomizeList = (params) =>
  defHttp.get({ url: Api.piCustomizeList, params });

/**
 * 自定义报表主表字段列表接口
 * @param params
 */
export const piCustomizeFieldList = (params) =>
  defHttp.get({ url: Api.piCustomizeFieldList, params });