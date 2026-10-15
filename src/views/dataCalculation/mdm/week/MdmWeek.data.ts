import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';


//列表数据
export const columns: BasicColumn[] = [
  {
    title: '所属年份',
    align: "center",
    dataIndex: 'year',
    customRender:({text}) =>{
      return text+"年";
    }
  },
  {
    title: '所属月份',
    align: "center",
    dataIndex: 'month',
    customRender:({text}) =>{
      return text+"月";
    }
  },
  {
    title: '周数',
    align: "center",
    dataIndex: 'weekNumber',
    customRender:({text}) =>{
      return text+"周";
    }
  },
  {
    title: '开始日期',
    align: "center",
    dataIndex: 'startDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '结束日期',
    align: "center",
    dataIndex: 'endDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  }
];
