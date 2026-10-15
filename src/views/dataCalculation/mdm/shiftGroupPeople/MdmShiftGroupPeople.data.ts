import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '班组名称',
    align: "center",
    dataIndex: 'shiftGroupId_dictText'
  },
  {
    title: '人员名称',
    align: "center",
    dataIndex: 'userId_dictText'
  },
  {
    title: '排序',
    align: "center",
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
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '人员分配',
    field: 'userId',
    component: 'JSelectUser',
    componentProps:{
      labelKey:'realname',
      rowKey:'id',
      selectType: 'All',
      excludeUserIdList: []
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
    component: 'Input',
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    //工厂
    label: t('language.factory'),
    field: 'factoryCode',
    component: 'Input',
    show: false,
  },
  {
    //部门
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    show: false,
  },
  {
    //产线
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    show: false,
  },
  {
    label: '班组名称',
    field: 'shiftGroupId',
    component: 'Input',
    show: false,
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