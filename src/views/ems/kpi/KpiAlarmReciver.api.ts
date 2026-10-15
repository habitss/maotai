import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/ems/kpiAlarmReciver/queryPageList',
  getByKpiTagId = '/ems/kpiAlarmReciver/getByKpiTagId',
  save = '/ems/kpiAlarmReciver/add',
  saveUpdateDto = '/ems/kpiAlarmReciver/saveUpdateDto',
  edit = '/ems/kpiAlarmReciver/edit',
  deleteOne = '/ems/kpiAlarmReciver/delete',
  deleteBatch = '/ems/kpiAlarmReciver/deleteBatch',
  importExcel = '/ems/kpiAlarmReciver/importExcel',
  exportXls = '/ems/kpiAlarmReciver/exportXls',
  groupNameList = '/ems/kpiAlarmReciver/groupNameList',
  groupUserList = '/ems/kpiAlarmReciver/groupUserList',
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
 * 通过kpiTagId查询报警接收人
 * @param params
 */
export const getByKpiTagId = (params) => defHttp.get({ url: Api.getByKpiTagId, params }, { isTransformResponse: false });

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.queryPageList, params });

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量删除
 * @param params
 * @param handleSuccess
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
    },
  });
};

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
};

/**
 * 查询用户分组列表接口
 */
export const getGroupNameList = () => defHttp.get({ url: Api.groupNameList });

/**
 * 查询用户分组对应的用户列表
 * @param params
 */
export const getGroupUserList = (params) => defHttp.get({ url: Api.groupUserList, params });

/**
 * 保存或者更新DTO
 * @param params
 */
export const saveOrUpdateByDto = (params) => {
  return defHttp.post({ url: Api.saveUpdateDto, params }, { isTransformResponse: false });
};
