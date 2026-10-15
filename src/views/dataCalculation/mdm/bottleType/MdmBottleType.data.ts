import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
//列表数据
export const columns: BasicColumn[] = [
  {
    //工厂
    title: t('language.factory'),
    align: 'center',
    dataIndex: 'factoryCodeVal',
    width: 100,
    resizable: true,
  },
  {
    //部门
    title: t('language.department'),
    align: 'center',
    dataIndex: 'deptCodeVal',
    width: 100,
    resizable: true,
  },
  {
    //产线
    title: t('language.productLine'),
    align: 'center',
    dataIndex: 'lineCodeVal',
    width: 100,
    resizable: true,
  },
  {
    //瓶型名称
    title: t('language.bottleName'),
    align: 'center',
    dataIndex: 'bottleTypeName',
    resizable: true,
  },
  {
    //瓶型编号
    title: t('language.bottleType') + t('language.coding'),
    align: 'center',
    dataIndex: 'bottleTypeCode',
    resizable: true,
  },
  {
    //排序
    title: t('language.sort'),
    align: 'center',
    dataIndex: 'sort',
    width: 70,
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
    label: t('language.bottleName'),
    field: 'bottleTypeName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('language.bottleType') + t('language.coding'),
    field: 'bottleTypeCode',
    component: 'Input',
    colProps: { span: 6 },
  },
];

//表单数据
export const addFormSchemas: FormSchema[] = [
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
    //瓶型编码
    label: t('language.bottleType') + t('language.coding'),
    field: 'bottleTypeCode',
    component: 'Input',
    componentProps: {
      disabled: false,
    },
    required: true,
  },
  {
    //瓶型名称
    label: t('language.bottleName'),
    field: 'bottleTypeName',
    component: 'Input',
    required: true,
  },
  {
    //排序
    label: t('language.sort'),
    field: 'sort',
    component: 'InputNumber',
  },
  //主键隐藏字段
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  //工厂隐藏字段
  {
    label: '',
    field: 'factoryCode',
    component: 'Input',
    show: false,
  },
];
