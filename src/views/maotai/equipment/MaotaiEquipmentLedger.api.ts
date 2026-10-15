import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();

enum Api {
  list = '/maotai/equipmentLedger/list/page',
  add = '/maotai/equipmentLedger/add',
  edit = '/maotai/equipmentLedger/edit',
  deleteOne = '/maotai/equipmentLedger/delete',
  deleteBatch = '/maotai/equipmentLedger/deletionBatch',
  queryById = '/maotai/equipmentLedger/queryById',
  importExcel = '/maotai/equipmentLedger/importExcel',
  exportXls = '/maotai/equipmentLedger/exportXls',
}

/** 导出api */
export const getExportUrl = Api.exportXls;

/** 导入api */
export const getImportUrl = Api.importExcel;

/** 列表接口 */
export const list = (params) => {
  // 功能位置编码 / 设备名称 模糊查询:值包 *xxx*,触发 JeecgBoot QueryGenerator 转 like(车间/状态下拉精确,不动)
  if (params) {
    if (params.funlocCode) params.funlocCode = '*' + params.funlocCode + '*';
    if (params.equipName) params.equipName = '*' + params.equipName + '*';
  }
  return defHttp.get({ url: Api.list, params });
};

/** 通过id查询 */
export const getById = (params) => {
  return defHttp.get({ url: Api.queryById, params });
};

/** 删除单个 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/** 批量删除 */
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

/** 保存或者更新(有id走编辑,无id走新增) */
export const saveOrUpdate = (params, isUpdate) => {
  return defHttp.post({ url: isUpdate ? Api.edit : Api.add, params });
};
