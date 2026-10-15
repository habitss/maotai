import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: 'center',
    sorter: true,
    dataIndex: 'factoryCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.factoryCode;
    },
  },
  {
    title: '部门',
    align: 'center',
    sorter: true,
    dataIndex: 'deptCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.deptCode;
    },
  },
  {
    title: '产线',
    align: 'center',
    dataIndex: 'lineCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.lineCode;
    },
  },
  {
    title: '来源系统',
    align: 'center',
    dataIndex: 'sourceSystem',
  },
  {
    title: '请求ID',
    align: 'center',
    dataIndex: 'requestId',
  },
  {
    title: '报警PI',
    align: 'center',
    dataIndex: 'alarmPiCode_dictText',
  },
  {
    title: '接入状态',
    align: 'center',
    dataIndex: 'ingestStatus',
  },
  {
    title: '错误信息',
    align: 'center',
    dataIndex: 'errorMsg',
  },
  {
    title: '接收时间',
    align: 'center',
    dataIndex: 'receiveTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂Code', order: 0, view: 'list', type: 'string', dictCode: '' },
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictCode: '' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictCode: '' },
  sourceSystem: { title: '来源系统', order: 3, view: 'text', type: 'string' },
  requestId: { title: '请求ID', order: 4, view: 'text', type: 'string' },
  alarmPiCode: {
    title: '报警PI',
    order: 5,
    view: 'list',
    type: 'string',
    dictTable: 'alarm_point',
    dictCode: 'alarm_point_code',
    dictText: 'alarm_name',
  },
  ingestStatus: { title: '接入状态', order: 6, view: 'text', type: 'string' },
  errorMsg: { title: '错误信息', order: 7, view: 'text', type: 'string' },
  receiveTime: { title: '接收时间', order: 8, view: 'datetime', type: 'string' },
  sort: { title: '排序号', order: 9, view: 'number', type: 'number' },
};
