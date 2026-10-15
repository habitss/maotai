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
    title: 'PCU',
    align: "center",
    dataIndex: 'pcuNo_dictText'
  },
  {
    title: '设备单元',
    align: "center",
    dataIndex: 'unitNo_dictText'
  },
  {
    title: 'EOP',
    align: "center",
    dataIndex: 'eopNo'
  },
  {
    title: 'EOP名称',
    align: "center",
    dataIndex: 'eopNo_dictText'
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
    title: '事件时间',
    align: "center",
    dataIndex: 'eventTime'
  },
  
  {
    title: '监控时间',
    align: "center",
    dataIndex: 'monitoringTime'
  },
  {
    title: '持续时间',
    align: "center",
    dataIndex: 'duration'
  },
  {
    title: 'ordercatName',
    align: "center",
    dataIndex: 'ordercatName'
  },
  {
    title: 'reccatName',
    align: "center",
    dataIndex: 'reccatName'
  },
  {
    title: '配方',
    align: "center",
    dataIndex: 'recipeNo_dictText'
  },
  {
    title: '配方名称',
    align: "center",
    dataIndex: 'recipeName'
  },
  {
    title: 'recprocNo',
    align: "center",
    dataIndex: 'recprocNo'
  },
  {
    title: 'productId',
    align: "center",
    dataIndex: 'productId'
  },
  {
    title: 'productName',
    align: "center",
    dataIndex: 'productName'
  },
  {
    title: 'equipmentName',
    align: "center",
    dataIndex: 'equipmentName'
  },
  {
    title: 'equipmentId',
    align: "center",
    dataIndex: 'equipmentId'
  },
  {
    title: 'PCU名称',
    align: "center",
    dataIndex: 'pcuName'
  },
  {
    title: '设备单元名称',
    align: "center",
    dataIndex: 'unitName'
  },
  {
    title: 'EOP名称',
    align: "center",
    dataIndex: 'eopName'
  },
  {
    title: '参数计数',
    align: "center",
    dataIndex: 'paramCount'
  },
  {
    title: '步骤状态',
    align: "center",
    dataIndex: 'stepStatus'
  },
  {
    title: '写入时间',
    align: "center",
    dataIndex: 'insertTimestamp'
  },
];