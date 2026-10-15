import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/maotai/equipmentDoc/list/page',
  add = '/maotai/equipmentDoc/add',
  edit = '/maotai/equipmentDoc/edit',
  deleteOne = '/maotai/equipmentDoc/delete',
  queryById = '/maotai/equipmentDoc/queryById',
}

/** 上传接口(JeecgBoot 标准) */
export const uploadUrl = '/sys/common/upload';

/** 按设备查询文档列表 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/** 通过id查询 */
export const getById = (params) => defHttp.get({ url: Api.queryById, params });

/** 新增文档(上传文件后,带元信息+fileUrl 调用) */
export const add = (params) => defHttp.post({ url: Api.add, params });

/** 编辑文档(可只改元信息;带新 fileUrl/fileName 则替换文件) */
export const edit = (params) => defHttp.post({ url: Api.edit, params });

/** 删除文档 */
export const deleteOne = (params, handleSuccess) =>
  defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
