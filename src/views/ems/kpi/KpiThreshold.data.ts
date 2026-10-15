import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: 'KPI点ID',
    align: 'center',
    dataIndex: 'kpiTagId',
  },
  {
    title: '点位大类',
    align: 'center',
    dataIndex: 'tagKind',
  },
  {
    title: '点位ID',
    align: 'center',
    dataIndex: 'tagId',
  },
  {
    title: '生效月份',
    align: 'center',
    dataIndex: 'yearmonth',
  },
  {
    title: '红色下线',
    align: 'center',
    dataIndex: 'rlbelow',
  },
  {
    title: '黄色上线',
    align: 'center',
    dataIndex: 'ylover',
  },
  {
    title: '红色上线',
    align: 'center',
    dataIndex: 'rlover',
  },
  {
    title: '黄色下线',
    align: 'center',
    dataIndex: 'ylbelow',
  },
  {
    title: '标准值',
    align: 'center',
    dataIndex: 'standard',
  },
  {
    title: 'KPI阈值类型',
    align: 'center',
    dataIndex: 'vmold_dictText',
  },
  {
    title: '产量',
    align: 'center',
    dataIndex: 'yields',
  },
  {
    title: 'BU用量',
    align: 'center',
    dataIndex: 'buUsage',
  },
];

// 高级查询数据
export const superQuerySchema = {
  kpiTagId: { title: 'KPI点ID', order: 0, view: 'number', type: 'number' },
  tagKind: { title: '点位大类', order: 1, view: 'number', type: 'number' },
  tagId: { title: '点位ID', order: 2, view: 'text', type: 'string' },
  yearmonth: { title: '生效月份', order: 3, view: 'text', type: 'string' },
  rlbelow: { title: '红色下线', order: 4, view: 'number', type: 'number' },
  ylover: { title: '黄色上线', order: 5, view: 'number', type: 'number' },
  rlover: { title: '红色上线', order: 6, view: 'number', type: 'number' },
  ylbelow: { title: '黄色下线', order: 7, view: 'number', type: 'number' },
  standard: { title: '标准值', order: 8, view: 'number', type: 'number' },
  vmold: {
    title: 'KPI阈值类型',
    order: 9,
    view: 'list',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_Vmold'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  yields: { title: '产量', order: 10, view: 'number', type: 'number' },
  buUsage: { title: 'BU用量', order: 11, view: 'number', type: 'number' },
};
