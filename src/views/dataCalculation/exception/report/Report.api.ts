import { defHttp } from '/@/utils/http/axios';
import {useMessage} from "@/hooks/web/useMessage";
const { createConfirm } = useMessage();
enum Api {
  list = '/exception/exceptionReport/queryPageList',

  deleteData = '/exception/exceptionReport/delete',

  batchDelete = '/exception/exceptionReport/deletionBatch',
  save='/exception/exceptionReport/add',
  edit='/exception/exceptionReport/edit',


}

/**
 * 查询配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 通过主键删除数据
 */
export const deleteData = (id, handleSuccess) => {
    return defHttp.delete({url: Api.deleteData+'?id='+id}, {isTransformResponse: false}).then(() => {
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
      return defHttp.delete({ url: Api.batchDelete, data: params }, { joinParamsToUrl: true }).then(() => {
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
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

