import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    width: 150,
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '关联点位',
    align: 'left',
    sorter: true,
    dataIndex: 'tagId_dictText',
    customRender: ({ text, record }) => {
      if (!text) return '';
      return `[${record.tagId}]${text}`;
    },
  },
  {
    title: '对应采集时间段',
    align: 'center',
    width: 150,
    dataIndex: 'collectTime',
    customRender: ({ text }) => {
      if (!text) return '';
      return text.split(':')[0] + ':' + text.split(':')[1];
    },
  },
  {
    title: '错误类型',
    align: 'center',
    width: 150,
    dataIndex: 'errorType_dictText',
  },
  {
    title: '详细错误信息',
    align: 'center',
    dataIndex: 'errorMsg',
  },
  {
    title: '当前重试次数',
    align: 'center',
    width: 130,
    dataIndex: 'retryCount',
  },
  {
    title: '创建时间',
    align: 'center',
    width: 180,
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '关联点位ID', order: 5, view: 'number', type: 'number', dictTable: 'mes3_pi_base_point', dictCode: 'id', dictText: 'point_name' },
  collectTime: { title: '对应采集时间段', order: 6, view: 'datetime', type: 'string' },
  errorType: {
    title: '错误类型',
    order: 7,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_CollectErrorType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  errorMsg: { title: '详细错误信息', order: 8, view: 'text', type: 'string' },
  retryCount: { title: '当前重试次数', order: 9, view: 'number', type: 'number' },
  maxRetryCount: { title: '最大重试次数', order: 10, view: 'number', type: 'number' },
  state: {
    title: '状态',
    order: 11,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_CollectErrLogState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  createTime: { title: '创建时间', order: 12, view: 'datetime', type: 'string' },
};
