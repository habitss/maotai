import { FormSchema } from '/@/components/Table';

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '标准',
    field: 'standardName',
    component: 'Input',
    slot: "standardSlot",
    required: true,
  },
  {
    label: '时间',
    field: 'searchTime',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    dynamicDisabled: ({ values }) => {
      return !values.standardName ? true : false;
    },
    required: true,
  },
  {
    label: '',
    field: 'tableName',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'fieldName',
    component: 'Input',
    show: false
  },
  {
    label: '标准id',
    field: 'standardId',
    component: 'Input',
    show: false
  },
];

//柱状图颜色配置
export const colorArray = [
  {type: '进行中', color: '#FFFF00'}, 
  {type: '已完成', color: '#00FF00'}, 
  {type: '延迟', color: '#FE0000'}, 
];

//处理设置过labelInValue的值
export function validSelectValue(value) {
  return Object.prototype.toString.call(value) === '[object Object]' ? value.key : value;
}