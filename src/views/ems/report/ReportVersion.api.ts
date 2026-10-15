import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  queryPageList = '/ems/reportVersion/queryPageList',
  save = '/ems/reportVersion/add',
  edit = '/ems/reportVersion/edit',
  deleteOne = '/ems/reportVersion/delete',
  deleteBatch = '/ems/reportVersion/deleteBatch',
  importExcel = '/ems/reportVersion/importExcel',
  exportXls = '/ems/reportVersion/exportXls',
  getBlob = '/ems/reportVersion/getblob',
  saveX = '/ems/reportVersion/saveX',
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
 * 定制报表版本的上传参数类型
 */
export interface UploadRepVersionParams {
  file: File;
  name: string;
  data: {
    id: number | null;
    repid: number;
    verName: string;
    verDesc: string;
    effectSdate: string;
  };
}

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.queryPageList, params });

/**
 * 获取文件接口
 * @param params
 */
export const getBlob = (id) => {
  return defHttp.get(
    { url: `${Api.getBlob}/${id}`, responseType: 'blob' },
    { isReturnNativeResponse: true } //直接返回
  );
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

/**
 * 保存报表历史版本
 * @param data
 */
export const saveReportVersionX = (params: UploadRepVersionParams, success) => {
  return defHttp.uploadFile({ url: Api.saveX }, params, { success });
};
