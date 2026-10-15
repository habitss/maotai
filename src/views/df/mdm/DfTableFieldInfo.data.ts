import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: "center",
    width: 120,
    dataIndex: 'factoryCode_dictText'
  },
  {
    title: '部门',
    align: "center",
    width: 120,
    dataIndex: 'deptCode_dictText'
  },
  {
    title: '产线',
    align: "center",
    width: 120,
    dataIndex: 'lineCode_dictText'
  },
  {
    title: '字段名',
    align: "center",
    dataIndex: 'dfFieldName'
  },
  {
    title: '字段描述',
    align: "center",
    dataIndex: 'dfFieldDesc'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '字段名',
    field: 'dfFieldName',
    component: 'JInput',
  },
  {
    label: '字段描述',
    field: 'dfFieldDesc',
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '字段名',
    field: 'dfFieldName',
    component: 'Input',
    componentProps: {
      disabled: true,
    }
  },
  {
    label: '字段描述',
    field: 'dfFieldDesc',
    component: 'Input',
    required: true,
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];