import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '重复异常名称',
    align: "center",
    dataIndex: 'exceptionName'
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '报表名称',
    field: 'piTableId',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: `mes3_pi_customize_table where del_flag = 0, name, id`,
        showChooseOption: false,
        showSearch: true,
      }
    },
    colProps: { span: 12 },
    required: true,
    ifShow: ({ values }) => {
      return values.openType == 'copy';
    },
  },
  {
    label: '字段名称',
    field: 'piFieldId',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: `mes3_pi_customize_table_field where del_flag = 0 and table_id = '${formModel.piTableId}', name, id`,
        showChooseOption: false,
        showSearch: true,
      }
    },
    colProps: { span: 12 },
    required: true,
    ifShow: ({ values }) => {
      return values.openType == 'copy';
    },
  },
  {
    label: '重复异常名称',
    field: 'exceptionName',
    component: 'JInput',
    colProps: { span: 12 },
  },
  {
    label: '当前列表打开类型',
    field: 'openType',
    component: 'Input',
    show: false,
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '重复异常名称',
    field: 'exceptionName',
    component: 'Input',
    required: true,
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '租户ID',
    field: 'tenantId',
    component: 'Input',
    show: false
  },
  {
    label: '工厂Code',
    field: 'factoryCode',
    component: 'Input',
    show: false
  },
  {
    label: '部门Code',
    field: 'deptCode',
    component: 'Input',
    show: false
  },
  {
    label: '产线Code',
    field: 'lineCode',
    component: 'Input',
    show: false
  },
  {
    label: 'pi报表Id',
    field: 'piTableId',
    component: 'Input',
    show: false
  },
  {
    label: 'pi报表实际表名',
    field: 'piTableCode',
    component: 'Input',
    show: false
  },
  {
    label: 'pi字段id',
    field: 'piFieldId',
    component: 'Input',
    show: false
  },
  {
    label: 'pi字段实际字段名',
    field: 'piFieldCode',
    component: 'Input',
    show: false
  },
];