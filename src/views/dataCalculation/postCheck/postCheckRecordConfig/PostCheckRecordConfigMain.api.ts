import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/post_check/postCheckRecordConfigMain/queryPageList',
  save = '/post_check/postCheckRecordConfigMain/add',
  edit = '/post_check/postCheckRecordConfigMain/edit',
  deleteOne = '/post_check/postCheckRecordConfigMain/delete',
  deleteBatch = '/post_check/postCheckRecordConfigMain/deletionBatch',
  importExcel = '/post_check/postCheckRecordConfigMain/importExcel',
  exportXls = '/post_check/postCheckRecordConfigMain/exportXls',
  //检查项目配置API
  queryConfigList = '/post_check/postCheckRecordConfigProject/queryList',
  saveConfig = '/post_check/postCheckRecordConfigProject/add',
  deleteConfig = '/post_check/postCheckRecordConfigProject/delete',
  deleteBatchConfig = '/post_check/postCheckRecordConfigProject/deletionBatch',
  importConfigExcel = '/post_check/postCheckRecordConfigProject/importExcel',
  exportConfigXls = '/post_check/postCheckRecordConfigProject/exportXls',
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
export const list = (params) =>
  defHttp.get({ url: Api.list, params });

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
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
}


/**
 * 岗位检查记录配置列表接口
 * @param params
 */
export const queryConfigList = (params) =>
  defHttp.get({ url: Api.queryConfigList, params });

/**
 * 岗位检查记录配置-检查项目-添加
 * @param params
 */
export const saveConfig = (params) => {
  return defHttp.post({ url: Api.saveConfig, params });
}

/**
 * 岗位检查记录配置-删除
 */
export const deleteConfig = (params) => {
  return defHttp.delete({ url: Api.deleteConfig, params }, { joinParamsToUrl: true });
}
/**
 * 岗位检查记录配置-批量删除
 * @param params
 */
export const batchDeleteConfig = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '注意，批量删除会刷新页面，请确保没有修改未保存的数据',
    content: '是否删除选中数据？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatchConfig, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 岗位检查记录配置-导出api
 * @param params
 */
export const getExportConfigUrl = Api.exportConfigXls;
/**
 * 岗位检查记录配置-导入api
 */
export const getImportConfigUrl = Api.importConfigExcel;