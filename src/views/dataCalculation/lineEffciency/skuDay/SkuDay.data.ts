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
    dataIndex: 'lineName'
  },
  {
    title: '日期',
    align: "center",
    dataIndex: 'dateDay',
    customRender:function (t,r,index) {
      let str = "";
      if(t.value){
        str = t.value.substring(0, 10);
      }
      return str;
    }
  },
  {
    title: '班次',
    align: "center",
    dataIndex: 'classDes'
  },
  // {
  //   title: 'sku编码',
  //   align: "center",
  //   dataIndex: 'skuCode'
  // },
  {
    title: 'SKU名称',
    align: "center",
    dataIndex: 'skuName'
  },
  {
    title: 'SKU速度',
    align: "center",
    edit: true,
    dataIndex: 'skuSpeed'
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
    dataIndex: 'outputValue'
  }
];



export const splitColumns: BasicColumn[] = [
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
    title: 'SKU名称',
    align: "center",
    dataIndex: 'skuName'
  },
  {
    title: 'SKU速度',
    align: "center",
    dataIndex: 'skuSpeed'
  },
  // {
  //   title: '产量',
  //   align: "center",
  //   dataIndex: 'outputValue'
  // }
  
];