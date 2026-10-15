import { defHttp } from '/@/utils/http/axios';
import {useMessage} from "@/hooks/web/useMessage";
const { createConfirm } = useMessage();
enum Api {
  list = '/cop/copDevice/queryPageList',

  deleteData = '/cop/copDevice/deleteData',

  batchDelete = '/cop/copDevice/deletionBatch',
  save='/cop/copDevice/add',
  edit='/cop/copDevice/edit',

  pointList = '/cop/copPoint/queryPageList',

  batchPointDelete = '/cop/copPoint/deletionBatch',

  importExcel = '/cop/copDevice/importExcel',

  exportXls = '/cop/copDevice/exportXls',

}

/**
 * 查询COP管理配置列表
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 查询清洗点管理配置列表
 * @param params
 */
export const getPointList = (params) => {
  return defHttp.get({ url: Api.pointList, params });
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
 * 保存或者更新COP管理
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
    let url = isUpdate ? Api.save : Api.edit;
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  }

/**
 * 批量删除
 * @param params
 */
export const batchPointDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.batchPointDelete, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    }
  });
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
