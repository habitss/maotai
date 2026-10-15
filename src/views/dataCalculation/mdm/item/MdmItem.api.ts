import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/mdm3/mdmItem/queryPageList',
  save = '/mdm3/mdmItem/add',
  edit = '/mdm3/mdmItem/edit',
  deleteOne = '/mdm3/mdmItem/delete',
  deleteBatch = '/mdm3/mdmItem/deletionBatch',
  importExcel = '/mdm3/mdmItem/importExcel',
  exportXls = '/mdm3/mdmItem/exportXls',

  listItemOption = '/mdm3/mdmItemSelection/queryPageList',
  saveItemOption='/mdm3/mdmItemSelection/add',
  editItemOption='/mdm3/mdmItemSelection/edit',
  deleteOneItemOption = '/mdm3/mdmItemSelection/delete',
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
 * 列表接口
 * @param params
 */
export const getItemOptionArr = (params) =>
  defHttp.get({ url: Api.listItemOption, params });

/**
 * 删除单个
 */
export const deleteOneItemOption = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOneItemOption, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdateItemOption = (params, isUpdate) => {
  let url = isUpdate ? Api.editItemOption : Api.saveItemOption;
  return defHttp.post({ url: url, params });
}
