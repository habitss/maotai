import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [

  {
    title: '责任部门',
    align: "center",
    fixed: 'left',
    width: 80,
    dataIndex: 'limsDept'
  },
  {
    title: '责任生产线',
    align: "center",
    fixed: 'left',
    width: 100,
    dataIndex: 'deviceName'
  },
  {
    title: '推送时间',
    align: "center",
    fixed: 'left',
    width: 150,
    dataIndex: 'reportDate'
  },
  {
    title: '批号',
    align: "center",
    fixed: 'left',
    width: 100,
    dataIndex: 'batchNo'
  },
  {
    title: '检验类别',
    align: "center",
    fixed: 'left',
    dataIndex: 'checkCategory'
  },
  {
    title: '样品',
    align: "left",
    dataIndex: 'mtlName',
    fixed: 'left'
    // ,
    // customRender:function (text, record, index, column) {
    //   let str = text.length>50?'...':'';
    //   return str;
    // }
  },
  {
    title: '流水号',
    align: "center",
    width: 170,
    dataIndex: 'limsFlowId'
  },
  {
    title: '数据来源',
    align: "center",
    dataIndex: 'dataSource'
  },
  {
    title: '取样点',
    align: "center",
    dataIndex: 'sampledotName'
  },
  {
    title: '供应商',
    align: "center",
    dataIndex: 'vendorName'
  },
  {
    title: '反馈人',
    align: "center",
    dataIndex: 'feedPerson'
  },
  {
    title: 'KPI类别',
    align: "center",
    dataIndex: 'kpiCategory'
  },
  {
    title: 'RCI/CI/SCI',
    align: "center",
    dataIndex: 'icategory'
  },
  {
    title: '红区/黄区',
    align: "center",
    dataIndex: 'zone'
  },
  {
    title: '超标及不合格项目',
    align: "center",
    dataIndex: 'testName'
  },
  {
    title: '检测结果',
    align: "center",
    dataIndex: 'reportResult'
  },
  {
    title: '红区下限',
    align: "center",
    dataIndex: 'lowLimita'
  },
  {
    title: '黄区下限',
    align: "center",
    dataIndex: 'lowLimitb'
  },
  {
    title: '目标值',
    align: "center",
    dataIndex: 'targetValue'
  },
  {
    title: '黄区上限',
    align: "center",
    dataIndex: 'highLimitb'
  },
  {
    title: '红区上限',
    align: "center",
    dataIndex: 'highLimita'
  },
  {
    title: '字符标准',
    align: "center",
    dataIndex: 'charsTandard'
  },
  {
    title: '触发级别',
    align: "center",
    dataIndex: 'triggerLevel'
  },
  {
    title: '是否隔离',
    align: "center",
    dataIndex: 'quarantine'
  },
  {
    title: '根本原因',
    align: "center",
    width: '16vw',
    dataIndex: 'reason'
  },
  {
    title: '分析时间',
    align: "center",
    width: '9vw',
    dataIndex: 'analysisTime'
  },
  {
    title: '分析时效符合度(24h)',
    align: "center",
    width: '9vw',
    dataIndex: 'analyzeTimeliness',
    customRender: function (text) {
      let str = '';
      let analyzeTimeliness = text.record['analyzeTimeliness']
      if(analyzeTimeliness){
        str=analyzeTimeliness+"h"
      }
      return str;
    },
    customCell: function(record, rowIndex){
      let style = {}
      if(record.analyzeTimeliness){
        if(record.analyzeTimeliness <= 24){
          style["backgroundColor"] = "#00ff00";
        }else{
          style["backgroundColor"] = "red";
        }
      }
      return {style}
    }
  },

  {
    title: '审核人',
    align: "center",
    dataIndex: 'approvalName'
  },
  {
    title: '审核时间',
    align: "center",
    width: '9vw',
    dataIndex: 'mgrapprovalTime'
  },
  {
    title: '审核时效符合度(72h)',
    align: "center",
    width: '9vw',
    dataIndex: 'auditTimeliness',
    customRender: function (text) {
      let str = '';
      let auditTimeliness = text.record['auditTimeliness']
      if(auditTimeliness){
        str=auditTimeliness+"h"
      }
      return str;
    },
    customCell: function(record){
      let style = {}
      if(record.auditTimeliness){
        if(record.auditTimeliness <= 72){
          style["backgroundColor"] = "#00ff00";
        }else{
          style["backgroundColor"] = "red";
        }
      }
      return {style}
    }
  },
  {
    title: '是否作废',
    align: "center",
    dataIndex: 'cancelFlag',
    slots: { customRender: 'cancelFlag' }
  },
  {
    title: '作废原因',
    align: "center",
    dataIndex: 'cancelReason'
  },
  {
    title: '是否填写根本原因',
    align: "center",
    width: 130,
    dataIndex: 'reason',
    slots: { customRender: 'isReason' }
  },
  {
    title: '是否审批根本原因',
    align: "center",
    width: 130,
    dataIndex: 'reasonStatus', 
    slots: { customRender: 'reasonStatus' }
  },
  {
    title: '是否5why分析',
    align: "center",
    width: 110,
    dataIndex: 'fiveWhyStatus',
    slots: { customRender: 'fiveWhyStatus' }
  },
  {
    title: '是否记录行动',
    align: "center",
    width: 110,
    dataIndex: 'actionStatus',
    slots: { customRender: 'actionStatus' }
  },
  {
    title: '审核状态',
    align: "center",
    width: 90,
    dataIndex: 'examineStatus',
    slots: { customRender: 'examineStatus' }
  },
  {
    title: '完成状态',
    align: "center",
    width: 80,
    dataIndex: 'completeStatus',
    slots: { customRender: 'completeStatus' }
  },

];

