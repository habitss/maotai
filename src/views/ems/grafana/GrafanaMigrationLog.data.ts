import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '源GF实例',
    align: 'center',
    dataIndex: 'sourceInstanceId_dictText',
  },
  {
    title: '源报表ID',
    align: 'center',
    dataIndex: 'sourceUid',
  },
  {
    title: '目标GF实例',
    align: 'center',
    dataIndex: 'targetInstanceId_dictText',
  },
  {
    title: '报表名称',
    align: 'center',
    dataIndex: 'dashboardTitle',
    slots: { customRender: 'targetLink' },
  },
  {
    title: '目标新报表ID',
    align: 'center',
    dataIndex: 'targetUid',
  },
  {
    title: '发布状态',
    align: 'center',
    dataIndex: 'deployState_dictText',
    width: 80,
  },
  {
    title: '发布时间',
    align: 'center',
    dataIndex: 'createTime',
    width: 180,
  },
];

// 高级查询数据
export const superQuerySchema = {
  sourceInstanceId: { title: '源GF实例ID', order: 0, view: 'text', type: 'string' },
  targetInstanceId: { title: '目标GF实例ID', order: 1, view: 'text', type: 'string' },
  dashboardTitle: { title: '报表名称', order: 2, view: 'text', type: 'string' },
  deployState: {
    title: '发布状态(0-待发布 1-成功 2-失败)',
    order: 3,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_DeplyState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  targetUid: { title: '目标新报表ID', order: 5, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 6, view: 'datetime', type: 'string' },
};
