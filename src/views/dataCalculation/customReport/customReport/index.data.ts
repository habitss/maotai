import { FormSchema } from '/@/components/Table';

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
  {
    label: '单元格行',
    field: 'row',
    component: 'Input',
    show: false
  },
  {
    label: '单元格列',
    field: 'col',
    component: 'Input',
    show: false
  },
  {
    label: '对应字段',
    field: 'mappingColumn',
    component: 'Select',
    componentProps: {
      showSearch: true
    },
    labelWidth: 150,
    labelLength: 8,
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '填报项组件',
    field: 'componentType',
    component: 'Select',
    labelWidth: 150,
    labelLength: 8,
    colProps: { span: 24 },
    required: true
  },
  {
    label: '组件字典配置',
    field: 'dictType',
    component: 'Select',
    componentProps: {
      showSearch: true
    },
    labelWidth: 150,
    labelLength: 8,
    colProps: { span: 24 },
    required: true,
    ifShow: false,
  },
];