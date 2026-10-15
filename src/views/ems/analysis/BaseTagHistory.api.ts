import { defHttp, emsHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  list = '/ems/energyGroup/listPage',
  save = '/ems/energyGroup/add',
  edit = '/ems/energyGroup/edit',
  deleteOne = '/ems/energyGroup/delete',
  deleteBatch = '/ems/energyGroup/deleteBatch',
  listBaseTag = '/ems/collectTag/listPage',
  listHistoryCurveData = '/ems/admin/influx/getHistoryCurveData',
}

/**
 * 列表接口
 * @param params
 */
export const listEnergyGroup = (params) => defHttp.get({ url: Api.list, params });

/**
 * 获取基础点位
 * @param params
 */
export const getBaseTagList = (params) => emsHttp.get({ url: Api.listBaseTag, params });

/**
 * 查询指定点位历史曲线
 * @param params
 */
export const getHistoryCurveData = (params) => defHttp.post({ url: Api.listHistoryCurveData, params }, { isTransformResponse: false });

/**
 * 批量删除
 * @param params
 * @param handleSuccess
 */
export const delEnergyGroupList = (params, handleSuccess) => {
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
export const saveEnergyGroup = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
};
