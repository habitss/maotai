import { FormSchema } from '/@/components/Table';
import dayjs from 'dayjs';

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '报表名称',
    field: 'tableCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table, name, table_name, del_flag = 0',
        showChooseOption: false,
        showSearch: true,
        onChange: (e) => {
          formModel.fieldCodes = '';
          formModel.timeCode = '';
        },
      }
    },
    required: true,
  },
  {
    label: 'PI字段',
    field: 'fieldCodes',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table_field, name, field_name, del_flag = 0 and table_name = \''+validSelectValue(formModel.tableCode)+'\'',
        showChooseOption: false,
        showSearch: true,
        mode: 'multiple',
        maxTagCount: 3,
      }
    },
    required: true,
  },
  {
    label: '查询字段',
    field: 'timeCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table_field, name, field_name, del_flag = 0 and type = 6 and table_name = \''+validSelectValue(formModel.tableCode)+'\'',
        showChooseOption: false,
        showSearch: true,
      }
    },
    required: true,
  },
  {
    label: '开始时间',
    field: 'searchTime',
    component: 'RangePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    required: true,
    defaultValue: [
      dayjs().subtract(6, 'day').startOf('day').format("YYYY-MM-DD HH:mm:ss"),
      dayjs().endOf('day').format("YYYY-MM-DD HH:mm:ss"),
    ]
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