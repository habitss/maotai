import { FormSchema } from '/@/components/Table';
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'buttonGroup',
    component: 'InputNumber',
    slot: 'buttonGroup',
    colProps: { span: 24 },
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
    label: '是否自动合并',
    field: 'mergeCell',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: '0',
      checkedChildren:'是',
      checkedValue: '1',
    },
    defaultValue: '0'
  },
  {
    label: '查询字段',
    field: 'queryField',
    component: 'Input',
    slot: 'queryFieldSlot',
    colProps: { span: 12 },
  },
  {
    label: '查询语句',
    field: 'querySql',
    component: 'InputTextArea',
    componentProps: { rows: 20 },
    required: true,
    colProps: { span: 12 },
  },
];