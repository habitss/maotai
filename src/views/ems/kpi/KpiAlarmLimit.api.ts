import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/ems/kpiAlarmLimit/queryPageList',
  getByKpiTagId = '/ems/kpiAlarmLimit/getByKpiTagId',
  save = '/ems/kpiAlarmLimit/add',
  saveUpdateDto = '/ems/kpiAlarmLimit/saveUpdateDto',
  edit = '/ems/kpiAlarmLimit/edit',
  deleteOne = '/ems/kpiAlarmLimit/delete',
  deleteBatch = '/ems/kpiAlarmLimit/deleteBatch',
  importExcel = '/ems/kpiAlarmLimit/importExcel',
  exportXls = '/ems/kpiAlarmLimit/exportXls',
}

/**
 * 导出api
 */
export const getExportUrl = Api.exportXls;

/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;

/**
 * 通过kpiTagId查询预警限值
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
 * 保存或者更新DTO
 * @param params
 */
export const saveOrUpdateByDto = (params) => {
  return defHttp.post({ url: Api.saveUpdateDto, params }, { isTransformResponse: false });
};
