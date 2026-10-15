import {BasicColumn} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '2025年度诊断计划',
    align: 'center',
    children:[
      {
        title: '部门',
        align: "center",
        fixed: 'left',
        dataIndex: 'deptCodeVal',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '产线',
        align: "center",
        fixed: 'left',
        dataIndex: 'lineCodeVal',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '活动',
        align: "center",
        fixed: 'left',
        dataIndex: 'sopActivity',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '类型',
        align: "center",
        fixed: 'left',
        dataIndex: 'sopTypeVal',
        width: '140px',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: 'SOP名称',
        align: "center",
        fixed: 'left',
        width: '200px',
        dataIndex: 'sopName',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '诊断负责人',
        align: "center",
        width: '140px',
        dataIndex: 'checkUserId',
        slots: { customRender: 'checkUserIdSlots' },
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '诊断责任人',
        align: "center",
        width: '140px',
        dataIndex: 'responsibleUserName',
        slots: { customRender: 'userNameSlots' },
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },

      {
        title: '诊断咨询人',
        align: "center",
        width: '140px',
        dataIndex: 'seekUserName',
        slots: { customRender: 'userNameSlots' },
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '诊断通知人',
        align: "center",
        width: '140px',
        dataIndex: 'noticeUserName',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '上一次诊断年份',
        align: "center",
        width: '140px',
        dataIndex: 'lastCheckYear',
        slots: { customRender: 'lastCheckYearSlots' },
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '诊断频率',
        align: "center",
        width: '140px',
        dataIndex: 'checkInterval',
        slots: { customRender: 'checkIntervalSlots' },
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '已完成',
        align: "center",
        width: '100px',
        dataIndex: 'completed',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '计划',
        align: "center",
        width: '100px',
        dataIndex: 'underWay',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },
      {
        title: '延迟',
        align: "center",
        width: '100px',
        dataIndex: 'delay',
        customCell: (record) => {
          return { rowSpan: record.rowSpan };
        }
      },

      {
        title: '被诊断人',
        align: "center",
        dataIndex: 'byCheckUserId',
        slots: { customRender: 'byCheckUserIdSlots' },
        width: 140
      },
      {
        title: '员工状态',
        align: "center",
        dataIndex: 'employeeState',
        slots: { customRender: 'employeeStateSlots' },
        width: 140
      },
      {
        title: '办公室诊断',
        align: "center",
        width: '140px',
        dataIndex: 'officeFileState',
        slots: { customRender: 'officeFileStateSlots' }
      },
      {
        title: '现场诊断',
        align: "center",
        width: '140px',
        dataIndex: 'sceneCheck',
        slots: { customRender: 'sceneCheckSlots' }
      }
    ]
  }
];
