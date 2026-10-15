import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: 'ehs代码',
    align: "center",
    dataIndex: 'code'
  },
  {
    title: 'ehs名称',
    align: "center",
    dataIndex: 'name'
  },
  {
    title: '点位代码',
    align: "center",
    dataIndex: 'pointCode'
  },
  {
    title: '点位名称',
    align: "center",
    dataIndex: 'pointName'
  },
];

// 高级查询数据
export const superQuerySchema = {
  code: {title: 'ehs代码',order: 0,view: 'text', type: 'string',},
  name: {title: 'ehs名称',order: 1,view: 'text', type: 'string',},
  status: {title: '是否必填',order: 2,view: 'text', type: 'string',},
  pointCode: {title: '点位代码',order: 3,view: 'text', type: 'string',},
  pointName: {title: '点位名称',order: 4,view: 'text', type: 'string',},
  sort: {title: '排序',order: 5,view: 'number', type: 'number',},
};
