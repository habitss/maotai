/**
 * 时间粒度类型(0-半小时 1-小时 2-班 3-天 4-周 5-月)
 */
export const enum GrainTypeEnum {
  HALFHOUR = 0,
  HOUR = 1,
  SHIFT = 2,
  DAY = 3,
  WEEK = 4,
  MONTH = 5,
  YEAR = 6,
}

/**
 * 点位大类(1-COLLECT 2-MANUAL 3-THIRDPARTY 4-ORSDATA)
 */
export const enum TagKindEnum {
  COLLECT = 1,
  MANUAL = 2,
  THIRDPARTY = 3,
  ORSDATA = 4,
}

/**
 * 阈值变化
 */
export const enum LimitColorEnum {
  RED = '#ff4d4f',
  YELLOW = '#f4e21a',
  GREEN = '#52c41a',
  GRAY = '#a3a3a3',
}
