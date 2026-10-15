/**
 * 工艺报警 mock 数据。
 * maotai 现无后端报警模块,按设计稿(alarm-center.html)固化示例数据供页面演示。
 * 后续报警数据源确定后,把 AlarmCenter.vue 里对 ALARM_LIST 的引用换成真实接口即可。
 */

export type AlarmLevel = 'critical' | 'warning' | 'info';
export type AlarmStatus = '未处理' | '处理中' | '已处理';

export interface AlarmRecord {
  id: string;
  time: string;
  level: AlarmLevel;
  content: string;
  equipment: string;
  workshop: string;
  status: AlarmStatus;
  /** 处理方式(已处理过才有:现场处理/远程复位/转派工单/标记误报) */
  handleWay?: string;
  /** 处理说明(已处理过才有) */
  handleDesc?: string;
  /** 视频联动摄像头(海康 cameraIndexCode;mock 为占位编码,host 配了再换真码) */
  cameraIndexCode: string;
  /** 摄像头位置名 */
  cameraName: string;
}

export const LEVEL_TEXT: Record<AlarmLevel, string> = {
  critical: '紧急',
  warning: '警告',
  info: '提示',
};

export const ALARM_LIST: AlarmRecord[] = [
  { id: '1', time: '2026-06-04 10:23', level: 'critical', content: 'T-108储罐液位超过安全上限(95.2%),存在溢罐风险', equipment: 'T-108', workshop: '中转酒罐车间', status: '未处理', cameraIndexCode: 'CAM-A03', cameraName: '中转酒罐车间A区' },
  { id: '2', time: '2026-06-04 09:15', level: 'critical', content: '12号勾调罐酒精度偏差超标,实测51.8%vol(标准53.0±0.5)', equipment: '12号勾调罐', workshop: '系列酒勾调厂房', status: '未处理', cameraIndexCode: 'CAM-B02', cameraName: '系列酒勾调厂房-勾调区' },
  { id: '3', time: '2026-06-04 08:45', level: 'warning', content: 'T-236储罐温度持续偏高,当前28.5°C(上限25°C),影响酒液品质', equipment: 'T-236', workshop: '成品酒罐房', status: '处理中', cameraIndexCode: 'CAM-C05', cameraName: '成品酒罐房-储罐区' },
  { id: '4', time: '2026-06-04 07:30', level: 'warning', content: '勾调配比流量波动异常,批次A-0624配比偏差达±1.2%', equipment: '勾调系统-03', workshop: '系列酒勾调厂房', status: '处理中', cameraIndexCode: 'CAM-B08', cameraName: '系列酒勾调厂房-配比区' },
  { id: '5', time: '2026-06-03 22:10', level: 'warning', content: 'T-319储罐液位快速下降,2小时内下降15%,疑似泄漏', equipment: 'T-319', workshop: '高位酒罐区', status: '未处理', cameraIndexCode: 'CAM-D01', cameraName: '高位酒罐区-东侧' },
  { id: '6', time: '2026-06-03 18:20', level: 'info', content: 'T-115储罐入库操作完成,累计入库12.5吨,酒液温度21.8°C正常', equipment: 'T-115', workshop: '中转酒罐车间', status: '已处理', cameraIndexCode: 'CAM-A06', cameraName: '中转酒罐车间B区' },
  { id: '7', time: '2026-06-03 14:05', level: 'info', content: '5号勾调罐搅拌工艺完成,总酸1.48g/L、总酯3.82g/L均达标', equipment: '5号勾调罐', workshop: '系列酒勾调厂房', status: '已处理', cameraIndexCode: 'CAM-B04', cameraName: '系列酒勾调厂房-搅拌区' },
  { id: '8', time: '2026-06-03 10:30', level: 'info', content: 'T-258储罐出库调拨完成,调出8.6吨至包装线,液位降至42.3%', equipment: 'T-258', workshop: '成品酒罐房', status: '已处理', cameraIndexCode: 'CAM-C09', cameraName: '成品酒罐房-出库区' },
];

/** 统计卡片数值(从 mock 数据派生;处理率按设计稿写死) */
export function getAlarmStats(list: AlarmRecord[]) {
  const pending = (lv: AlarmLevel) => list.filter((a) => a.level === lv && a.status !== '已处理').length;
  return {
    critical: pending('critical'),
    warning: pending('warning'),
    info: list.filter((a) => a.level === 'info').length,
    rate: '96.2',
  };
}
