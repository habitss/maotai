import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
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
    title: '总SOP数',
    align: 'center',
    dataIndex: 'total',
    key: 'total',
    width: 100
  },
  {
    title: '2024年SOP管理进度',
    align: 'center',
    children:[
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
    title: '9月',
    align: 'center',
    children:[
      {
        title: '计划回顾数',
        align: 'center',
        dataIndex: 'ninePlanReview',
        key: 'ninPlanReview',
        width: 100
      },
      {
        title: '实际回顾数',
        align: 'center',
        dataIndex: 'nineRealityReview',
        key: 'nineRealityReview',
        width: 100
      },
      {
        title: '回顾完成率',
        align: 'center',
        dataIndex: 'nineReviewRate',
        key: 'nineReviewRate',
        width: 100
      },
    ]
  }
];
