import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piCustomizeTableField/listData',
  listAll = '/pi/piCustomizeTableField/listAll',
  save='/pi/piCustomizeTableField/add',
  edit='/pi/piCustomizeTableField/edit',
  deleteOne = '/pi/piCustomizeTableField/deleteData',
  deleteBatch = '/pi/piCustomizeTableField/deleteBatchData',
  importExcel = '/pi/piCustomizeTableField/importExcel',
  exportXls = '/pi/piCustomizeTableField/exportXls',
  fieldList = '/pi/piCustomizeTableField/fieldByTableIdData',
  sort = '/pi/piCustomizeTableField/getSort',
  openOrCloseEdit = '/pi/piCustomizeTableField/openOrCloseEdit'
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
export const list = (params) =>{

  return defHttp.get({url: Api.list, params});
}
/**
 * 列表接口
 * @param params
 */
export const listAll = (params) =>{
  return defHttp.get({url: Api.listAll, params});
}
  

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
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
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  // return defHttp.post({url: url, params});
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 获取非唯一约束字段接口(排除定时任务已配置的字段)
 * @param params
 */
export const fieldList = (params) =>
  defHttp.get({url: Api.fieldList, params});

/**
 * 获取排序
 */
export const sort = (params) =>
  defHttp.get({url: Api.sort, params});

/**
 * 批量打开/关闭PI编辑
 */
export const openOrCloseEdit = (params,handleSuccess) => {
  return defHttp.delete({url: Api.openOrCloseEdit, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}