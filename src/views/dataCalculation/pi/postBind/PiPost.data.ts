import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '岗位',
    align: "center",
    dataIndex: 'postId_dictText'
  },
  {
    title: '关联字段数量',
    align: "center",
    dataIndex: 'relatedField',
    customRender:({text})=>{
      return text.split(",").length;
    }
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '岗位',
    field: 'postId',
    component: 'JDictSelectTag',
  },
  {
    label: "关联字段",
    field: 'relatedField',
    component: 'Input',
  },
  {
    label: '',
    field: 'lineCode',
    component: 'Input',
    show: false
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '岗位',
    field: 'postId',
    component: 'Select',
    required: true,
    colProps: {
      span: 12,
    },
  },
  {
    label: '关联字段',
    field: 'relatedField',
    component: 'Input',
    slot: 'fieldListSlot',
    required: true,
    colProps: {
      span: 24,
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'factoryCode',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'deptCode',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'lineCode',
    component: 'Input',
    show: false
  },
  {
    label: '关联表',
    field: 'relatedTable',
    component: 'Input',
    required: true,
    show: false
  },
];