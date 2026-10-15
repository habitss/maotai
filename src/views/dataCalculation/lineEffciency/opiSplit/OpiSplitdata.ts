import {BasicColumn} from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineName'
  },
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'startTime'
  },
  {
    title: '结束时间',
    align: "center",
    dataIndex: 'endTime'
  },
  {
    title: '运行时长',
    align: "center",
    dataIndex: 'runningTime'
  },
  {
    title: '产量',
    align: "center",
    dataIndex: 'production'
  },
  {
    title: 'OPI',
    align: "center",
    dataIndex: 'actualEfficiencyValue'
  },
];



export const skuColumns: BasicColumn[] = [
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineName'
  },
  {
    title: 'SKU',
    align: "center",
    dataIndex: 'skuName'
  },
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'startTime',
    width: 160
  },
  {
    title: '结束时间',
    align: "center",
    dataIndex: 'endTime',
    width: 160
  },
  {
    title: '运行时长',
    align: "center",
    dataIndex: 'runningTime'
  },
  {
    title: '产量',
    align: "center",
    dataIndex: 'outputValue'
  },
  {
    title: '速度',
    align: "center",
    edit: true,
    dataIndex: 'skuSpeed'
  },
  {
    title: 'OPI',
    align: "center",
    dataIndex: 'opi'
  },
];