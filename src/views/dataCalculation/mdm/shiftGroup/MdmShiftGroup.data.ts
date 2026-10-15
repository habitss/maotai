import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    //部门
    title: t('language.department'),
    dataIndex: 'deptCodeVal',
    align: 'center',
    width: 100,
    resizable: true,
  },
  {
    //产线
    title: t('language.productLine'),
    dataIndex: 'lineCodeVal',
    align: 'center',
    width: 100,
    resizable: true,
  },
  {
    title: '班组名称',
    align: "center",
    width: 200,
    dataIndex: 'groupName'
  },
  {
    title: '排序',
    align: "center",
    width: 80,
    dataIndex: 'sort'
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 6 },
  },
  {
    label: "班组名称",
    field: "groupName",
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    //部门
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    required: true,
  },
  {
    //产线
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    required: true,
  },
  {
    label: '班组名称',
    field: 'groupName',
    component: 'Input',
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
  {
    label: '',
    field: 'factoryCode',
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