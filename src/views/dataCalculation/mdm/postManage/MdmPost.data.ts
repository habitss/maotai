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
    width: 140,
  },
  {
    //部门
    title: t('language.department'),
    align: 'center',
    dataIndex: 'deptCodeVal',
    width: 140,
  },
  {
    //产线
    title: t('language.productLine'),
    align: 'center',
    dataIndex: 'lineCodeVal',
    width: 140,
  },
  {
    //岗位名称
    title: '岗位名称',
    align: 'center',
    dataIndex: 'postName',
    resizable: true,
  },
  {
    //岗位编号
    title: '岗位编码',
    align: 'center',
    dataIndex: 'postCode',
    width: 100,
  },
  {
    //岗位类型
    title: '岗位类型',
    align: 'center',
    dataIndex: 'postType',
    resizable: true,
    customRender: ({text}) => {
      return text == '1' ? '管理岗' : '非管理岗';
    },
    width: 100,
  },
  {
    //排序
    title: t('language.sort'),
    align: 'center',
    dataIndex: 'sort',
    width: 70,
  },
  {
    //备注
    title: t('language.remark'),
    align: 'left',
    dataIndex: 'remark',
  },
  {
    //主设备
    title: '关联主设备',
    align: 'center',
    dataIndex: 'mainDeviceName',
    width: 100,
  },
  {
    //副设备
    title: '关联副设备',
    align: 'center',
    dataIndex: 'deviceNames',
    width: 140,
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
    label: '岗位名称',
    field: 'postName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '岗位编码',
    field: 'postCode',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '岗位类型',
    field: 'postType',
    component: 'Select',
    componentProps: {
      options: [
        { value: '0', label: '非管理岗' },
        { value: '1', label: '管理岗' },
      ],
    },
    colProps: { span: 6 },
  },
];

//表单数据
export const addFormSchemas: FormSchema[] = [
  {
    //岗位类型
    label: '岗位类型',
    field: 'postType',
    component: 'Select',
    componentProps: {
      options: [
        { value: 0, label: '非管理岗' },
        { value: 1, label: '管理岗' },
      ],
    },
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
    dynamicRules: ({ values }) => {
      return [
        {
          required: values.postType == 0,
        },
      ];
    },
  },
  {
    //岗位编码
    label: '岗位编码',
    field: 'postCode',
    component: 'Input',
    componentProps: {
      disabled: false,
    },
    required: true,
  },
  {
    //岗位名称
    label: '岗位名称',
    field: 'postName',
    component: 'Input',
    required: true,
  },
  {
    //排序
    label: t('language.sort'),
    field: 'sort',
    component: 'InputNumber',
  },
  {
    //备注
    label: t('language.remark'),
    field: 'remark',
    component: 'InputTextArea',
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
