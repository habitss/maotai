import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/i18n/mdmI18nEntryLang/queryPageList',
  save = '/i18n/mdmI18nEntryLang/add',
  edit = '/i18n/mdmI18nEntryLang/edit',
  deleteOne = '/i18n/mdmI18nEntryLang/delete',
  deleteBatch = '/i18n/mdmI18nEntryLang/deleteBatch',
  importExcel = '/i18n/mdmI18nEntryLang/importExcel',
  exportXls = '/i18n/mdmI18nEntryLang/exportXls',

  pageWithLangList = '/i18n/mdmI18nEntryLang/pageWithLang',
  postSaveRow = '/i18n/mdmI18nEntryLang/saveRow',
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
 * 词条维度分页查询
 * @param params
 */
export const listPageWithLang = (params) => defHttp.get({ url: Api.pageWithLangList, params }, { isTransformResponse: false });

/**
 * 保存行
 * @param params
 */
export const saveEntryLangRow = (params) => {
  return defHttp.post({ url: Api.postSaveRow, params }, { isTransformResponse: false });
};
