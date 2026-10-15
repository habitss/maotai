import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/period/periodVerificationSchedule/queryPageList',
  deleteData = '/period/periodVerificationSchedule/deleteData',
  save='/period/periodVerificationSchedule/saveOrUpdate',
  edit='/period/periodVerificationSchedule/edit',

  cancel='/period/periodVerificationSchedule/cancel',

  updatePeriod='/period/periodVerificationSchedule/updatePeriod',

  process='/period/periodVerificationSchedule/process',

  configList='/period/periodVerificationConfig/queryPageList',

  saveSubmit='/period/periodVerificationSubmit/add',

  findSubmit='/period/periodVerificationSubmit/queryByParam',

  viewList = '/period/periodVerificationSubmit/queryList',

  factoryPath = '/meeting/meeting/queryFactoryPath'

}

/**
 * 查询配置列表
 * @param params
 */
export const factoryPath = (params) => {
  return defHttp.get({ url: Api.factoryPath, params });
};

/**
 * 查询配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 查询配置列表
 * @param params
 */
export const viewList = (params) => {
  return defHttp.get({ url: Api.viewList, params });
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
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params) => {
    let url = Api.save ;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

export const cancel = (params) => {
  let url = Api.cancel ;
  return defHttp.post({ url: url, params }, { isTransformResponse: true });
}

  /**
 * 更新配置
 * @param params
 */
export const updatePeriod = (params) => {
  let url = Api.updatePeriod ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 保存或者更新
 * @param params
 */
export const processState = (params) => {
  let url = Api.process ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

export const getConfigList = (params) => {
  return defHttp.get({ url: Api.configList, params });
};

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateSubmit = (params) => {
  let url = Api.saveSubmit ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 查询提交数据
 * @param params
 */
export const findSubmit = (params) => {
  let url = Api.findSubmit ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}
