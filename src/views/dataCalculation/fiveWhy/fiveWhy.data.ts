import { BasicColumn } from '/@/components/Table';

// 完成情况确认下拉框数据
export const finishVerifyStateOptions = [
  { label: '待确认', value: '1', },
  { label: '合格', value: '2', },
  { label: '不合格', value: '3', },
];
//列宽
export const columnWidth = 240;
//5W列配置
export const columns: BasicColumn[] = [
  {
    title: '问题',
    dataIndex: 'problem',
    align: "center",
    width: 140,
  },
  {
    title: '问题描述',
    dataIndex: 'problemDesc',
    align: "center",
    width: columnWidth,
  },
  {
    title: '1.为什么',
    dataIndex: 'oneWhy',
    align: "center",
    width: columnWidth,
  },
  {
    title: '1.原因',
    dataIndex: 'oneReason',
    align: "center",
    width: columnWidth,
  },
  {
    title: '2.为什么',
    dataIndex: 'twoWhy',
    align: "center",
    width: columnWidth,
  },
  {
    title: '2.原因',
    dataIndex: 'twoReason',
    align: "center",
    width: columnWidth,
  },
  {
    title: '3.为什么',
    dataIndex: 'threeWhy',
    align: "center",
    width: columnWidth,
  },
  {
    title: '3.原因',
    dataIndex: 'threeReason',
    align: "center",
    width: columnWidth,
  },
  {
    title: '4.为什么',
    dataIndex: 'fourWhy',
    align: "center",
    width: columnWidth,
  },
  {
    title: '4.原因',
    dataIndex: 'fourReason',
    align: "center",
    width: columnWidth,
  },
  {
    title: '5.为什么',
    dataIndex: 'fiveWhy',
    align: "center",
    width: columnWidth,
  },
  {
    title: '5.原因',
    dataIndex: 'fiveReason',
    align: "center",
    width: columnWidth,
  },
];


//措施列配置
export const measureColumns: BasicColumn[] = [
  {
    title: '问题',
    dataIndex: 'problem',
    align: "center",
    width: 140,
  },
  {
    title: '原因',
    dataIndex: 'reason',
    align: "center",
    width: 240,
  },
  {
    title: '问题解决',
    dataIndex: 'solveProblem',
    align: "center",
    width: 240,
  },
  {
    title: '提出人',
    align: "center",
    dataIndex: 'putUserId',
    width: 100,
  },
  {
    title: '提出部门',
    align: "center",
    dataIndex: 'putDeptId',
    width: 120,
  },
  {
    title: '责任人',
    align: "center",
    dataIndex: 'dutyUserId',
    width: 100,
  },
  {
    title: '责任部门',
    align: "center",
    dataIndex: 'dutyDeptId',
    width: 120,
  },
  {
    title: '执行人',
    align: "center",
    dataIndex: 'executeUserId',
    width: 100,
  },
  {
    title: '执行部门',
    align: "center",
    dataIndex: 'executeDeptId',
    width: 120,
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'verifyUserId',
    width: 100,
  },
  {
    title: '确认部门',
    align: "center",
    dataIndex: 'verifyDeptId',
    width: 120,
  },
  {
    title: '计划完成日期',
    dataIndex: 'planFinishDate',
    align: "center",
    width: 130,
  },
  {
    title: '实际完成日期',
    dataIndex: 'practicalFinishDate',
    align: "center",
    width: 130,
  },
  {
    title: '完成情况',
    align: "center",
    dataIndex: 'finishState',
    customCell(record) {
      let colorObj = { bgColor: '' }
      if (record.finishState == 1) {
        colorObj.bgColor = '#00FF00'
      } else if (record.finishState == 2 || record.finishState == 4) {
        colorObj.bgColor = '#FE0000'
      } else if (record.finishState == 0) {
        colorObj.bgColor = 'yellow'
      } else if (record.finishState == 3) {
        colorObj.bgColor = '#666666'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
    width: 120,
  },
  {
    title: '完成情况确认',
    align: "center",
    dataIndex: 'finishVerifyState',
    width: 120,
    customRender: function({text}){
      return finishVerifyStateOptions.find(item => item.value == text)?.label;
    },
  },
  {
    title: '行动性质',
    dataIndex: 'actionMeasures',
    align: "center",
    width: 120,
  },
  {
    title: '行动级别',
    dataIndex: 'actionLevel',
    align: "center",
    width: 120,
  },
  {
    title: '问题分类',
    dataIndex: 'problemTypeId',
    align: "center",
    width: 120,
  },
  {
    title: '行动是否有效',
    dataIndex: 'isEnable',
    ellipsis: false,
    align: "center",
    width: 80,
  },
  {
    title: '上传至行动计划',
    dataIndex: 'actionType',
    ellipsis: false,
    align: "center",
    width: 80,
  },
];