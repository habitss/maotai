import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

//配置类型
const reportType = ref('1');

//配置类型下拉框数据
const reportTypeOptions = [
  {
    label: '通用岗位记录',
    value: '1'
  },
  {
    label: '岗位检查清单',
    value: '2'
  }
];

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode_dictText'
  },
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode_dictText'
  },
  {
    title: '配置名称',
    align: "center",
    dataIndex: 'reportName'
  },
  {
    title: '配置类型',
    align: "center",
    dataIndex: 'reportType',
    customRender: ({ text }) => {
      return text ? reportTypeOptions[text-1].label : '';
    }
  },
  {
    title: '是否启用',
    align: "center",
    dataIndex: 'ifEffective',
    customRender: ({ record }) => {
      return record.ifEffective == '1' ? "是" : "否";
    }
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
    label: '部门',
    field: 'deptCode',
    component: 'Input',
    slot: 'deptCode',
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'Input',
    slot: 'lineCode',
  },
  {
    label: '配置名称',
    field: 'reportName',
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
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'Input',
    slot: 'lineCode',
    required: true,
  },
  {
    label: '配置名称',
    field: 'reportName',
    component: 'Input',
    required: true,
  },
  {
    label: '配置类型',
    field: 'reportType',
    component: 'Input',
    show: false
  },
  {
    label: '是否启用',
    field: 'ifEffective',
    component: 'Switch',
    componentProps: {
      //非选中时的内容
      unCheckedChildren: '否',
      //非选中时的值
      unCheckedValue: '0',
      //选中时的内容
      checkedChildren: '是',
      //选中时的值
      checkedValue: '1',
    },
    defaultValue: '1',
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
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}

//设置配置类型用于判断
export function setConfigReportType(value){
  reportType.value = value;
}

//配置列表数据
export const configColumns: BasicColumn[] = [
  {
    title: '步骤',
    align: "center",
    dataIndex: 'step',
    width: 100,
    slots: { customRender: 'step' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '项目检查次数',
    align: "center",
    dataIndex: 'checkFrequency',
    width: 140,
    slots: { customRender: 'checkFrequency' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
    ifShow: (column) => {
      return reportType.value === '1' ? true : false;
    },
  },
  {
    title: '标准检查次数说明',
    align: "center",
    dataIndex: 'frequencyIllustrate',
    width: 240,
    slots: { customRender: 'frequencyIllustrate' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
    ifShow: (column) => {
      return reportType.value === '1' ? true : false;
    },
  },
  {
    title: '标准检查次数',
    align: "center",
    dataIndex: 'frequencyIllustrateCount',
    width: 120,
    slots: { customRender: 'frequencyIllustrateCount' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
    ifShow: (column) => {
      return reportType.value === '1' ? true : false;
    },
  },
  {
    title: '检查项目',
    align: "center",
    width: 240,
    dataIndex: 'checkItem',
    slots: { customRender: 'checkItem' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '检查标准',
    align: "center",
    width: 240,
    dataIndex: 'checkStandardDesc',
    slots: { customRender: 'checkStandardDesc' },
    customCell: (record, index, column) => {
      return { rowSpan: record.rowSpan };
    },
    ifShow: (column) => {
      return reportType.value === '2' ? true : false;
    },
  },
  {
    title: '检查数据记录',
    align: "center",
    width: 240,
    dataIndex: 'checkname',
    slots: { customRender: 'checkname' },
    colSpan: 2,
  },
  {
    title: '检查数据记录',
    align: "center",
    dataIndex: 'checknameFormat',
    width: 140,
    slots: { customRender: 'checknameFormat' },
    colSpan: 0,
  },
  {
    title: '是否必填',
    align: "center",
    width: 120,
    dataIndex: 'checknameIfmust',
    slots: { customRender: 'checknameIfmust' },
  },
  {
    title: '排序',
    align: "center",
    width: 130,
    dataIndex: 'sort',
    slots: { customRender: 'sort' },
  },
  {
    title: '项目id',
    dataIndex: 'id',
    ifShow: false,
  },
  {
    title: '标准id',
    dataIndex: 'bzid',
    ifShow: false,
  },
];