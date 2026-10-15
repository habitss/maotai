import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '事件开始时间',
    align: 'center',
    dataIndex: 'startTime',
    width: 160,
  },
  {
    title: '事件结束时间',
    align: 'center',
    dataIndex: 'endTime',
    width: 160,
  },
  {
    title: '指标PI值',
    align: 'center',
    dataIndex: 'piValue',
  },
  {
    title: '其他参数',
    align: 'center',
    dataIndex: 'dataJson',
  },
  {
    title: 'EOP编号',
    align: 'center',
    dataIndex: 'eopId',
  },
  {
    title: '指标实例编码',
    align: 'center',
    dataIndex: 'piCode',
  },
  {
    title: '指标名称',
    align: 'center',
    dataIndex: 'piName',
  },
  {
    title: '指标类型',
    align: 'center',
    dataIndex: 'piType',
  },
  {
    title: 'UDE规则编码',
    align: 'center',
    dataIndex: 'ruleId',
  },
  {
    title: '运行模式状态',
    align: 'center',
    dataIndex: 'status',
  },
  {
    title: '步骤号',
    align: 'center',
    dataIndex: 'stepId',
  },
  {
    title: '批次号',
    align: 'center',
    dataIndex: 'batchId',
  },
  {
    title: '订单号',
    align: 'center',
    dataIndex: 'orderId',
  },
  {
    title: '运行状态',
    align: 'center',
    dataIndex: 'running',
  },
  {
    title: '实例ID',
    align: 'center',
    dataIndex: 'thingId',
  },
  {
    title: '配方号',
    align: 'center',
    dataIndex: 'recipeId',
  },
  {
    title: 'UDE规则描述',
    align: 'center',
    dataIndex: 'ruleName',
  },
  {
    title: '配方类型ID',
    align: 'center',
    dataIndex: 'recipeTypeId',
  },
  {
    title: '最后更新时间',
    align: 'center',
    dataIndex: 'updateTime',
    width: 160,
  },
];

// 高级查询数据
export const superQuerySchema = {
  startTime: { title: '事件开始时间', order: 1, view: 'datetime', type: 'string' },
  endTime: { title: '事件结束时间', order: 2, view: 'datetime', type: 'string' },
  piValue: { title: '指标PI值', order: 3, view: 'text', type: 'string' },
  dataJson: { title: '其他参数', order: 4, view: 'textarea', type: 'string' },
  eopId: { title: 'EOP编号', order: 5, view: 'number', type: 'number' },
  piCode: { title: '指标实例编码', order: 6, view: 'text', type: 'string' },
  piName: { title: '指标名称', order: 7, view: 'text', type: 'string' },
  piType: { title: '指标类型', order: 8, view: 'text', type: 'string' },
  ruleId: { title: 'UDE规则编码', order: 9, view: 'text', type: 'string' },
  status: { title: '运行模式状态', order: 10, view: 'number', type: 'number' },
  stepId: { title: '步骤号', order: 11, view: 'number', type: 'number' },
  batchId: { title: '批次号', order: 12, view: 'number', type: 'number' },
  orderId: { title: '订单号', order: 13, view: 'number', type: 'number' },
  running: { title: '运行状态', order: 14, view: 'number', type: 'number' },
  thingId: { title: '实例ID', order: 15, view: 'text', type: 'string' },
  recipeId: { title: '配方号', order: 16, view: 'number', type: 'number' },
  ruleName: { title: 'UDE规则描述', order: 17, view: 'text', type: 'string' },
  recipeTypeId: { title: '配方类型ID', order: 18, view: 'number', type: 'number' },
  updateTime: { title: '最后更新时间', order: 20, view: 'datetime', type: 'string' },
};
