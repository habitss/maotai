import { BasicColumn } from '/@/components/Table';

//上方列表配置
export const upColumns: BasicColumn[] = [
  {
    title: '层级',
    children: [
      {
        title: 'KPI',
        dataIndex: 'title',
        key: 'title',
      },
    ],
  },
  {
    title: '',
    children: [
      {
        title: '',
        dataIndex: 'month',
        key: 'month',
      },
    ],
  },
  {
    title: '当月',
    children: [
      {
        title: '目标值',
        dataIndex: 'monthTargetValue',
      },
      {
        title: '实际值',
        dataIndex: 'monthValue',
      },
      {
        title: '差距',
        dataIndex: 'monthDiff',
        customCell : (record) => { return setDiffCellBg(record, 'monthDiff')}
      },
    ],
  },
  {
    title: '月年累',
    children: [
      {
        title: '目标值',
        dataIndex: 'yearTargetValue',
      },
      {
        title: '实际值',
        dataIndex: 'yearValue',
      },
      {
        title: '差距',
        dataIndex: 'yearDiff',
      },
    ],
  },
];

//下方列表配置
export const downColumns: BasicColumn[] = [
  {
    title: '',
    align: "center",
    dataIndex: 'monthTracking',
    width: 100,
    customCell: (record, index, column) => { 
      let rowSpan = 3;
      if(index !== 0){
        rowSpan = 0;
      }
      return {
        rowSpan,
      };
    },
  },
  {
    title: '',
    align: "center",
    dataIndex: 'indicator',
    width: 100,
  },
  {
    title: '1月',
    align: "center",
    dataIndex: 'janValue',
    width: 90,
  },
  {
    title: '2月',
    align: "center",
    dataIndex: 'febValue',
    width: 90,
  },
  {
    title: '3月',
    align: "center",
    dataIndex: 'marValue',
    width: 90,
  },
  {
    title: '4月',
    align: "center",
    dataIndex: 'aprValue',
    width: 90,
  },
  {
    title: '5月',
    align: "center",
    dataIndex: 'mayValue',
    width: 90,
  },
  {
    title: '6月',
    align: "center",
    dataIndex: 'junValue',
    width: 90,
  },
  {
    title: '7月',
    align: "center",
    dataIndex: 'julValue',
    width: 90,
  },
  {
    title: '8月',
    align: "center",
    dataIndex: 'augValue',
    width: 90,
  },
  {
    title: '9月',
    align: "center",
    dataIndex: 'sepValue',
    width: 90,
  },
  {
    title: '10月',
    align: "center",
    dataIndex: 'octValue',
    width: 90,
  },
  {
    title: '11月',
    align: "center",
    dataIndex: 'novValue',
    width: 90,
  },
  {
    title: '12月',
    align: "center",
    dataIndex: 'decValue',
    width: 90,
  },
  {
    title: '当月年累',
    align: "center",
    dataIndex: 'yearTarget',
    width: 90,
  },
];

function setDiffCellBg(cell, col){
  let bgColor = {};
  if(cell[col] > 0){
    bgColor =  {
      'background-color': '#FE0000',
      'color': 'white'
    }
  }else{
    bgColor = {
      'background-color': 'white',
      'color': 'black'
    }
  }
  return {  
    style: bgColor
  }
}