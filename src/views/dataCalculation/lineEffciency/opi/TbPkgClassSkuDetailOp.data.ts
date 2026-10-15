import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '产线',
    align: "center",
    dataIndex: 'line'
  },
  {
    title: '日期',
    align: "center",
    dataIndex: 'date'
  },
  {
    title: '班次',
    align: "center",
    dataIndex: 'classtime'
  },
  {
    title: 'SKU',
    align: "center",
    dataIndex: 'sku'
  },
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'st',
    width: 160
  },
  {
    title: '结束时间',
    align: "center",
    dataIndex: 'et',
    width: 160
  },
  {
    title: '产量',
    align: "center",
    dataIndex: 'detail'
  },
  {
    title: '速度',
    align: "center",
    dataIndex: 'speed'
  },
  {
    title: '手动录入速度',
    align: "center",
    dataIndex: 'speedOp'
  },
];



export const splitColumns: BasicColumn[] = [
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'st',
    width: 160
  },
  {
    title: '结束时间',
    align: "center",
    dataIndex: 'et',
    width: 160
  },
  {
    title: '产量',
    align: "center",
    dataIndex: 'detail'
  },
  {
    title: '速度',
    align: "center",
    dataIndex: 'speed'
  },
  {
    title: '手动录入速度',
    align: "center",
    dataIndex: 'speedOp'
  }
];
