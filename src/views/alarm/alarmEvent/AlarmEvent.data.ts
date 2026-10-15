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
    title: '报警点位编码',
    align: 'center',
    dataIndex: 'alarmPiCode',
  },
  {
    title: '报警点位',
    align: 'center',
    dataIndex: 'alarmPointId_dictText',
  },
  {
    title: '来源系统',
    align: 'center',
    dataIndex: 'sourceSystem',
  },
  {
    title: '功能位置编码',
    align: 'center',
    dataIndex: 'funcLocCode',
  },
  {
    title: '设备',
    align: 'center',
    dataIndex: 'deviceCode_dictText',
    customRender: ({ text, record }) => {
      return text || record.deviceCode;
    },
  },
  {
    title: '报警等级',
    align: 'center',
    dataIndex: 'alarmLevel',
  },
  {
    title: '报警时间',
    align: 'center',
    dataIndex: 'alarmTime',
  },
  // {
  //   title: '报警描述',
  //   align: 'center',
  //   dataIndex: 'alarmDesc',
  // },
  {
    title: '在岗人员',
    align: 'center',
    dataIndex: 'onDutyPerson',
  },
  {
    title: '当前状态',
    align: 'center',
    dataIndex: 'status',
    customRender: ({ text }) => {
      let result = text;
      const statusItem = alarmStatusList.find((item) => item.value === text);
      if (statusItem) {
        result = statusItem.label;
      }
      return result;
    },
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂Code', order: 0, view: 'list', type: 'string', dictCode: '' },
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictCode: '' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictCode: '' },
  alarmPiCode: { title: '报警PI', order: 3, view: 'text', type: 'string' },
  alarmPointId: { title: '报警点位ID', order: 4, view: 'number', type: 'number', dictTable: 'alarm_point', dictCode: 'id', dictText: 'alarm_name' },
  sourceSystem: { title: '来源系统', order: 5, view: 'text', type: 'string' },
  funcLocCode: { title: '功能位置编码', order: 6, view: 'text', type: 'string' },
  deviceCode: { title: '设备编码', order: 7, view: 'text', type: 'string' },
  alarmLevel: { title: '报警等级', order: 8, view: 'number', type: 'number' },
  alarmTime: { title: '报警时间', order: 9, view: 'datetime', type: 'string' },
  alarmDesc: { title: '报警描述', order: 10, view: 'text', type: 'string' },
  onDutyPerson: { title: '在岗人员', order: 11, view: 'text', type: 'string' },
  status: { title: '当前状态', order: 12, view: 'text', type: 'string' },
  sort: { title: '排序号', order: 13, view: 'number', type: 'number' },
};

//报警状态(0-未响应 1-已响应 2-处理中 3-已处理)
export const alarmStatusList = [
  { value: 0, label: '未响应' },
  { value: 1, label: '已响应' },
  { value: 2, label: '处理中' },
  { value: 3, label: '已处理' },
];
