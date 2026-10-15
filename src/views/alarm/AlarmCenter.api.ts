import { defHttp } from '/@/utils/http/axios';

enum Api {
  comparison = '/alarm/alarmEvent/bigScreenComparison',
  typeCount = '/alarm/alarmEvent/bigScreenAlarmType',
  topFive = '/alarm/alarmEvent/bigScreenDeviceTopFive',
  statusCount = '/alarm/alarmEvent/bigScreenStatus',
  list = '/alarm/alarmEvent/bigScreenList',
  todoList = '/alarm/alarmEvent/bigScreenTodo',
}

/**
 * 同比环比
 * @param params
 */
export const comparison = (params) => defHttp.get({ url: Api.comparison, params });

/**
 * 报警类型
 * @param params
 */
export const typeCount = (params) => defHttp.get({ url: Api.typeCount, params });

/**
 * 设备异常排名TOP5
 * @param params
 */
export const topFive = (params) => defHttp.get({ url: Api.topFive, params });

/**
 * 报警分析
 * @param params
 */
export const statusCount = (params) => defHttp.get({ url: Api.statusCount, params });

/**
 * 报警信息列表
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 待办事项
 * @param params
 */
export const todoList = (params) => defHttp.get({ url: Api.todoList, params });
