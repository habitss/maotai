import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '班次类别',
    align: "center",
    dataIndex: 'shiftTypeId_dictText'
  },
  {
    title: '班次名称',
    align: "center",
    dataIndex: 'shiftName'
  },
  {
    title: '上班时间',
    align: "center",
    dataIndex: 'shiftStartTime'
  },
  {
    title: '下班时间',
    align: "center",
    dataIndex: 'shiftEndTime'
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
  {
    label: "班次名称",
    field: 'shiftName',
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '班次名称',
    field: 'shiftName',
    component: 'Input',
    required: true,
  },
  {
    label: '上班时间',
    field: 'shiftStartTime',
    required: true,
    component: 'TimePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'HH:mm:ss',
      format: 'HH:mm:ss'
    },
  },
  {
    label: '下班时间',
    field: 'shiftEndTime',
    required: true,
    component: 'TimePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'HH:mm:ss',
      format: 'HH:mm:ss'
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
    label: '班次类别id',
    field: 'shiftTypeId',
    component: 'Input',
    show: false,
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
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}