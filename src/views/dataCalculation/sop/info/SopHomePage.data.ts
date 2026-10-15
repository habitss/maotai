import {BasicColumn} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: '120px',
    slots: { customRender: 'sortSlots' }
  },
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode',
    width: '140px',
    slots: { customRender: 'deptSlots' }
  },

  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode',
    width: '170px',
    slots: { customRender: 'lineSlots' }
  },
  {
    title: '类型',
    align: "center",
    dataIndex: 'sopType',
    width: '140px' ,
    slots: { customRender: 'sopTypeSlots' }
  },
  {
    title: '活动',
    align: "center",
    dataIndex: 'sopActivity',
    width: '110px',
    slots: { customRender: 'inputSlots'}
  },
  {
    title: '岗位',
    align: "center",
    dataIndex: 'sopPost',
    width: '100px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: 'SOP编号',
    align: "center",
    dataIndex: 'sopNumber',
    width: '210px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: 'SOP名称',
    align: "center",
    dataIndex: 'sopDesc',
    width: '300px',
    slots: { customRender: 'inputSlots'}
  },
  { 
    title: '是否需要诊断', 
    align: "center", 
    dataIndex: 'diagnosis',
    width: '80px' ,
    slots: { customRender: 'diagnosisSlots'}
  },
  {
    title: '关键SOP',
    align: "center",
    dataIndex: 'sopStateName',
    width: '140px' 
  },
  {
    title: 'OWD频次',
    align: "center",
    dataIndex: 'owdFrequencyName',
    width: '140px' 
  },
  { 
    title: '回顾频次', 
    align: "center", 
    dataIndex: 'reviewFrequencyName',
    width: '140px' 
  },
  { 
    title: '最新版版本号', 
    align: "center", 
    dataIndex: 'versionNumber',
    width: '140px' 
  },
  { 
    title: '实施时间', 
    align: "center", 
    dataIndex: 'putIntoTime',
    width: '140px'
  },
  { 
    title: '编写人', 
    align: "center", 
    dataIndex: 'putIntoNames_dictText',
    width: '140px',
    slots: { customRender: 'putIntoNamesSlots'}
  },
  { 
    title: '历史版本查询', 
    align: "center", 
    dataIndex: 'oldList',
    width: '140px',
    slots: { customRender: 'oldListSlots'}
  },
  { 
    title: 'OWD计划', 
    align: "center", dataIndex: 'planUrl',
    width: '140px',
    slots: { customRender: 'planUrlSlots'}
  },
  { 
    title: '回顾计划', 
    align: "center", dataIndex: 'reviewPlanUrl',
    width: '140px',
    slots: { customRender: 'reviewPlanUrlSlots'}
  }
];




export const responsibilityColumns: BasicColumn[] = [
  { title: 'SOP编写项目',  align: 'center', dataIndex: 'sopTitle' },
  { title: 'R（前10章编写人）', align: 'center',  dataIndex: 'r1',slots: { customRender: 'userSlots'} },
  { title: 'R（前11章编写人）', align: 'center', dataIndex: 'r2',slots: { customRender: 'userSlots'} },
  { title: 'A（责任人）', align: 'center', dataIndex: 'a' ,slots: { customRender: 'userSlots'}},
  { title: 'C（被询问人）', align: 'center', dataIndex: 'c',slots: { customRender: 'userSlots'} },
  { title: 'I（被通知人）', align: 'center', dataIndex: 'i' ,slots: { customRender: 'inputSlots'}}
]