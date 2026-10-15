import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { useUserStore } from '/@/store/modules/user';

// 用户信息
const userStore = useUserStore();
const { createConfirm } = useMessage();

enum Api {
  list = '/lims/limsResultsAbnormal/queryPageData',
  save='/lims/limsResultsAbnormal/add',
  edit='/lims/limsResultsAbnormal/edit',
  updateFwhy = '/lims/limsResultsAbnormal/updateFwhy',
  deleteBatch = '/lims/limsResultsAbnormal/deleteBatch',
  importExcel = '/lims/limsResultsAbnormal/importExcel',
  exportXls = '/lims/limsResultsAbnormal/exportXls',
  approval = '/lims/limsResultsAbnormal/approval',
  causeApproval = '/lims/limsResultsAbnormal/causeApproval',
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
  params.factoryCode = userStore.factoryCode;
  params.deptCode = userStore.departCode;
  return defHttp.get({ url: Api.list, params })
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
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
}

/**
 * 更新5why分析状态
 * @param params
 */
export const updateFwhyData = (params) => {
  return defHttp.post({ url: Api.updateFwhy, params }, { isTransformResponse: false });
}

/**
 * 审批
 * @param params
 */
export const approval = (params) => {
  return defHttp.post({ url: Api.approval, params }, { isTransformResponse: false });
}

/**
 * 根本原因审批 审批通过状态为1，不通过状态为2
 * @param params
 */
export const causeApproval = (params) => {
  return defHttp.post({ url: Api.causeApproval, params }, { isTransformResponse: false });
}