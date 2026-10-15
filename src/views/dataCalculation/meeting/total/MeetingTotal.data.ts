import { BasicColumn, FormSchema } from '/@/components/Table';

//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'dept',
    fixed: 'left',
    width: 120,
  },
  {
    title: '日会',
    children: [
      {
        title: '会议应召开次数',
        dataIndex: 'all_count_day',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议计划召开数',
        dataIndex: 'plan_count_day',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议取消数',
        align: "center",
        dataIndex: 'canceled_count_day',
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议实际召开数',
        dataIndex: 'open_count_day',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议召开率',
        dataIndex: 'range_count_day',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议评估次数',
        align: "center",
        dataIndex: 'record_count_day',
        ellipsis:false,
        width: 100,
      },
    ],
  },
  {
    title: '周会',
    children: [
      {
        title: '会议应召开次数',
        dataIndex: 'all_count_week',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议计划召开数',
        dataIndex: 'plan_count_week',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议取消数',
        align: "center",
        dataIndex: 'canceled_count_week',
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议实际召开数',
        dataIndex: 'open_count_week',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议召开率',
        dataIndex: 'range_count_week',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议评估次数',
        align: "center",
        dataIndex: 'record_count_week',
        ellipsis:false,
        width: 100,
      },
    ],
  },
  {
    title: '月会',
    children: [
      {
        title: '会议应召开次数',
        dataIndex: 'all_count_month',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议计划召开数',
        dataIndex: 'plan_count_month',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议取消数',
        align: "center",
        dataIndex: 'canceled_count_month',
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议实际召开数',
        dataIndex: 'open_count_month',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议召开率',
        dataIndex: 'range_count_month',
        align: "center",
        ellipsis:false,
        width: 100,
      },
      {
        title: '会议评估次数',
        align: "center",
        dataIndex: 'record_count_month',
        ellipsis:false,
        width: 100,
      },
    ],
  },
];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: '日期范围',
    field: 'times',
    component: 'RangeDate',
    componentProps: {
      //日期格式化
      format:'YYYY/MM/DD',
      //范围文本描述用集合
      placeholder:['请选择开始日期','请选择结束日期']
    },
    colProps: { span: 9 },
  },
];
