import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();

enum Api {
  list = '/maotai/equipmentMaint/list/page',
  add = '/maotai/equipmentMaint/add',
  edit = '/maotai/equipmentMaint/edit',
  deleteOne = '/maotai/equipmentMaint/delete',
  deleteBatch = '/maotai/equipmentMaint/deletionBatch',
  queryById = '/maotai/equipmentMaint/queryById',
  statistics = '/maotai/equipmentMaint/statistics',
  importExcel = '/maotai/equipmentMaint/importExcel',
  exportXls = '/maotai/equipmentMaint/exportXls',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

/** 列表接口 */
export const list = (params) => {
  // 工单号 / 设备名称 / 执行人 模糊查询:值包 *xxx*,触发 JeecgBoot QueryGenerator 转 like
  if (params) {
    if (params.orderNo) params.orderNo = '*' + params.orderNo + '*';
    if (params.equipmentName) params.equipmentName = '*' + params.equipmentName + '*';
    if (params.executor) params.executor = '*' + params.executor + '*';
  }
  return defHttp.get({ url: Api.list, params });
};

/** 通过id查询 */
export const getById = (params) => defHttp.get({ url: Api.queryById, params });

/** 顶部统计:本月已完成/待执行/逾期/覆盖率 */
export const getStatistics = () => defHttp.get({ url: Api.statistics });

/** 删除单个 */
export const deleteOne = (params, handleSuccess) =>
  defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });

/** 批量删除 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () =>
      defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => handleSuccess()),
  });
};

/** 保存或更新(有 id 走编辑,无 id 走新增) */
export const saveOrUpdate = (params, isUpdate) => defHttp.post({ url: isUpdate ? Api.edit : Api.add, params });
