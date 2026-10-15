import { listAll } from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTableField.api';
import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
export const piFieldList = ref([]);

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '节点名称',
    align: 'center',
    dataIndex: 'nodeName'
  },
  {
    title: '节点编码',
    align: 'center',
    dataIndex: 'nodeCode'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "节点名称",
    field: "nodeName",
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '节点名称',
    field: 'nodeName',
    component: 'Input',
    required: true,
  },
  {
    label: '节点层级',
    field: 'nodeLevel',
    component: 'Input',
    show: false
  },
  {
    label: '父级ID',
    field: 'parentId',
    component: 'Input',
    show: false,
  },
  {
    label: '父级编码',
    field: 'parentCode',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

//详细配置列表数据
export const detailColumns: BasicColumn[] = [
  {
    title: '条件字段',
    align: 'left',
    dataIndex: 'conditionField'
  },
  {
    title: '条件值',
    align: 'center',
    dataIndex: 'conditionValue'
  },
];
//详细配置表单数据
export const detailFormSchema: FormSchema[] = [
  {
    label: '目标表',
    field: 'tableCode',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = 0, name, table_name",
        showChooseOption: false,
        showSearch: true,
        onChange: async (val) => {
          formModel.fieldCode = null;
          getPiFieldList(val);
        },
      }
    },
    required: true,
  },
  {
    label: '目标字段',
    field: 'fieldCode',
    component: 'Select',
    componentProps: {
      options: piFieldList,
      placeholder: '请选择字段名称',
      showSearch: true,
      optionFilterProp: 'name',
      fieldNames: {
        label: 'name',
        value: 'fieldName',
      }
    },
    required: true,
  },
  // {
  //   label: '评价维度',
  //   field: 'evaluation',
  //   component: 'Input',
  //   required: true,
  // },
  {
    label: '上限',
    field: 'upperLimit',
    component: 'Input',
    required: true,
  },
  {
    label: '下限',
    field: 'lowerLimit',
    component: 'Input',
    required: true,
  },
  {
    label: 'CPK目标',
    field: 'targetValue',
    component: 'Input',
    required: true,
  },
  // {
  //   label: '计算时长(天)',
  //   field: 'calcTime',
  //   component: 'Input',
  //   required: true,
  // },
  {
    label: '生效日期',
    field: 'effectiveDate',
    component: 'DatePicker',
    required: true,
  },
];
export async function getPiFieldList(tableName) { 
  piFieldList.value = await listAll({
    tableName: tableName
  });
}