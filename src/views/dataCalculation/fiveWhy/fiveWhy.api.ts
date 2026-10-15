import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/five/why/fiveWhyAnalyse/queryPageList',
  save='/five/why/fiveWhyAnalyse/add',
  edit='/five/why/fiveWhyAnalyse/edit',
  deleteOne = '/five/why/fiveWhyAnalyse/delete',
  deleteBatch = '/five/why/fiveWhyAnalyse/deletionBatch',
  importExcel = '/five/why/fiveWhyAnalyse/importExcel',
  exportXls = '/five/why/fiveWhyAnalyse/exportXls',
  queryData = '/five/why/fiveWhyAnalyse/queryData',
  detailList = '/fivewhy/fiveWhyDetail/queryPageList',
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
  defHttp.get({url: Api.list, params});

/**
 * 列表接口
 * @param params
 */
export const queryData = (params) =>
  defHttp.get({url: Api.queryData, params});

/**
 * 删除单个
 */
export const deleteOne = (params) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true});
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
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate, isTransformResponse) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params}, {isTransformResponse});
}

/**
 * 5w详情列表接口
 * @param params
 */
export const detailList = (params) =>
  defHttp.get({url: Api.detailList, params});