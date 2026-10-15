import {BasicColumn} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '产线编码',
    align: "center",
    dataIndex: 'lineCode',
    width: 80,
  },
  {
    title: '产线名称',
    align: "center",
    dataIndex: 'lineName',
    width: 80,
  },
  {
    title: '设备编码',
    align: "center",
    dataIndex: 'sbId'
  },
  {
    title: '设备名称',
    align: "center",
    dataIndex: 'sbName'
  },
  {
    title: '产量点位通道号',
    align: "center",
    dataIndex: 'pointCode'
  },
  {
    title: '产量点位名称',
    align: "center",
    dataIndex: 'pointName'
  },
  {
    title: '酒机运行信号点位通道号',
    align: "center",
    dataIndex: 'pointCodeNew',
    width: 200
  },
  {
    title: '酒机运行信号点位名称',
    align: "center",
    dataIndex: 'pointNameNew',
    width: 200
  },
  {
    title: 'CIP运行信号点位通道号',
    align: "center",
    dataIndex: 'cipCode',
    width: 230
  },
  {
    title: 'CIP运行信号点位名称',
    align: "center",
    dataIndex: 'cipName',
    width: 230
  },
  {
    title: '进酒温度点位通道号',
    align: "center",
    dataIndex: 'jjwdCode',
    width: 200
  },
  {
    title: '进酒温度点位名称',
    align: "center",
    dataIndex: 'jjwdName',
    width: 200
  },
  {
    title: '线效点位通道号',
    align: "center",
    dataIndex: 'lineEfficiencyPointCode',
    width: 200
  },
  {
    title: '是否推送',
    align: "center",
    dataIndex: 'ifSend',
    customRender: ({ text }) => {
      return text == "0" ? '不推送' : text == "1" ? '推送' : '不推送';
    }
  }
];

// 高级查询数据
export const superQuerySchema = {
  lineCode: {title: '产线code',order: 0,view: 'text', type: 'string',},
  lineName: {title: '产线名称',order: 1,view: 'text', type: 'string',},
  sbId: {title: '设备id',order: 2,view: 'text', type: 'string',},
  sbName: {title: '设备名称',order: 3,view: 'text', type: 'string',},
  pointCode: {title: '原点位通道号',order: 4,view: 'text', type: 'string',},
  pointName: {title: '原点位名称',order: 5,view: 'text', type: 'string',},
  pointCodeNew: {title: '新虚拟点位通道号',order: 6,view: 'text', type: 'string',},
  pointNameNew: {title: '新虚拟点位名称',order: 7,view: 'text', type: 'string',},
  handleType: {title: '虚拟点位处理方式（0防清零/1线效状态）',order: 8,view: 'number', type: 'number',},
  initialTime: {title: '初始时间',order: 9,view: 'datetime', type: 'string',},
  initialNum: {title: '初始产量',order: 10,view: 'number', type: 'number',},
  filterUp: {title: '滤波上限',order: 11,view: 'number', type: 'number',},
  filterDown: {title: '滤波下限',order: 12,view: 'number', type: 'number',},
  ratedProduction: {title: '额定产量',order: 13,view: 'number', type: 'number',},
  ifSend: {title: '是否推送',order: 14,view: 'number', type: 'number',},
  sort: {title: '排序',order: 15,view: 'number', type: 'number',},
};
