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
    title: '报表名称',
    align:"center",
    dataIndex: 'name',
  },
  {
    title: '表名',
    align:"center",
    dataIndex: 'tableName',
  },
  {
    title: '备注说明',
    align:"center",
    dataIndex: 'notes',
  },
  {
    title: '排序',
    align:"center",
    width: 80,
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
    colProps: { span: 6 },
  },
  {
    label: '报表名称',
    field: 'name',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '表名',
    field: 'tableName',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
];