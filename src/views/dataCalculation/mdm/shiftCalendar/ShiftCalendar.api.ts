import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/mdm/mdmShiftRelation/queryList',

  shiftList = '/mdm/mdmShiftRelation/queryListByDate',

  addList = '/mdm/mdmShiftRelation/queryListByAdd',

  addSchedule= '/mdm/mdmShiftRelation/addSchedule',
  groupList = '/mdm/mdmShiftGroup/queryList',
  save='/mdm/mdmShiftRelation/addList',
  edit='/mdm/mdmShiftRelation/edit',
  deleteOne = '/mdm/mdmShiftRelation/delete',
  deleteBatch = '/mdm/mdmShiftRelation/deletionBatch',
}

/**
 * 列表接口
 * @param params
 */
export const shiftList = (params) =>
  defHttp.get({url: Api.shiftList, params});

/**
 * 列表接口
 * @param params
 */
export const addList = (params) =>
  defHttp.get({url: Api.addList, params});


/**
 * 列表接口
 * @param params
 */
export const groupList = (params) =>
  defHttp.get({url: Api.groupList, params});


/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

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
  return defHttp.post({url: url, params});
}

/**
 * 保存或者更新
 * @param params
 */
export const addSchedule = (params) => {
  return defHttp.post({url: Api.addSchedule, params});
}
