import { BasicColumn, FormSchema } from '/@/components/Table';

//标准管理列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '标准名称',
    align:"center",
    dataIndex: 'standardName',
    width: 200,
  },
  {
    title: '启用时间',
    align:"center",
    dataIndex: 'enableTime',
    width: 100,
  },
  {
    title: '停用时间',
    align:"center",
    dataIndex: 'stopTime',
    width: 100,
  },
  {
    title: '版本',
    align:"center",
    dataIndex: 'standardVersion',
    width: 60,
  },
  {
    title: '排序',
    align:"center",
    dataIndex: 'sort',
    width: 60,
  },
  {
    title: '重算时间对照字段',
    align:"center",
    dataIndex: 'calcTimeField_dictText',
    width: 140,
  },
  {
    title: '条件字段',
    align:"center",
    dataIndex: 'conditionText',
  },
];

//查询表单字段
export const searchFormSchemas: FormSchema[] = [
  {
    label: '标准名称',
    field: 'standardName',
    component: 'Input',
    colProps: { span: 8 },
  },
];

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: '关联的数据库表名',
    field: 'tableName',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: 'pi基础数据表Id',
    field: 'customizeId',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: '标准名称',
    field: 'standardName',
    component: 'Input',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '启用时间',
    field: 'enableTime',
    component: 'DatePicker',
    componentProps: {
      valueFormat:'YYYY-MM-DD',
    },
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '停用时间',
    field: 'stopTime',
    component: 'DatePicker',
    componentProps: {
      valueFormat:'YYYY-MM-DD',
    },
    labelWidth: 140,
    labelLength: 8,
    colProps: { span: 24 },
  },
  {
    label: '版本',
    field: 'standardVersion',
    component: 'InputNumber',
    labelWidth: 140,
    labelLength: 8,
    colProps: { span: 24 },
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    labelWidth: 140,
    labelLength: 8,
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '重算时间对照字段',
    field: 'calcTimeField',
    component: 'Select',
    labelWidth: 140,
    labelLength: 8,
    colProps: { span: 24 },
    required: true,
  },
];