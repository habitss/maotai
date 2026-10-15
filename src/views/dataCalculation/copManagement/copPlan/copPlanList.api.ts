import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/cop/copSchedule/queryPageList',

  deleteData = '/cop/copSchedule/deleteData',

  save='/cop/copSchedule/saveOrUpdate',

  cancel='/cop/copSchedule/cancel',

  edit='/cop/copSchedule/edit',

  updateCop = '/cop/copSchedule/updateCop',

  process='/cop/copSchedule/process',

  configList='/cop/copConfig/queryPageList',

  saveSubmit='/cop/copSubmit/add',

  findSubmit='/cop/copSubmit/queryByParam',

  optionsListApi='/mes3_mdm/dict/queryPageList',

  weekList='/mdm/mdmWeek/queryPageList'
}

/**
 * 查询COP管理配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

export const optionsApi = (params) => {
  return defHttp.get({ url: Api.optionsListApi, params });
};

export const getWeekList = (params) => {
  return defHttp.get({ url: Api.weekList, params });
};

/**
 * 通过主键删除COP管理数据
 */
export const deleteData = (id, handleSuccess) => {
    return defHttp.delete({url: Api.deleteData+'?id='+id}, {isTransformResponse: false}).then(() => {
      handleSuccess();
    });
}

/**
 * 保存或者更新COP管理
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
 * 保存或者更新COP配置
 * @param params
 */
export const updateCop = (params) => {
  let url = Api.updateCop ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

export const getConfigList = (params) => {
  return defHttp.get({ url: Api.configList, params });
};


/**
 * 保存或者更新
 * @param params
 */
export const processState = (params) => {
  let url = Api.process ;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}


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
