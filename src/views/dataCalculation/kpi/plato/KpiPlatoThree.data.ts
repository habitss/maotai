import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  
  // {
  //   title: 'PI名称',
  //   align: "center",
  //   dataIndex: 'piName'
  // },
  {
    title: '控制点',
    align: "center",
    dataIndex: 'parentCodeVal'
  },
  {
    title: 'KPI名称',
    align: "center",
    dataIndex: 'superCodeVal'
  },
  {
    title: '控制活动',
    align: "center",
    dataIndex: 'controlActivity'
  },
  {
    title: '标准',
    align: "center",
    dataIndex: 'standard'
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'unit'
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remarks'
  }
];
