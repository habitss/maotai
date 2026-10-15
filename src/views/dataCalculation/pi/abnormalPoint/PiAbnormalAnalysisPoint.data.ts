import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columnsLeft: BasicColumn[] = [
  {
    title: '编号',
    align: 'left',
    dataIndex: 'serialNumber'
  },
  {
    title: '部门',
    align: 'left',
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '产线',
    align: 'left',
    dataIndex: 'lineCode_dictText',
  },
  {
    title: 'PI报表',
    align: 'left',
    dataIndex: 'tableName'
  },
  {
    title: 'PI名称',
    align: 'left',
    dataIndex: 'fieldName'
  },
];
export const columnsCenter: BasicColumn[] = [
  {
    title: '异常排查点',
    align: 'left',
    dataIndex: 'pointName'
  },
  {
    title: '标准值',
    align: 'center',
    dataIndex: 'standardValue'
  },
];
export const columnsRight: BasicColumn[] = [
  {
    title: '实际值',
    align: 'left',
    dataIndex: 'actualValue',
    slots: {
      customRender: 'actualValueSlots',
    },
  },
  {
    title: '原因分析',
    align: 'center',
    dataIndex: 'reasonAnalysis',
    slots: {
      customRender: 'reasonAnalysisSlots',
    },
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'Input',
    slot: 'deptCode',
  },
  {
    label: '产线',
    field: 'lineCode',
    slot: 'lineCode',
    component: 'Input',
  },
  {
    label: '报表',
    field: 'tableCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table, name, table_name, line_code = \'' + formModel.lineCode + '\' and del_flag = 0',
        showChooseOption: false,
        showSearch: true,
        labelInValue: true,
        onChange: e => {
          formModel.tableName = e.label[0].children;
          formModel.fieldCode = null;
        },
      }
    },
  },
  {
    label: 'PI名称',
    field: 'fieldCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table_field, name, field_name, table_name = \'' + validSelectValue(formModel.tableCode) + '\' and del_flag = 0',
        showChooseOption: false,
        showSearch: true,
        labelInValue: true,
        onChange: e => {
          formModel.fieldName = e.label[0].children;
        },
      }
    },
  },
  {
    label: '异常排查点',
    field: 'pointName',
    component: 'Input',
  },
];

//查询数据
export const searchFormSchema2: FormSchema[] = [
  {
    label: '异常排查点',
    field: 'pointName',
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'Input',
    slot: 'deptCode',
    required: true,
    show: ({ values }) => {
      return !values.parentId;
    },
  },
  {
    label: '产线',
    field: 'lineCode',
    slot: 'lineCode',
    component: 'Input',
    required: true,
    show: ({ values }) => {
      return !values.parentId;
    },
  },
  {
    label: '报表',
    field: 'tableCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table, name, table_name, line_code = \'' + formModel.lineCode + '\' and del_flag = 0',
        showChooseOption: false,
        showSearch: true,
        labelInValue: true,
        onChange: e => {
          formModel.tableName = e.label[0].children;
          formModel.fieldCode = null;
        },
      }
    },
    required: true,
    show: ({ values }) => {
      return !values.parentId;
    },
  },
  {
    label: 'PI名称',
    field: 'fieldCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'mes3_pi_customize_table_field, name, field_name, table_name = \'' + validSelectValue(formModel.tableCode) + '\' and del_flag = 0',
        showChooseOption: false,
        showSearch: true,
        labelInValue: true,
        onChange: e => {
          formModel.fieldName = e.label[0].children;
        },
      }
    },
    required: true,
    show: ({ values }) => {
      return !values.parentId;
    },
  },
  {
    label: '异常排查点',
    field: 'pointName',
    component: 'Input',
    required: true,
  },
  {
    label: '标准值',
    field: 'standardValue',
    component: 'Input',
  },
  {
    label: '编号',
    field: 'serialNumber',
    component: 'InputNumber',
    required: true,
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '工厂',
    field: 'factoryCode',
    component: 'Input',
    show: false
  },
  {
    label: 'pi报表名称',
    field: 'tableName',
    component: 'Input',
    show: false
  },
  {
    label: 'pi列名',
    field: 'fieldName',
    component: 'Input',
    show: false
  },
  {
    label: '父级',
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

//处理设置过labelInValue的值
export function validSelectValue(value) {
  return Object.prototype.toString.call(value) === '[object Object]' ? value.key : value;
}
/**
 * 获取显示列配置信息
 * @param isEmbed 是否异常分析嵌入
 */
export function getColumnsConfig(isEmbed){
  if(isEmbed){
    return [...columnsCenter, ...columnsRight];
  }
  return [...columnsLeft, ...columnsCenter];
}