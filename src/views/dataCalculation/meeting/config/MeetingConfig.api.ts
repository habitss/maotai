import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/meeting/meetingConfig/queryPageList',
  deleteOne = '/meeting/meetingConfig/delete',
  save='/meeting/meetingConfig/add',
  edit='/meeting/meetingConfig/edit',
}

/**
 * 查询配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};


/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

/**
 * 删除单个
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}

