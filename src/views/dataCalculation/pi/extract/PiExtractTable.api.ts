import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piExtractTable/list',
  save='/pi/piExtractTable/add',
  edit='/pi/piExtractTable/edit',
  deleteOne = '/pi/piExtractTable/delete',
  deleteBatch = '/pi/piExtractTable/deleteBatch',
  importExcel = '/pi/piExtractTable/importExcel',
  exportXls = '/pi/piExtractTable/exportXls',
  getTable = '/pi/piExtractTable/getTable',
  getTargetTable = '/pi/piExtractTable/getTargetTable',
  fieldList = '/pi/piExtractTableField/queryPageList',
  getTargetTableField = '/pi/piExtractTableField/getTargetTableField'
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
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

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
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}


/**
 * 获取来源表
 * @param params
 */
export const getTableList = (params) => {
  return defHttp.get({ url: Api.getTable, params }, { isTransformResponse: false });
}

/**
 * 获取目标表
 * @param params
 */
export const targetTable = (params) => {
  return defHttp.get({ url: Api.getTargetTable, params }, { isTransformResponse: false });
}

/**
 * 列表接口(字段)
 * @param params
 */
export const fieldList = (params) => defHttp.get({ url: Api.fieldList, params });

/**
 * 获取目标表字段
 * @param params
 */
export const targetTableField = (params) => {
  return defHttp.get({ url: Api.getTargetTableField, params }, { isTransformResponse: false });
}