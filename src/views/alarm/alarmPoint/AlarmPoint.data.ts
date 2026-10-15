import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: 'center',
    sorter: true,
    width: 100,
    dataIndex: 'factoryCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.factoryCode;
    },
  },
  {
    title: '部门',
    align: 'center',
    sorter: true,
    width: 100,
    dataIndex: 'deptCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.deptCode;
    },
  },
  {
    title: '产线',
    align: 'center',
    width: 140,
    dataIndex: 'lineCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.lineCode;
    },
  },
  {
    title: 'PI编码',
    align: 'center',
    dataIndex: 'alarmPiCode',
  },
  {
    title: '报警名称',
    align: 'center',
    dataIndex: 'alarmName',
  },
  {
    title: '报警大类',
    align: 'center',
    width: 100,
    dataIndex: 'alarmCategory',
  },
  {
    title: '报警小类',
    align: 'center',
    width: 100,
    dataIndex: 'alarmSubCategory',
  },
  {
    title: '默认报警等级',
    align: 'center',
    width: 110,
    dataIndex: 'alarmLevelDefault',
  },
  {
    title: '功能位置编码',
    align: 'center',
    dataIndex: 'funcLocCode',
  },
  {
    title: '设备',
    align: 'center',
    dataIndex: 'deviceId_dictText',
    customRender: ({ text, record }) => {
      return text || record.deviceCode;
    },
  },
  // {
  //   title: '所属系统',
  //   align: 'center',
  //   dataIndex: 'sourceSystem',
  // },
  {
    title: '是否启用',
    align: 'center',
    dataIndex: 'enableFlag',
    width: 90,
    customRender: ({ text }) => {
      return text === 0 ? '否' : '是';
    },
  },
  {
    title: '备注',
    align: 'center',
    dataIndex: 'remark',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂Code', order: 0, view: 'list', type: 'string', dictCode: '' },
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictCode: '' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictCode: '' },
  alarmPiCode: { title: 'PI编码', order: 3, view: 'text', type: 'string' },
  alarmName: { title: '报警名称', order: 4, view: 'text', type: 'string' },
  alarmCategory: { title: '报警大类', order: 5, view: 'text', type: 'string' },
  alarmSubCategory: { title: '报警小类', order: 6, view: 'text', type: 'string' },
  alarmLevelDefault: { title: '默认报警等级', order: 7, view: 'number', type: 'number' },
  funcLocCode: { title: '功能位置编码', order: 8, view: 'text', type: 'string' },
  sourceSystem: { title: '所属系统', order: 9, view: 'text', type: 'string' },
  enableFlag: { title: '是否启用', order: 10, view: 'number', type: 'number', dictCode: '' },
  remark: { title: '备注', order: 11, view: 'text', type: 'string' },
  sort: { title: '排序号', order: 12, view: 'number', type: 'number' },
};

export const yn = [
  { label: '否', value: 0 },
  { label: '是', value: 1 },
];
