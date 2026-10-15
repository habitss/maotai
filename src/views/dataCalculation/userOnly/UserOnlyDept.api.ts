import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { addFactoryDept} from '/@/utils/common/compUtils';

const { createConfirm } = useMessage();

enum Api {
  list = '/mes3User/userOnlyDept/queryPageList',
  queryNoExistsUserList = '/mes3User/userOnlyDept/queryNoExistsUser',
  save = '/mes3User/userOnlyDept/add',
  edit = '/mes3User/userOnlyDept/edit',
  deleteOne = '/mes3User/userOnlyDept/delete',
  deleteBatch = '/mes3User/userOnlyDept/deletionBatch',
  importExcel = '/mes3User/userOnlyDept/importExcel',
  exportXls = '/mes3User/userOnlyDept/exportXls',
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
 * 查询不存在用户管理中的数据
 * @param params
 */
export const queryNoExistsUserList = (params) =>
  defHttp.get({ url: Api.queryNoExistsUserList, params });

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
  if(!isUpdate){
    addFactoryDept(params);
  }
  return defHttp.post({ url: url, params });
}
