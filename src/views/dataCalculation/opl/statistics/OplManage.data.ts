import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    dataIndex: 'title',
    width: 70,
    align: 'center',
    key: 'title',
  },
  {
    title: '总OPL数',
    align: 'center',
    dataIndex: 'total',
    key: 'total',
    width: 100
  },
  {
    title: '年OPL管理进度',
    align: 'center',
    children: [
      {
        title: '计划回顾数',
        align: 'center',
        dataIndex: 'planReview',
        key: 'planReview',
        width: 100
      },
      {
        title: '实际回顾数',
        align: 'center',
        dataIndex: 'realityReview',
        key: 'realityReview',
        width: 100
      },
      {
        title: '回顾完成率',
        align: 'center',
        dataIndex: 'reviewRate',
        key: 'reviewRate',
        width: 100
      },
    ]
  },
  {
    title: '月份',
    align: 'center',
    children: [
      {
        title: '计划回顾数',
        align: 'center',
        dataIndex: 'monthPlanReview',
        key: 'ninPlanReview',
        width: 100
      },
      {
        title: '实际回顾数',
        align: 'center',
        dataIndex: 'monthRealityReview',
        key: 'monthRealityReview',
        width: 100
      },
      {
        title: '回顾完成率',
        align: 'center',
        dataIndex: 'monthReviewRate',
        key: 'monthReviewRate',
        width: 100
      },
    ]
  }
];
