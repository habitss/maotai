import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
enum Api {
  list = '/mes3_mdm/brand/queryPageList',
  save = '/mes3_mdm/brand',
  edit = '/mes3_mdm/brand',
  duplicateCheck = '/mes3_mdm/duplicate/check',
  deleteDict = '/mes3_mdm/brand',
  deleteBatch = '/mes3_mdm/brand/deleteBatch',
  importExcel = '/mes3_mdm/brand/importExcel',
  exportXls = '/mes3_mdm/brand/exportXls',

//   recycleBinList = '/mes3_mdm/brand/deleteList',
//   putRecycleBin = '/mes3_mdm/brand/back',
//   deleteRecycleBin = '/mes3_mdm/brand/deletePhysic',
//   itemList = '/mes3_mdm/dict/queryPageList',
//   deleteItem = '/mes3_mdm/dict',
//   itemSave = '/mes3_mdm/dict',
//   itemEdit = '/mes3_mdm/dict',
//   dictItemCheck = '/mes3_mdm/dict/dictItemCheck',
//   refreshCache = '/mes3_mdm/dict/refleshCache',
//   queryAllDictItems = '/mes3_mdm/dict/queryAllDictItems',
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
 * 字典列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * 删除字典
 */
export const deleteDict = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteDict + `/${params.id}` }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量删除字典
 * @param params
 */
export const batchDeleteDict = (params, handleSuccess) => {
  Modal.confirm({
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
 * 保存或者更新字典
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  const url = Api.save;
  if (isUpdate) {
    return defHttp.put({ url: url, params });
  }
  return defHttp.post({ url: url, params });
};
/**
 * 唯一校验
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });
/**
 * 字典回收站列表
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * 回收站还原
 * @param params
 */
export const putRecycleBin = (id, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * 回收站删除
 * @param params
 */
export const deleteRecycleBin = (id, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * 品牌管理列表
 * @param params
 */
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
/**
 * 品牌管理删除
 * @param params
 */
export const deleteItem = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteItem + `/${params.id}` }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 保存或者更新品牌管理
 * @param params
 */
export const saveOrUpdateDictItem = (params, isUpdate) => {
  const url = Api.itemSave;
  if (isUpdate) {
    return defHttp.put({ url: url, params });
  }
  return defHttp.post({ url: url, params });
};
/**
 * 校验字典数据值
 * @param params
 */
export const dictItemCheck = (params) => defHttp.get({ url: Api.dictItemCheck, params }, { isTransformResponse: false });
/**
 * 刷新字典
 * @param params
 */
export const refreshCache = () => defHttp.get({ url: Api.refreshCache }, { isTransformResponse: false });
/**
 * 获取所有字典项
 * @param params
 */
export const queryAllDictItems = () => defHttp.get({ url: Api.queryAllDictItems }, { isTransformResponse: false });

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
   const url = Api.save;
    if (isUpdate) {
      return defHttp.put({ url: url, params }, { isTransformResponse: false });
    }
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
}
