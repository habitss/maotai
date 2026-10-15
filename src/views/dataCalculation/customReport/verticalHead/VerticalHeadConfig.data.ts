import { FormSchema } from '/@/components/Table';
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'buttonGroup',
    component: 'InputNumber',
    slot: 'buttonGroup',
    colProps: { span: 4 },
    labelWidth: 1
  },
  {
    label: '数据开始行',
    field: 'startRow',
    component: 'InputNumber',
    required: true,
  },
  {
    label: '数据开始列',
    field: 'startCol',
    component: 'InputNumber',
    required: true,
  },
  {
    label: '查询字段',
    field: 'queryField',
    component: 'Select',
    required: true,
  },
  {
    label: '是否按班次查询',
    field: 'queryShift',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: '0',
      checkedChildren:'是',
      checkedValue: '1',
    },
    defaultValue: '0'
  },
];