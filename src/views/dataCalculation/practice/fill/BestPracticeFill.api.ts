import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/practice/bestPracticeFill/queryPageList',
  save = '/practice/bestPracticeFill/add',
  edit = '/practice/bestPracticeFill/edit',
  deleteOne = '/practice/bestPracticeFill/delete',
  deleteBatch = '/practice/bestPracticeFill/deletionBatch',
  importExcel = '/practice/bestPracticeFill/importExcel',
  exportXls = '/practice/bestPracticeFill/exportXls',
  queryeRcommendByFillId = '/practice/bestPracticeRecommend/queryByFillId',
  saveRecommend='/practice/bestPracticeRecommend/add',
  editRecommend='/practice/bestPracticeRecommend/edit',
  deleteByFillId='/practice/bestPracticeReview/deleteByFillId',
}
/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 * @param params
 */
export const getImportUrl = Api.importExcel;
/**
 * 根据最佳时间填报id删除
 */
export const deleteByFillId = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteByFillId, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
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
 * 保存或者更新推荐表
 * @param params
 */
export const saveOrUpdateRecommend = (params, isUpdate) => {
  let url = isUpdate ? Api.editRecommend : Api.saveRecommend;
  return defHttp.post({ url: url, params });
}

/**
 * 根据填报id查询推荐表数据
 * @param params
 */
export const queryeRcommendByFillId = (params) =>
  defHttp.get({ url: Api.queryeRcommendByFillId, params });