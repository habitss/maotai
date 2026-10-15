import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
const consistencyTypes: any[] = [
  { label: '一级指数', value: '1' },
  { label: '二级指数', value: '2' }
]

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '指数名称',
    align: "center",
    dataIndex: 'consistencyName',
    width: 200,
  },
  {
    title: '指数类型',
    align: "center",
    dataIndex: 'consistencyType',
    customRender: ({ record }) => {
      return record.consistencyType === '1' ? "一级指数" : "二级指数";
    },
    width: 200,
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: 80,
  },
  {
    title: '备注',
    align: "left",
    dataIndex: 'remark'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '指数名称',
    field: 'consistencyName',
    component: 'Input',
  },
  {
    label: '指数类型',
    field: 'consistencyType',
    component: 'Select',
    componentProps: {
      options: consistencyTypes
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '指数名称',
    field: 'consistencyName',
    component: 'Input',
    required: true,
  },
  {
    label: '指数类型',
    field: 'consistencyType',
    component: 'RadioGroup',
    componentProps: {
      options: consistencyTypes
    },
    required: true,
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}