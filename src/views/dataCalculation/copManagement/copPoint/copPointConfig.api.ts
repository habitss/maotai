import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/cop/copConfig/queryPageList',
  deleteData = '/cop/copConfig/deleteData',
  save='/cop/copConfig/add',
  edit='/cop/copConfig/edit',
}

/**
 * 查询COP清洗点列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 通过主键删除COP清洗点数据
 */
export const deleteData = (params, handleSuccess) => {
  return defHttp.delete({url: Api.deleteData, params}).then(() => {
    handleSuccess();
  });
}

/**
 * 保存或者更新COP清洗点
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params });
  }
