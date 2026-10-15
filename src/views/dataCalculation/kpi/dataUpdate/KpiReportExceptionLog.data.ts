import dayjs from 'dayjs';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { list as kpiTypeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
import { getColor} from '/@/utils/common/piColorUtils';

export const approveOptions = [
  { label: '审批通过', value: 1 },
  { label: '驳回', value: 2 },
  { label: '待审批', value: 3 },
]

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'objectName',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: 'KPI分项',
    align: "center",
    dataIndex: 'kpiName',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: 'KPI分类',
    align: "center",
    dataIndex: 'typeName',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '数据类型',
    align: "center",
    dataIndex: 'indicatorName',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '数据周期',
    align: "center",
    dataIndex: 'kpiDataKey',
    customRender: ({ text }) => {
      if(text.startsWith('w_')){
        const arr = text.split('_');
        return arr[1] + '第' + arr[2] + '周';
      }
      return text;
    },
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '修改原因',
    align: "center",
    dataIndex: 'updateCause',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
    width: 300,
  },
  {
    title: '类型',
    align: "center",
    dataIndex: 'dataType',
    width: 80,
  },
  {
    title: '结果值',
    align: "center",
    dataIndex: 'dataResult',
    width: 80,
  },
  {
    title: '结果等级',
    align: "center",
    dataIndex: 'dataColor',
    width: 80,
    customRender: ({ record }) => {
      switch (record.dataColor) {
        case 1:
          return '绿区';
        case 2:
          return '红区';
        case 3:
          return '黄区';
        default:
          return '';
      }
    },
    customCell(record) {
      let color = '';
      if (record.dataColor == 1) {
        color = 'green'
      } else if (record.dataColor == 2) {
        color = 'red'
      } else if (record.dataColor == 3) {
        color = 'yellow'
      }
      return {
        style: {
          'background-color': getColor(color),
        }
      }
    },
  },
  {
    title: '提出人',
    align: "center",
    dataIndex: 'createBy_dictText',
    width: 100,
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '提出时间',
    align: "center",
    dataIndex: 'createTime',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '审批状态',
    align: "center",
    dataIndex: 'approveType',
    width: 120,
    slots: { customRender: 'approveTypeSlot' },
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '审批人',
    align: "center",
    dataIndex: 'updateBy_dictText',
    width: 100,
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
  {
    title: '审批时间',
    align: "center",
    dataIndex: 'approveTime',
    customCell: (record, index, column) => {
      return { rowSpan: index % 2 == 0 ? 2 : 0 };
    },
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "年份",
    field: "kpiTypeYear",
    component: 'DatePicker',
    componentProps: ({ schema, formModel }) => {
      schema.componentProps = {
        picker: "year",
        valueFormat: "YYYY",
        'value-format': "YYYY",
        format: "YYYY",
        allowClear: false,
        onChange: (value) => {
          formModel.kpiParent = undefined;
        }
      }
    },
    defaultValue: dayjs().format('YYYY'),
  },
  {
    label: 'KPI分类',
    field: 'kpiParent',
    component: 'ApiTreeSelect',
    componentProps: ({ formModel }) => {
      return {
        api: kpiTypeList,
        params: {
          //year: formModel.kpiTypeYear ?? dayjs().format('YYYY'),
          pageSize: 999,
        },
        fieldNames: { children: 'children', label: 'kpiType', value: 'id' }
      }
    },
  },
  {
    label: '提出日期',
    field: 'createTime',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: "YYYY-MM-DD",
      placeholder: ['请选择开始日期', '请选择结束日期']
    },
  },
  {
    label: '审核状态',
    field: 'approveType',
    component: 'Select',
    componentProps: {
      options: approveOptions
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'KPI名称',
    field: 'kpiName',
    component: 'Input',
    slot: 'textSlot',
    colProps: {
      span: 12,
    },
  },
  {
    label: '数据类型',
    field: 'indicator_dictText',
    component: 'Input',
    slot: 'textSlot',
    colProps: {
      span: 12,
    },
  },
  {
    label: '原值',
    field: 'oldValue',
    component: 'Input',
    slot: 'textSlot',
    colProps: {
      span: 12,
    },
  },
  {
    label: '修改人',
    field: 'createBy_dictText',
    component: 'Input',
    slot: 'textSlot',
    colProps: {
      span: 12,
    },
  },
  {
    label: '新值',
    field: 'newValue',
    component: 'Input',
    required: true,
    colProps: {
      span: 12,
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '修改时间',
    field: 'createTime',
    component: 'Input',
    slot: 'textSlot',
    colProps: {
      span: 12,
    },
  },
  {
    label: '修改原因',
    field: 'updateCause',
    component: 'InputTextArea',
    required: true,
    colProps: {
      span: 24,
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '',
    field: 'hint',
    component: 'Input',
    slot: 'hintSlot',
    ifShow: ({ values }) => {
      return values.id;
    },
    colProps: {
      span: 24,
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];