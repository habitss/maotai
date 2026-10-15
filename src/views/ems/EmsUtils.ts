import { LimitColorEnum } from '@/enums/EmsEnum';

/**
 * 根据限值判断值的颜色
 * @param value
 * @param redUppLimit
 * @param redLowLimit
 * @param yellowUppLimit
 * @param yellowLowLimit
 */
export const judgeValueColor = (value, redUppLimit, redLowLimit, yellowUppLimit, yellowLowLimit) => {
  if (!value || isNaN(Number(value))) {
    return LimitColorEnum.GRAY; // 无数据
  }
  if ((redUppLimit && Number(value) >= redUppLimit) || (redLowLimit && Number(value) <= redLowLimit)) {
    return LimitColorEnum.RED; // 红色
  }
  if ((yellowUppLimit && Number(value) >= yellowUppLimit) || (yellowLowLimit && Number(value) <= yellowLowLimit)) {
    return LimitColorEnum.YELLOW; // 黄色
  }
  if (redUppLimit || redLowLimit || yellowUppLimit || yellowLowLimit) {
    return LimitColorEnum.GREEN; // 绿色
  }
  return LimitColorEnum.GRAY;
};
