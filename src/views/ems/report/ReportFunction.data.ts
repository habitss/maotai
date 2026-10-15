import dayjs from 'dayjs';
import type { SelectProps } from 'ant-design-vue';
import { ref } from 'vue';

export const repShowFlagOptions = ref<SelectProps['options']>([
  { value: '1', label: '显示用量' },
  { value: '2', label: '显示底数' },
  { value: '3', label: '显示用量和底数' },
]);
export const repShowTypeOptions = ref<SelectProps['options']>([
  { value: '1', label: '先用量再底数' },
  { value: '2', label: '用量底数交替' },
]);

export const baseDataList = [
  { id: 'CollectDataHalfHour', label: '采集表半小时数据' },
  { id: 'CollectDataHour', label: '采集表小时数据' },
  { id: 'CollectDataClass', label: '采集表班数据' },
  { id: 'CollectDataDay', label: '采集表日数据' },
  { id: 'CollectDataWeek', label: '采集表周数据' },
  { id: 'CollectDataMtd', label: '采集表MTD数据' },
  { id: 'ThirdpartyHour', label: '第三方小时数据' },
  { id: 'ThirdpartyClass', label: '第三方班数据' },
  { id: 'ThirdpartyDay', label: '第三方日数据' },
  { id: 'ThirdpartyWeek', label: '第三方周数据' },
  { id: 'ThirdpartyMtd', label: '第三方MTD数据' },
  { id: 'ManualClass', label: '手输表班数据' },
  { id: 'ManualDay', label: '手输表日数据' },
  { id: 'ManualWeek', label: '手输表周数据' },
  { id: 'ManualMtd', label: '手输表MTD数据' },
  { id: 'OrsDataHour', label: '中间表小时数据' },
  { id: 'OrsDataClass', label: '中间表班数据' },
  { id: 'OrsDataDay', label: '中间表日数据' },
  { id: 'OrsDataWeek', label: '中间表周数据' },
  { id: 'OrsDataMtd', label: '中间表MTD数据' },
];

export const headProps = [
  { name: 'department', displayName: '部门', size: '*' },
  { name: 'productLine', displayName: '产线', size: '*' },
  { name: 'deviceName', displayName: '设备', size: '*' },
  { name: 'recipeType', displayName: '配方类型', size: '*' },
  { name: 'brand', displayName: '品牌', size: '1.5*' },
  { name: 'recipe', displayName: '配方', size: '3*' },
  { name: 'orderid', displayName: '订单', size: '*' },
  { name: 'batchid', displayName: '批次', size: '*' },
  { name: 'stepName', displayName: '步骤', size: '*' },
  { name: 'unit', displayName: '单位', size: 50 },
  { name: 'category', displayName: '小能源', size: '*' },
  { name: 'usage', displayName: '用量', size: '*' },
];

export const getInitialYearMonth = () => {
  return dayjs().format('YYYY-MM');
};
