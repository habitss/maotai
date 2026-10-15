import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/grafana/grafanaDatasource/queryPageList',
  save = '/grafana/grafanaDatasource/add',
  edit = '/grafana/grafanaDatasource/edit',
  deleteOne = '/grafana/grafanaDatasource/delete',
  deleteBatch = '/grafana/grafanaDatasource/deleteBatch',
  importExcel = '/grafana/grafanaDatasource/importExcel',
  exportXls = '/grafana/grafanaDatasource/exportXls',

  crawlDataSourceUrl = '/grafana/grafanaDatasource/crawl',
  getDsCntMapUrl = '/grafana/grafanaDatasource/get/dsCntMap',
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
export const list = (params) => defHttp.get({ url: Api.queryPageList, params });

/**
 * 查询所有GF实例的数据源数量
 * @param params
 */
export const getDsCntMap = () => defHttp.get({ url: Api.getDsCntMapUrl });

/**
 * 获取GF实例的Datasource数据
 * @param params
 */
export const doCrawlDatasourceData = (params) => {
  return defHttp.get({ url: Api.crawlDataSourceUrl, params }, { isTransformResponse: false });
};

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
