import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();
enum Api {
  list = '/meeting/meetingTemplate/queryPageList',
  deleteOne = '/meeting/meetingTemplate/delete',

  deleteBatch = '/meeting/meetingTemplate/deletionBatch',

  importExcel = '/meeting/meetingTemplate/importExcel',

  exportXls = '/meeting/meetingTemplate/exportXls',
  save='/meeting/meetingTemplate/add',
  edit='/meeting/meetingTemplate/edit',

  saveSchedule='/meeting/meeting/scheduleMeeting',

  cancelSchedule= '/meeting/meeting/scheduleMeetingCancel',

  schedule = '/meeting/meetingSchedule/queryByMeetingType'

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
 * 查询列表
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
 * 查询列表
 * @param params
 */
export const getSchedule = (params) => {
  return defHttp.get({ url: Api.schedule, params });
};

/**
 * 保存排期
 * @param params
 */
export const saveSchedule = (params) => {
  let url = Api.saveSchedule;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 取消排期
 * @param params
 */
export const cancelSchedule = (params) => {
  let url = Api.cancelSchedule;
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
