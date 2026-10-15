import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { list } from '/@/views/dataCalculation/kpi/type/KpiType.api';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '所属周/月/年',
    align: "center",
    dataIndex: 'dateIdentify',
    // customCell: (record, index, column) => {
    //   return { colSpan: mergeCells(record, getDataSource(), "dateIdentify", index)}; //(当前内容,原数组,key值,下标) };
    // }
  },
  {
    title: 'KPI',
    align: "center",
    dataIndex: 'kpiName',
    // customCell: (record, index, column) => {
    //   return { colSpan: mergeCells(record, getDataSource(), "kpiName", index)}; //(当前内容,原数组,key值,下标) };
    // }
  },
  {
    title: 'KPI考核结果',
    align: "center",
    dataIndex: 'color',
    slots: { customRender: 'colorNode' },
  },
  {
    title: '异常情况描述',
    align: "left",
    ellipsis: false,
    dataIndex: 'anomalyDescription'
  },
  {
    title: '原因分析',
    align: "left",
    dataIndex: 'reason'
  },
  {
    title: '整改行动',
    align: "left",
    dataIndex: 'action',
    slots: { customRender: 'actionNode' },
  },
  {
    title: '5WHY是否分析',
    align: "center",
    dataIndex: 'fiveType',
    slots: { customRender: 'fiveType' },
  },
  {
    title: '差距分析是否分析',
    align: "center",
    dataIndex: 'diffType',
    slots: { customRender: 'diffType' },
  },
  {
    title: '部门经理抽查',
    children: [
      {
        title: '是否抽查',
        dataIndex: 'managerCheck',
        width: 80,
        slots: { customRender: 'switchCell' },
      },
      {
        title: '是否合格',
        dataIndex: 'managerQualified',
        width: 80,
        slots: { customRender: 'switchCell' },
      },
    ],
  },
  {
    title: '企管部抽查',
    children: [
      {
        title: '是否抽查',
        dataIndex: 'controlCheck',
        width: 80,
        slots: { customRender: 'switchCell' },
      },
      {
        title: '是否合格',
        dataIndex: 'controlQualified',
        width: 80,
        slots: { customRender: 'switchCell' },
      },
    ],
  },


];


//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "KPI分类",
    field: "id",
    component: 'ApiTreeSelect',
    componentProps: {
      api: list,
      params: { async: false, parentId: '' },
      fieldNames: { label: 'kpiType', value: 'id' },
      showSearch: true,
      treeNodeFilterProp: 'kpiType',
    },
    //colProps: {span: 6},
  },
];


