import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '问题名称',
    align: 'left',
    dataIndex: 'problemName'
  },
  {
    title: '是否启用',
    align: 'center',
    dataIndex: 'disabled',
    customRender: ({ record }) => {
      return record.disabled == 1 ? '否' : '是';
    },
    width: 80
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy_dictText',
    width: 120
  },
  {
    title: '创建日期',
    align: 'center',
    dataIndex: 'createTime',
    width: 180
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "问题名称",
    field: "problemName",
    component: 'Input',
    //colProps: {span: 6},
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '问题名称',
    field: 'problemName',
    component: 'Input',
    required: true,
  },
  {
    label: '是否启用',
    field: 'disabled',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: 1,
      checkedChildren:'是',
      checkedValue: 0,
    },
    defaultValue: 0
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
    field: 'parentId',
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
