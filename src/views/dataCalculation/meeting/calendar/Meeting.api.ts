import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/meeting/meeting/queryPageList',

  listForDate = '/meeting/meeting/queryByDate',

  listForMonth= '/meeting/meeting/queryList',

  viewList = '/meeting/meetingInfo/queryPageList',

  checkList = '/meeting/meetingCheck/queryPageList',

  recordList = '/meeting/meetingRecord/queryList',

  checkCode = '/meeting/meeting/generateQr',

  info = '/meeting/meeting/queryById',

  save='/meeting/meeting/add',

  edit='/meeting/meeting/edit',

  saveInfo='/meeting/meetingInfo/add',

  editInfo='/meeting/meetingInfo/edit',

  cancel='/meeting/meeting/active',

  deleteOne='/meeting/meeting/delete',

  saveRecord='/meeting/meetingRecord/add',

  depart='/meeting/meeting/queryDeptByCode',

  check= '/meeting/meetingCheck/add',

  checkUsers = '/meeting/meetingCheck/queryParticipant',

  checkEdit = '/meeting/meetingCheck/edit',

  getCheck = '/meeting/meetingCheck/getCheck',

  linkList = '/meeting/meetingLinks/queryByTemplate'

}

/**
 * 列表接口
 * @param params
 */
export const linkList = (params) =>
  defHttp.get({ url: Api.linkList, params });


/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

export const listForMonth = (params) =>
  defHttp.get({ url: Api.listForMonth, params });

export const listForDate = (params) =>
  defHttp.get({ url: Api.listForDate, params });

export const info = (params) =>
  defHttp.get({ url: Api.info, params });

export const checkUsers = (params) =>
  defHttp.get({ url: Api.checkUsers, params });


export const viewList = (params) =>
  defHttp.get({ url: Api.viewList, params });

export const checkList = (params) =>
  defHttp.get({ url: Api.checkList, params });

export const check = (params) => {
  let url = Api.check;
  return defHttp.post({ url: url, params });
}

export const checkEdit = (params) => {
  return defHttp.post({ url: Api.checkEdit, params }, { isTransformResponse: false });
};

export const checkCode = (params) => {
  let url = Api.checkCode;
  return defHttp.post({ url: url, params });
}

export const getCheck = (params) => {
  let url = Api.getCheck;
  return defHttp.post({ url: url, params });
}

export const recordList = (params) => {
  return defHttp.get({ url: Api.recordList, params });
}

export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.save : Api.edit;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

export const saveOrUpdateInfo = (params, isUpdate) => {
  let url = isUpdate ? Api.saveInfo : Api.editInfo;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

export const saveOrUpdateRecord = (params) => {
  let url = Api.saveRecord;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 查询列表
 * @param params
 */
export const cancel = (params) => {
  return defHttp.post({ url: Api.cancel, params }, { isTransformResponse: false });
};

/**
 * 查询机构id
 * @param params
 */
export const departCode = (params) =>
  defHttp.get({ url: Api.depart, params });


/**
 * 删除单个
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
