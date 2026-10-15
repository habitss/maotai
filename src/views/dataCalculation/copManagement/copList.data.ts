import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: 'cop设备',
    align: 'center',
    dataIndex: 'copDeviceName',
  },
  {
    //部门
    title: t('language.department'),
    dataIndex: 'deptCode_dictText',
    align: 'center',
    resizable: true,
  },
  {
    //产线
    title: t('language.productLine'),
    dataIndex: 'lineCode_dictText',
    align: 'center',
    resizable: true,
  },

  {
    title: '是否可用',
    align:"center",
    dataIndex: 'active',
    customRender:function ({ record }) {
      return String(record.active) === '1' ? "可用" : "不可用";
    }
  },
  {
    title: '顺序',
    align:"center",
    width: 100,
    dataIndex: 'sort',
  },
];

//列表页面查询表单
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
    colProps: { span: 7 },
  },
  {
    label: '设备名称',
    field: 'copDeviceName',
    component: 'Input',
    colProps: {
      span: 7,
    },
  }
];

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
    {
      label: '主键',
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      //部门
      label: t('language.department'),
      field: 'deptCode',
      component: 'Input',
      slot:'deptCode',
      required: true,
      colProps: { span: 24 },
    },
    {
      //产线
      label: t('language.productLine'),
      field: 'lineCode',
      component: 'Input',
      slot:'lineCode',
      required: true,
      colProps: { span: 24 },
    },
    {
      label: 'cop设备名称',
      field: 'copDeviceName',
      component: 'Input',
      required: true,
    },
    {
        label: '顺序',
        field: 'sort',
        component: 'InputNumber',
    },
    {
        label: '是否可用',
        field: 'active',
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
      },
  ];
