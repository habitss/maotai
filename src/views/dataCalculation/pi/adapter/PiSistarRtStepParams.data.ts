import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  // {
  //   title: '站点',
  //   align: "center",
  //   dataIndex: 'siteNo'
  // },
  // {
  //   title: '区域',
  //   align: "center",
  //   dataIndex: 'areaNo'
  // },
  {
    title: '年份',
    align: "center",
    dataIndex: 'yearNo'
  },
  {
    title: '步骤',
    align: "center",
    dataIndex: 'reccatNo_dictText'
  },
  {
    title: '订单号',
    align: "center",
    dataIndex: 'orderNo'
  },
  {
    title: '批次号',
    align: "center",
    dataIndex: 'batchNo'
  },
  
  {
    title: 'EOP',
    align: "center",
    dataIndex: 'eopNo'
  },{
    title: 'EOP名称',
    align: "center",
    dataIndex: 'eopNo_dictText'
  },
  {
    title: 'PCU名称',
    align: "center",
    dataIndex: 'pcuNo_dictText'
  },
  {
    title: 'PCU',
    align: "center",
    dataIndex: 'pcuNo'
  },
  {
    title: '开始时间',
    align: "center",
    dataIndex: 'startTime'
  },
  {
    title: 'ropId',
    align: "center",
    dataIndex: 'ropId'
  },
  {
    title: 'userProtId',
    align: "center",
    dataIndex: 'userProtId'
  },
  {
    title: 'paramPos',
    align: "center",
    dataIndex: 'paramPos'
  },
  {
    title: 'eparNo',
    align: "center",
    dataIndex: 'eparNo'
  },
  {
    title: '参数名',
    align: "center",
    dataIndex: 'paramName'
  },
  {
    title: '实际值',
    align: "center",
    dataIndex: 'actualValue'
  },
  {
    title: '设定值',
    align: "center",
    dataIndex: 'setpointValue'
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'uom'
  },
  {
    title: '写入时间',
    align: "center",
    dataIndex: 'insertTimestamp'
  }
];

