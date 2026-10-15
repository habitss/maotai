import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/pi/piResultTransformationModel/queryPageList',
  save='/pi/piResultTransformationModel/add',
  edit='/pi/piResultTransformationModel/edit',
  deleteOne = '/pi/piResultTransformationModel/delete',
  deleteBatch = '/pi/piResultTransformationModel/deletionBatch',
  importExcel = '/pi/piResultTransformationModel/importExcel',
  exportXls = '/pi/piResultTransformationModel/exportXls',

  detailSaveOrUpdate = '/pi/piResultTransformationModelDetail/saveOrUpdate',
  getConditionData = '/pi/piResultTransformationModelDetail/getDetailData',
  getResultModel = '/pi/piResultTransformationModel/getResultModel'
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
export const list = (params) => {
  //按排序字段排序
  params.column = 'sort';
  params.order = 'asc';
  if(params.modelName){
    params.modelName = `*${params.modelName}*`
  }
  return defHttp.get({ url: Api.list, params });
}

/**
 * 删除单个
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

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
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = async (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
 try {
    const response = await defHttp.post({ url: url, params }, { 
      isTransformResponse: false 
    });
    console.log('API响应:', response); // 这里会打印错误信息
    // 如果业务失败，主动抛出错误
    if (response && response.success === false) {
      const error = new Error(response.message || '操作失败');
      error.code = response.code;
      error.response = response;
      throw error;
    }
    return response; 
  } catch (error) {
    // 重新抛出，让调用方处理
    throw error;
  }
}


/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const detailSaveOrUpdate = (params, isUpdate) => {
  return defHttp.post({ url:  Api.detailSaveOrUpdate, params }, { isTransformResponse: false });
}


export const getConditionData = (params) => {
  //按排序字段排序
  return defHttp.get({ url: Api.getConditionData, params });
}

export const getResultModel = (params) => {
  //按排序字段排序
  params.orderField = 'sort';
  params.orderType = 'asc';
  return defHttp.get({ url: Api.getResultModel, params });
}