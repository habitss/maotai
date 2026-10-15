import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '来源表名称',
    align: "center",
    dataIndex: 'sourceTableName'
  },
  {
    title: '来源表',
    align: "center",
    dataIndex: 'sourceTable'
  },
  {
    title: '目标表名称',
    align: "center",
    dataIndex: 'targetTableName'
  },
  {
    title: '目标表',
    align: "center",
    dataIndex: 'targetTable'
  }
];



export const fieldColumns: BasicColumn[] = [
  {
    title: '来源表字段名称',
    align: "center",
    dataIndex: 'sourceFieldName',
    width: 160
  },
  {
    title: '来源表字段',
    align: "center",
    dataIndex: 'sourceField',
    width: 160
  },
  {
    title: '目标表字段名称',
    align: "center",
    dataIndex: 'targetFieldName',
    slots: { 
      customRender: "targetField"
    }
  },
  {
    title: '目标表字段',
    align: "center",
    dataIndex: 'targetField'
  }
  
];