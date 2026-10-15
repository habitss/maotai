import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂代码',
    align: "center",
    dataIndex: 'code'
  },
  {
    title: '工厂名称',
    align: "center",
    dataIndex: 'name'
  },
  {
    title: '是否进行1秒拆分',
    align: "center",
    dataIndex: 'ifSplitSecond',
    customRender: ({ text }) => {
      return text == "0" ? '不拆分' : text == "1" ? '拆分' : '不拆分';
    }
  },
];

// 高级查询数据
export const superQuerySchema = {
  code: {title: 'ehs代码',order: 0,view: 'text', type: 'string',},
  name: {title: 'ehs名称',order: 1,view: 'text', type: 'string',},
  status: {title: '是否必填',order: 2,view: 'text', type: 'string',},
  sort: {title: '排序',order: 3,view: 'number', type: 'number',},
};
