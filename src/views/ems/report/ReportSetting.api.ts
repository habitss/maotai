import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  rootPath = '/ems/reportSetting',
  queryPageList = '/ems/reportSetting/queryPageList',
  save = '/ems/reportSetting/add',
  edit = '/ems/reportSetting/edit',
  deleteOne = '/ems/reportSetting/delete',
  deleteBatch = '/ems/reportSetting/deleteBatch',
  importExcel = '/ems/reportSetting/importExcel',
  exportXls = '/ems/reportSetting/exportXls',
  addCustomReport = '/ems/reportSetting/addCustom',
  saveOrder = '/ems/reportSetting/saveOrder',
  getMap = '/ems/reportSetting/map',
  getBlob = '/ems/reportSetting/getBlob',
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
 * 查询单个对象
 * @param id
 */
export const getOne = (id) => {
  return defHttp.get({ url: `${Api.rootPath}/${id}` });
};

/**
 * 列表接口
 * @param params
 */
export const queryPageList = (params) => {
  params.selectFields = 'id,factory_code,dept_code,line_code,rep_type,rep_name,icon_name,basedata_name,sort,create_by,create_time';
  return defHttp.get({ url: Api.queryPageList, params });
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
 * 定制报表的上传参数类型
 */
export interface UploadRepParams {
  file: File;
  name: string;
  data: {
    repName: string;
    baseRepNames: string;
  };
}
//定义批量加载 Png 图标
export interface PngIcon {
  default: string;
}

// 动态导入所有SVG文件
const pngIcons: Record<string, PngIcon> = import.meta.glob('@/assets/images/ems/*.png', { eager: true });

// 获取图标路径的函数
export const getPngIcon = (id: string): string => {
  return pngIcons[`/src/assets/images/ems/${id}.png`]?.default || '/images/ems/rep-default.png';
};

/**
 * 上传定制报表
 * @param data
 */
export const addCustomReportSetting = (params: UploadRepParams, success) => {
  return defHttp.uploadFile({ url: Api.addCustomReport }, params, { success });
};

/**
 * 保存排序
 */
export const saveReportsetOrder = (params) => {
  return defHttp.post({ url: Api.saveOrder, params });
};

/**
 * 获取报表地图列表
 */
export const getReportsetMap = () => {
  return defHttp.get({ url: Api.getMap });
};

/**
 * 获取报表 Xlsx 文件接口
 * @param params
 */
export const getXlsxBlob = (id) => {
  return defHttp.get(
    {
      url: `${Api.getBlob}/${id}/xlsx`,
      responseType: 'blob',
      headers: {
        'Content-Type': 'application/vnd.ms-excel',
      },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};
export const getblobByModel = (id, midJson) => {
  console.log(midJson);
  return defHttp.get(
    {
      url: `${Api.getBlob}/${id}/xlsx`,
      responseType: 'blob',
      headers: {
        'Content-Type': 'application/vnd.ms-excel',
      },
    },
    { isReturnNativeResponse: true } //直接返回
  );
};
