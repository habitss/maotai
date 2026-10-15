import { defHttp } from '/@/utils/http/axios';
import type { AlarmEventVO } from '../alarm/AlarmCenter.api';

/**
 * 首页看板数据源(HomeDashboard.vue)。
 * 真实三源:设备台账(在线率+状态分布)、保养工单(保养执行)、工艺报警(报警概览);
 * 能耗三项(电/水/压缩空气)与车间能耗雷达暂无计量数据源,页面用原型静态数,
 * 接入后只需替换 HomeDashboard.vue 里的 EnergyStub —— 本文件无需改动。
 * 后端零新增:全部复用现有端点(equipmentLedger/equipmentMaint/scada alarm)。
 */

enum Api {
  /** 设备台账分页(EquipmentLedgerController,Shiro JWT)——统计 runStatus 用 */
  ledgerList = '/maotai/equipmentLedger/list/page',
  /** 保养工单分页(EquipmentMaintController)——按 status+planDate 自统计 */
  maintList = '/maotai/equipmentMaint/list/page',
  /** 保养统计(EquipmentMaintController#statistics:monthCompleted/pending/overdue/coverageRate) */
  maintStatistics = '/maotai/equipmentMaint/statistics',
  /** 工艺报警分页(ScadaAlarmQueryController,固定 sourceSystem='中集自控') */
  alarmList = '/maotai/scada/alarm/list',
}

/** 台账行(只列看板统计用到的字段) */
export interface LedgerRow {
  id: string;
  /** 运行状态 1在线 2停机 3维修 4报警(Integer,与台账列表 RUN_STATUS_MAP 同字典) */
  runStatus?: number;
}

/** 保养工单行(只列统计字段) */
export interface MaintRow {
  id: string;
  /** pending待执行 / progress进行中 / completed已完成 / overdue逾期 */
  status?: string;
  /** 计划日期 YYYY-MM-DD */
  planDate?: string;
  /** 完成日期 YYYY-MM-DD(已完成单统计周期用) */
  completeDate?: string;
}

/** 拉全量设备台账(按 999 上限)统计状态分布;返回原始行由页面自聚合 */
export const fetchLedgerRows = () =>
  defHttp.get<{ records: LedgerRow[] }>({ url: Api.ledgerList, params: { pageNo: 1, pageSize: 999 } });

/** 拉全量保养工单(按 999 上限);周期口径(当天/当周/当月)在页面按 planDate/completeDate 过滤 */
export const fetchMaintRows = () =>
  defHttp.get<{ records: MaintRow[] }>({ url: Api.maintList, params: { pageNo: 1, pageSize: 999 } });

/** 保养统计卡(monthCompleted 本月已完成 / pending 待执行 / overdue 逾期 / coverageRate 覆盖率) */
export const fetchMaintStatistics = () => defHttp.get<Record<string, number>>({ url: Api.maintStatistics });

/** 拉工艺报警(按 1000 上限,同工艺报警页);页面按 alarmLevel+alarmTime 周期过滤 */
export const fetchAlarmRows = (params?: { alarmLevel?: number; status?: number; keyword?: string }) =>
  defHttp.get<{ records: AlarmEventVO[] }>({
    url: Api.alarmList,
    params: { pageNo: 1, pageSize: 1000, ...params },
  });
