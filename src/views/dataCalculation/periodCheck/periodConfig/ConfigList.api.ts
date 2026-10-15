import { defHttp } from '/@/utils/http/axios';
import {useMessage} from "@/hooks/web/useMessage";
const { createConfirm } = useMessage();

enum Api {
  list = '/period/periodVerificationDevice/queryPageList',
  deleteData = '/period/periodVerificationDevice/delete',

  batchDelete = '/period/periodVerificationDevice/deletionBatch',

  save='/period/periodVerificationDevice/add',
  edit='/period/periodVerificationDevice/edit',

  configList = '/period/periodVerificationConfig/queryPageList',

  configDeleteData = '/period/periodVerificationConfig/delete',

  configSave='/period/periodVerificationConfig/add',

  configEdit='/period/periodVerificationConfig/edit',

  importExcel = '/period/periodVerificationDevice/importExcel',

  exportXls = '/period/periodVerificationDevice/exportXls',

  importConfigExcel = '/period/periodVerificationConfig/importExcel',

  exportConfigXls = '/period/periodVerificationConfig/exportXls',
}

/**
 * 查询列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
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
      return defHttp.delete({ url: Api.batchDelete, data: params }, { joinParamsToUrl: true }).then(() => {
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
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

/**
 * 查询列表
 * @param params
 */
export const getConfigList = (params) => {
  return defHttp.get({ url: Api.configList, params });
};


/**
 * 通过主键删除数据
 */
export const deleteConfigData = (id, handleSuccess) => {
  return defHttp.delete({url: Api.configDeleteData+'?id='+id}, {isTransformResponse: false}).then(() => {
    handleSuccess();
  });
}

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateConfig = (params, isUpdate) => {
  let url = isUpdate ? Api.configSave : Api.configEdit;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
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
 * 导出配置api
 * @param params
 */
export const getExportConfigUrl = Api.exportConfigXls;
/**
 * 导入配置api
 */
export const getImportConfigUrl = Api.importConfigExcel;
