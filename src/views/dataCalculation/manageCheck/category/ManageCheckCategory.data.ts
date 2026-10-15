import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '分类名称',
    align: 'left',
    dataIndex: 'categoryName'
  },
  {
    title: '分类编码',
    align: 'center',
    dataIndex: 'code'
  },
  {
    title: '是否启用',
    align: 'center',
    dataIndex: 'ifEnable',
    customRender: ({ record }) => {
      return record.ifEnable == 1 ? '是' : '否';
    }
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy_dictText'
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '分类名称',
    field: 'categoryName',
    component: 'Input',
  },
  {
    label: '是否启用',
    field: 'ifEnable',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: 0,
      checkedChildren:'是',
      checkedValue: 1,
    },
    defaultValue: 1
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'code',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'parentCode',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'pid',
    component: 'Input',
    show: false
  },
];