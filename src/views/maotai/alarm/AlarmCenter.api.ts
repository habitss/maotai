import { defHttp } from '/@/utils/http/axios';

/**
 * 工艺报警接口(对接中集自控真实数据)。
 * 走 maotai 专用查询端点 /maotai/scada/alarm/list(ScadaAlarmQueryController,Shiro JWT),
 * 固定 sourceSystem='中集自控' 过滤、不做 orgCode/tenant 强制隔离
 * (不复用通用 /alarm/alarmEvent/list/page——那套按登录用户部门隔离,不适合全厂值班响应的中集工艺报警)。
 */

enum Api {
  /** 中集自控工艺报警分页(maotai 专用,ScadaAlarmQueryController) */
  queryPageList = '/maotai/scada/alarm/list',
  /** 处理工艺报警(ScadaAlarmQueryController#handle) */
  handle = '/maotai/scada/alarm/handle',
}

/** 后端 AlarmEvent 实体(只列前端用到的字段,见 AlarmEvent.java / AlarmEvent.data.ts) */
export interface AlarmEventVO {
  id: string;
  /** 报警等级 1-低 2-中 3-高 */
  alarmLevel: number;
  /** 报警时间(后端 datetime,字符串形式) */
  alarmTime: string;
  /** 报警描述 */
  alarmDesc?: string;
  /** 报警点位编码(关联 alarm_point) */
  alarmPiCode?: string;
  /** 报警点位名称(后端 join alarm_point 返回,非持久化字段;可能为空) */
  alarmName?: string;
  /** 设备编码 */
  deviceCode?: string;
  /** 工厂Code(jeecg dict → factoryCode_dictText) */
  factoryCode?: string;
  factoryCode_dictText?: string;
  /** 部门Code(jeecg dict → deptCode_dictText,前端"车间"列用) */
  deptCode?: string;
  deptCode_dictText?: string;
  /** 产线Code */
  lineCode?: string;
  lineCode_dictText?: string;
  /** 来源系统(中集自控) */
  sourceSystem?: string;
  /** 状态 0未响应 1已响应 2处理中 3已处理 */
  status: number;
  /** 处理方式(现场处理/远程复位/转派工单/标记误报) */
  handleWay?: string;
  /** 处理说明 */
  handleDesc?: string;
}

/** 列表查询参数(sourceSystem 后端固定为'中集自控',前端不传) */
export interface AlarmListParams {
  pageNo?: number;
  pageSize?: number;
  /** 级别(1/2/3,可选) */
  alarmLevel?: number;
  /** 状态(0/1/2/3,可选) */
  status?: number;
  /** 关键词(可选,模糊匹配报警描述) */
  keyword?: string;
}

/** jeecg 标准分页返回(defHttp 默认解包到这层) */
export interface AlarmListResult {
  records: AlarmEventVO[];
  total: number;
  size: number;
  current: number;
}

/**
 * 工艺报警分页列表(中集自控数据)
 */
export const listAlarms = (params: AlarmListParams) =>
  defHttp.get<{ records: AlarmEventVO[]; total: number }>({ url: Api.queryPageList, params });

/** 处理报警提交参数(字段名对齐后端 AlarmHandleRequest) */
export interface HandleAlarmParams {
  /** 报警事件主键(前端 string,后端 Integer,Jackson 自动转) */
  id: string;
  /** 处理方式:现场处理/远程复位/转派工单/标记误报 */
  handleWay: string;
  /** 处理说明(可选,最长 500) */
  handleDesc?: string;
  /** 处理状态(必传):2-处理中 3-已处理;前端按流转规则限定可选项(未处理→2/3,处理中→仅3),与处理方式解耦 */
  status: number;
}

/**
 * 处理工艺报警(落库 alarm_event:status + handle_way + handle_desc + update_by/time)。
 * status 必传(2处理中/3已处理),与处理方式解耦——处理方式只是记录性字段。
 */
export const handleAlarm = (params: HandleAlarmParams) =>
  defHttp.post({ url: Api.handle, params });
