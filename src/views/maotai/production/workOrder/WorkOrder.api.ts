import { defHttp } from '/@/utils/http/axios';

export interface WorkOrderItem {
  id: string;
  orderNo?: string;
  processName?: string;
  workshopCode?: string;
  status?: string;
  sourceTank?: string;
  sourceBrand?: string;
  targetTank?: string;
  targetBrand?: string;
  transferPath?: string;
  transferPathName?: string;
  skidCode?: string;
  amount?: number;
  emptySource?: number;
  batchNo?: string;
  planStartTime?: string;
  planEndTime?: string;
  startTime?: string;
  endTime?: string;
  planDate?: string;
  operator?: string;
  plateNo?: string;
  driverName?: string;
  contactPhone?: string;
  alcohol?: number;
  remark?: string;
  abandonType?: string;
  abandonUser?: string;
  abandonTime?: string;
}

export interface WorkOrderLogItem {
  id: string;
  orderNo: string;
  operator?: string;
  action?: string;
  content?: string;
  orderStatus?: string;
  operateTime?: string;
}

export interface TransferPath {
  code: string;
  name: string;
  lineName: string;
  skidCode?: string;
}

export interface WorkOrderStatistics {
  statusCount: Record<string, number>;
  workshopCount: Record<string, number>;
}

const BASE = '/maotai/workOrder';
const LOG_BASE = '/maotai/workOrderLog';
const ROUTE_BASE = '/maotai/transferRoute';

export const getOrderPage = (params) => defHttp.get({ url: `${BASE}/list/page`, params });
// 批次追溯:全量有效工单(不含已删除),前端构建酒液谱系图
export const getTraceOrders = () => defHttp.get<WorkOrderItem[]>({ url: `${BASE}/trace/orders` });
// 计数双向联动:workshopCode=当前车间 tab(statusCount 按它过滤),status=当前状态 tab(workshopCount 按它过滤,逗号多值)
export const getOrderStatistics = (params?: { workshopCode?: string; status?: string }) =>
  defHttp.get<WorkOrderStatistics>({ url: `${BASE}/statistics`, params });
export const addOrder = (data: WorkOrderItem) => defHttp.post({ url: `${BASE}/add`, params: data });
export const batchAddOrder = (data: { common: WorkOrderItem; rows: WorkOrderItem[] }) =>
  defHttp.post({ url: `${BASE}/batchAdd`, params: data });
export const editOrder = (id: string, data: WorkOrderItem) =>
  // @RequestParam 取 query:必须拼 URL(defHttp.put 的 params 不会上 query)
  defHttp.put({ url: `${BASE}/edit?id=${encodeURIComponent(id)}`, data });
export const transitionOrder = (id: string, action: string) =>
  defHttp.put({ url: `${BASE}/transition?id=${encodeURIComponent(id)}&action=${action}` });
export const deleteOrder = (id: string) => defHttp.delete({ url: `${BASE}/delete`, params: { id } }, { joinParamsToUrl: true });
export const getPaths = (params) => defHttp.get<TransferPath[]>({ url: `${BASE}/paths`, params });
export const getOrderLogPage = (params) => defHttp.get({ url: `${LOG_BASE}/list/page`, params });
/** 车间罐清单(设备台账 tank_config_id 非空);工单与路径配置下拉共用 */
export const getTanks = (workshopCode: string) =>
  defHttp.get<{ label: string; value: string }[]>({ url: `${BASE}/tanks`, params: { workshopCode } });

/** 转酒路径配置 */
export interface TransferRouteItem {
  id: string;
  workshopCode?: string;
  processType?: string;
  sourceTank?: string;
  targetTank?: string;
  skidCode?: string;
  routeCode?: string;
  routeName?: string;
  enabled?: number;
  sortNo?: number;
  remark?: string;
}
export const getRoutePage = (params) => defHttp.get({ url: `${ROUTE_BASE}/list/page`, params });
export const addRoute = (data: TransferRouteItem) => defHttp.post({ url: `${ROUTE_BASE}/add`, params: data });
export const editRoute = (data: TransferRouteItem) => defHttp.put({ url: `${ROUTE_BASE}/edit`, data });
export const deleteRoute = (id: string) =>
  defHttp.delete({ url: `${ROUTE_BASE}/delete`, params: { id } }, { joinParamsToUrl: true });

/** 车间×工艺 → 工艺列表(前后端校验对齐) */
export const WORKSHOP_PROCESSES: Record<string, string[]> = {
  ZZG: ['卸车工艺', '装车工艺', '倒罐工艺'],
  XLG: ['卸车工艺', '倒罐工艺', '装车工艺'],
  CPG: ['倒罐工艺', '灌装工艺'],
  GWG: ['灌装工艺', '装车工艺'],
};
export const TRUCK_PROCESSES = ['卸车工艺', '装车工艺'];
export const STATUS_TEXT: Record<string, string> = {
  pending: '待执行',
  running: '执行中',
  paused: '已暂停',
  completed: '已完成',
  abandoned: '已废弃',
};
