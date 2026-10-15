import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

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
    title: '检查清单名称',
    align: "center",
    dataIndex: 'configMainId_dictText'
  },
  {
    title: '填报日期',
    align: "center",
    dataIndex: 'fillingDate',
  },
  {
    title: '班次',
    align: "center",
    dataIndex: 'classes_dictText'
  },
  {
    title: '班组',
    align: "center",
    dataIndex: 'teamsGroups_dictText'
  },
  {
    title: '订单/罐号',
    align: "center",
    dataIndex: 'orderNo'
  },
  {
    title: '操作工',
    align: "center",
    dataIndex: 'operator_dictText'
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'confirm_dictText'
  },
  {
    title: '更新人',
    align: "center",
    dataIndex: 'updateBy_dictText'
  },
  {
    title: '创建人',
    align: "center",
    dataIndex: 'createBy_dictText'
  },
  {
    title: '创建时间',
    align: "center",
    dataIndex: 'createTime'
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
    label: '填报日期',
    field: 'fillingDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
  },
  {
    label: '检查清单名称',
    field: 'configName',
    component: 'Input',
  },
  {
    label: '班次',
    field: 'classes',
    component: 'KtShiftSelect',
  },
  {
    label: '班组',
    field: 'teamsGroups',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: 'mes3_mdm_shift_group,group_name,id'
    },
  },
  {
    label: '订单/罐号',
    field: 'orderNo',
    component: 'Input',
  },
];

//通用检查清单列表数据
export const generalColumns: BasicColumn[] = [
  {
    title: '步骤',
    align: "center",
    dataIndex: 'step',
    width: 80,
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '检查项目',
    align: "center",
    dataIndex: 'checkItem',
    width: '8vw',
    ellipsis: false,
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '检查标准',
    align: "center",
    dataIndex: 'checkStandardDesc',
    ellipsis: false,
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '标准检查频次说明',
    align: "center",
    dataIndex: 'frequencyIllustrate',
    width: '8vw',
    ellipsis: false,
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '标准检查频次',
    align: "center",
    dataIndex: 'frequencyIllustrateCount',
    width: '4vw',
    ellipsis: false,
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '检查数据记录',
    align: "center",
    dataIndex: 'checkname',
    ellipsis: false,
    colSpan: 2,
    customCell: (record) => {
      return { rowSpan: record.standardRowSpan };
    },
  },
  {
    title: '检查数据记录',
    align: "center",
    dataIndex: 'checkData',
    colSpan: 0,
    slots: { customRender: 'checkData' },
  },
  {
    title: '检查时间',
    align: "center",
    dataIndex: 'checkTime',
    slots: { customRender: 'checkTime' },
    customCell: (record) => {
      if(record.reportType == 1){
        return { rowSpan: 1 };
      }
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '检查照片',
    align: "center",
    dataIndex: 'checkPhoto',
    slots: { customRender: 'checkPhoto' },
  },
];