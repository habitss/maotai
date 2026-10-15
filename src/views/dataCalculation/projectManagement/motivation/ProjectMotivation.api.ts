import { defHttp } from '/@/utils/http/axios';

enum Api {
  depart = '/project/management/projectManagement/queryDepartSummary',
  personal = '/project/management/projectManagement/queryPersonalProjectSummary',
}
/**
 * 列表接口
 * @param params
 */
export const list = (type, params) =>
  defHttp.get({ url: Api[type], params });