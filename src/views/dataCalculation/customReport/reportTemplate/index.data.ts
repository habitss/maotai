import { BasicColumn, FormSchema } from '/@/components/Table';
import { list as getCustomReport } from '/@/views/dataCalculation/customReport/reportConfig/reportConfig.api';

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
  {
    title: '绑定表',
    align:"center",
    dataIndex: 'tableId_dictText'
  },
  {
    title: '表名',
    align:"center",
    dataIndex: 'tableName'
  }
];

//模板配置数据列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: '模板名称',
    field: 'reportName',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '绑定表',
    field: 'tableId',
    component: 'ApiSelect',
    componentProps: {
      api: getCustomReport,
      labelField: 'name',
      valueField: 'id',
      params:{pageSize: 999},
      resultField:'records',
    },
    colProps: {
      span: 6,
    },
  },
];


//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
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
    label: '模板名称',
    field: 'reportName',
    component: 'Input',
    labelWidth: 150,
    labelLength: 4,
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '绑定表',
    field: 'tableId',
    component: 'ApiSelect',
    componentProps: {
      api: getCustomReport,
      labelField: 'name',
      valueField: 'id',
      params:{pageSize: 999},
      resultField:'records',
      getPopupContainer: () => document.body,
    },
    labelWidth: 150,
    labelLength: 4,
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '表名',
    field: 'tableName',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    labelWidth: 150,
    labelLength: 8,
    colProps: { span: 24 },
    required: true,
  },
];