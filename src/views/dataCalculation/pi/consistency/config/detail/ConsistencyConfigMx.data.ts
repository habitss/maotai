import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: 'pi名称',
    align: "center",
    dataIndex: 'piName',
    width: 200,
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'unit',
    width: 80,
  },
  {
    title: '指数权重(%)',
    align: "center",
    dataIndex: 'weight',
    width: 100,
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: 80,
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark'
  },
  {
    title: '已配置pi数量',
    align: "center",
    dataIndex: 'piTotal',
    width: 100,
    customRender: ({ text }) => {
      return text ? text : '0';
    },
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'pi类型',
    field: 'piType',
    component: 'RadioGroup',
    componentProps: {
      options: [
        { label: 'pi基础数据', value: '1' },
        { label: '二级指数', value: '2' }
      ],
    },
    required: true,
    ifShow: ({ values }) => {
      return values.consistencyType == '2';
    },
  },
  {
    label: 'pi编码',
    field: 'piCode',
    component: 'Select',
    required: true,
    ifShow: ({ values }) => {
      return values.piType == '2' || values.consistencyType == '1';
    },
  },
  {
    label: 'pi名称',
    field: 'piName',
    component: 'Input',
    required: true,
  },
  {
    label: '单位',
    field: 'unit',
    component: 'Input',
    required: true,
  },
  {
    label: '指数权重（%）',
    field: 'weight',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
    },
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    componentProps: {
      min: 0,
    },
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
  {
    label: '',
    field: 'consistencyType',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'zbId',
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