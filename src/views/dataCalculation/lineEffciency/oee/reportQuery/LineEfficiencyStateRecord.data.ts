import {BasicColumn} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '产线编码',
    align: "center",
    dataIndex: 'lineCode',
    width: 100
  },
  {
    title: '产线名称',
    align: "center",
    dataIndex: 'lineName',
    width: 100
  },
  {
    title: '设备编码',
    align: "center",
    dataIndex: 'sbId',
    width: 100
  },
  {
    title: '设备名称',
    align: "center",
    dataIndex: 'sbName',
    width: 100
  },
  {
    title: '运行状态',
    align: "center",
    dataIndex: 'stateCode',
    width: 100,
    customRender: ({ text }) => {
      return text === "0" ? '未运行' : text === "1" ? '运行' : '未知';
    }
  },
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'startTime',
    width: 200
  },
  {
    title: '实际结束时间',
    align: "center",
    dataIndex: 'endTimeActual',
    width: 200
  },
  {
    title: '理论结束时间',
    align: "center",
    dataIndex: 'endTime',
    width: 200
  },
  {
    title: '运行时长（秒）',
    align: "center",
    dataIndex: 'runningTime',
    width: 150
  },
  {
    title: '线效值',
    align: "center",
    dataIndex: 'lineEfficiencyValue'
  },
  {
    title: '产量',
    align: "center",
    dataIndex: 'production'
  },
  {
    title: '额定产量',
    align: "center",
    dataIndex: 'ratedProduction'
  },
];
