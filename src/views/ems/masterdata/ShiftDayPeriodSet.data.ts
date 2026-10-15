import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: 'center',
    sorter: true,
    dataIndex: 'factoryCode_dictText',
  },
  {
    title: '时间区间',
    align: 'center',
    sorter: true,
    dataIndex: 'dataPeriod_dictText',
  },
  {
    title: '班别',
    align: 'center',
    sorter: true,
    dataIndex: 'shift_dictText',
    customRender: ({ text, record }) => {
      return record.dataPeriod == 1 ? text : '';
    },
  },
  {
    title: '结束时间点',
    align: 'center',
    dataIndex: 'hour',
    customRender: ({ text }) => {
      return text + ':00';
    },
  },
  {
    title: '区间时长(h)',
    align: 'center',
    dataIndex: 'periodTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  dataPeriod: {
    title: '时间区间',
    order: 3,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_RepDataPeriod'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  shift: {
    title: '班别',
    order: 4,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_ShiftType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  hour: { title: '结束时间点', order: 5, view: 'number', type: 'number' },
  periodTime: { title: '区间时长(h)', order: 6, view: 'number', type: 'number' },
};
