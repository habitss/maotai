import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();

enum Api {
  list = '/pi/standardMaintenance/queryPageList',
  addData = '/pi/standardMaintenance/add',
  updateData = '/pi/standardMaintenance/edit',
  deleteData = '/pi/standardMaintenance/deleteData',
  exportXls = '/pi/standardMaintenance/exportXls',
  queryStandard = '/pi/standardMaintenance/queryStandard',
  updateManageData = '/pi/standardManager/edit',
  batchEditVal = '/pi/standardMaintenance/batchEditVal',
  batchDelete = '/pi/standardMaintenance/batchDelete'
}

/**
 * 查询PI标准值维护列表
 * @param params
 */
export const getList = (params) => {
  params.column = 'sort';
  params.order = 'asc';
  return defHttp.get({ url: Api.list, params });
};

/**
 * 添加PI标准值维护数据
 * @param params
 */
export const addData = (params) => {
  return defHttp.post({ url: Api.addData, params }, {isTransformResponse: false});
};

/**
 * 修改PI标准值维护数据
 * @param params
 */
export const updateData = (params) => {
  return defHttp.put({ url: Api.updateData, params }, {isTransformResponse: false});
};

/**
 * 通过主键删除PI标准值维护数据
 */
export const deleteData = (id) => {
  return defHttp.delete({url: Api.deleteData+'?id='+id}, {isTransformResponse: false});
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
// /**
//  * 导入api
//  */
// export const getImportUrl = Api.importExcel;


/**
 * 查询标准
 * @param params
 */
export const queryStandard = (params) => {
  return defHttp.get({ url: Api.queryStandard, params });
};

/**
 * 修改标准管理数据
 * @param params
 */
export const updateManageData = (params) => {
  return defHttp.put({ url: Api.updateManageData, params }, {isTransformResponse: false});
};


/**
 * 批量更新
 * @param params
 */
export const batchEditVal = (params) => {
  return defHttp.post({ url: Api.batchEditVal , params }, { isTransformResponse: false });
}

/**
 * 批量删除
 */
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
      return defHttp.delete({url: Api.batchDelete, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}