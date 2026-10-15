import { defHttp } from '/@/utils/http/axios';

/** 接口操作日志(审计总表 mes3_maotai_api_op_log)查询 */
export const getApiOpLogList = (params) => defHttp.get({ url: '/maotai/apiOpLog/list/page', params });
