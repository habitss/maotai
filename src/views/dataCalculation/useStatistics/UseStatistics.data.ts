import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '分类',
    align: "center",
    children:[
      {
        title: "部门",
        align: "center",
        dataIndex: 'dept',
        width:'140px',
        customCell: () => {
          return {
            style: { textAlign: 'left' } // 内容居左
          }
        }
      }
    ]
  },
  {
    title: '管理工具',
    align: "center",
    children:[
      {
        title: "行动计划",
        align: "center",
        dataIndex: 'move',
        width:'80px',
      },
      {
        title: "分层检查",
        align: "center",
        dataIndex: 'layering',
        width:'80px',
      },
      {
        title: "有效会议",
        align: "center",
        dataIndex: 'meeting',
        width:'80px',
      },
      {
        title: "异常报告",
        align: "center",
        dataIndex: 'abnormal',
        width:'80px',
      },
      {
        title: "期间核查",
        align: "center",
        dataIndex: 'period',
        width:'80px',
      },
      {
        title: "COP报表",
        align: "center",
        dataIndex: 'cop',
        width:'80px',
      },
      {
        title: "通用岗位记录",
        align: "center",
        dataIndex: 'post',
        width:'110px',
      },
      {
        title: "岗位检查清单",
        align: "center",
        dataIndex: 'postInspect',
        width:'110px',
      },
      {
        title: "管理者检查清单",
        align: "center",
        dataIndex: 'managerInspect',
        width:'120px',
      }
    ]
  },
  {
    title: 'KPI',
    align: "center",
    children:[
      {
        title: "合计",
        align: "center",
        dataIndex: 'kpiAmount',
        width:'50px',
      }
    ]
  }
];

