import { BasicColumn, FormSchema } from '/@/components/Table';

//模板配置数据默认表头
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '模板名称',
    align:"center",
    dataIndex: 'reportName'
  },
];

//模板配置数据列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: '模板名称',
    field: 'reportName',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
];


//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
  {
    label: '模板名称',
    field: 'reportName',
    component: 'Input',
    required: true,
  },
  {
    label: '类型',
    field: 'reportType',
    component: 'Input',
    defaultValue: 2,
    show: false,
  },
  {
    label: '关联表id',
    field: 'dataId',
    component: 'Input',
    show: false,
  },
  {
    label: '绑定表',
    field: 'tableId',
    component: 'Input',
    show: false,
  },
  {
    label: '表名',
    field: 'tableName',
    component: 'Input',
    show: false,
  },
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    labelWidth: 150,
    labelLength: 4,
    ifShow: false,
    colProps: { span: 24 },
  },
  {
    label: '',
    field: 'factoryCode',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'deptCode',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'lineCode',
    component: 'Input',
    show: false,
  },
];