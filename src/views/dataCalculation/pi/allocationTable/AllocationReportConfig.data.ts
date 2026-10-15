import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '字段中文名',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: '字段名',
    align:"center",
    dataIndex: 'fieldName'
   },
   {
    title: '标准值',
    align:"center",
    dataIndex: 'showStandard',
    width:100,
   },
   {
    title: '偏差值',
    align:"center",
    dataIndex: 'showDeviation',
    width:100
   },
   {
    title: '实际值',
    align:"center",
    dataIndex: 'showActual',
    width:100
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '字段中文名',
    field: 'name',
    component: 'Input',
    colProps: {
      span: 8,
    },
  },
];