import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '设备类型名称',
    align: 'left',
    dataIndex: 'deviceTypeName'
  },
  {
    title: '设备类型编码',
    align: 'center',
    dataIndex: 'deviceTypeCode'
  },
  {
    title: '排序',
    align: 'center',
    dataIndex: 'sort',
    width: '60px',
  },
  {
    title: '备注',
    align: 'center',
    dataIndex: 'remark'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '设备类型名称',
    field: 'deviceTypeName',
    component: 'Input',
  },
  {
    label: '设备类型编码',
    field: 'deviceTypeCode',
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '设备类型名称',
    field: 'deviceTypeName',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入设备类型名称!' },
      ];
    },
  },
  {
    label: '设备类型编码',
    field: 'deviceTypeCode',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入设备类型编码!' },
      ];
    },
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '备注',
    field: 'remark',
    component: 'Input',
  },
  {
    label: '父级节点',
    field: 'pid',
    component: 'JTreeSelect',
    componentProps: {
      dict: "mes3_mdm_device_type,device_type_name,id",
      pidField: "pid",
      pidValue: "0",
      hasChildField: "has_child",
    },
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
