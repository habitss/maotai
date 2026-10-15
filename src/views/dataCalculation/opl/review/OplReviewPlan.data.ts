import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  { title: '部门', align: "center", dataIndex: 'deptCodeVal', width: '120px', fixed: 'left', ellipsis: true },
  { title: '产线', align: "center", dataIndex: 'lineCodeVal', width: '120px', fixed: 'left', ellipsis: true },
  { title: '类型', align: "center", dataIndex: 'oplTypeVal', width: '100px', fixed: 'left', ellipsis: true },
  { title: '活动', align: "center", dataIndex: 'oplActivity', width: '140px', fixed: 'left', ellipsis: true },
  { title: '岗位', align: "center", dataIndex: 'oplPost', width: '100px', fixed: 'left', ellipsis: true },
  { title: 'OPL编号', align: "center", dataIndex: 'oplNumber', width: '120px', fixed: 'left', ellipsis: true },
  { title: 'OPL名称', align: "center", dataIndex: 'oplDesc', width: '200px', fixed: 'left', ellipsis: true },
  { title: '关键OPL', align: "center", dataIndex: 'oplState', width: '100px', slots: { customRender: 'oplStateSlots' } },
  { title: '回顾频次', align: "center", dataIndex: 'frequency', width: '120px', slots: { customRender: 'frequencySlots' } },
  { title: '上一次回顾年份', align: "center", dataIndex: 'lastYear', width: '100px' },
  { title: '回顾间隔', align: "center", dataIndex: 'reviewInterval', width: '120px', slots: { customRender: 'reviewIntervalSlots' } },
  { title: '回顾负责人', align: "center", dataIndex: 'headUserId', width: '120px', slots: { customRender: 'headUserIdSlots' } },
  { title: '回顾责任人', align: "center", dataIndex: 'responsibleUserId', width: '120px', slots: { customRender: 'responsibleUserIdSlots' } },
  { title: '回顾咨询人', align: "center", dataIndex: 'seekUserId', width: '120px', slots: { customRender: 'seekUserIdSlots' } },
  { title: '回顾通知人', align: "center", dataIndex: 'noticeUserId', width: '120px', slots: { customRender: 'noticeUserIdSlots' } }
];
