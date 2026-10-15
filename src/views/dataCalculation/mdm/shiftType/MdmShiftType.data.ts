import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    dataIndex: 'factoryCode_dictText',
    align: "center",
    width: 100,
  },
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
    title: '班次类别名称',
    dataIndex: 'shiftTypeName',
    align: "center",
    width: 200,
  },
  {
    title: '是否启用',
    dataIndex: 'stateFlag',
    align: "center",
    width: 100,
    customRender: ({ record }) => {
      return record.stateFlag === '1' ? "是" : "否";
    }
  },
  {
    title: '排序',
    dataIndex: 'sort',
    align: "center",
    width: 80,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    align: "center",
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
    label: "班次类别名称",
    field: "shiftTypeName",
    component: 'Input',
  },
  {
    label: "是否启用",
    field: 'stateFlag',
    component: 'Select',
    componentProps: {
      options: [
        { value: '1', label: '是' },
        { value: '0', label: '否' },
      ],
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: t('language.factory'),
    field: 'factoryCode',
    component: 'Input',
    slot: 'factoryCode',
    required: true,
  },
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
    label: '班次类别名称',
    field: 'shiftTypeName',
    component: 'Input',
    required: true,
  },
  {
    label: '是否启用',
    field: 'stateFlag',
    component: 'Switch',
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'否',
      //非选中时的值
      unCheckedValue:'0',
      //选中时的内容
      checkedChildren:'是',
      //选中时的值
      checkedValue:'1',
    },
    defaultValue: '1',
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
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}