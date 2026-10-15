import { BasicColumn } from '/@/components/Table';
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
    dataIndex: 'oplType',
    width: '140px',
    slots: { customRender: 'oplTypeSlots' }
  },
  {
    title: '活动',
    align: "center",
    dataIndex: 'oplActivity',
    width: '110px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: '岗位',
    align: "center",
    dataIndex: 'oplPost',
    width: '100px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: 'OPL编号',
    align: "center",
    dataIndex: 'oplNumber',
    width: '210px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: 'OPL名称',
    align: "center",
    dataIndex: 'oplDesc',
    width: '300px',
    slots: { customRender: 'inputSlots' }
  },
  {
    title: '关键OPL',
    align: "center",
    dataIndex: 'oplStateName',
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
    slots: { customRender: 'putIntoNamesSlots' }
  },
  {
    title: '历史版本查询',
    align: "center",
    dataIndex: 'oldList',
    width: '140px',
    slots: { customRender: 'oldListSlots' }
  },
  {
    title: '回顾计划',
    align: "center", dataIndex: 'reviewPlanUrl',
    width: '140px',
    slots: { customRender: 'reviewPlanUrlSlots' }
  }
];




export const responsibilityColumns: BasicColumn[] = [
  { title: 'OPL编写项目', align: 'center', dataIndex: 'oplTitle' },
  { title: 'R（前10章编写人）', align: 'center', dataIndex: 'r1', slots: { customRender: 'userSlots' } },
  { title: 'R（前11章编写人）', align: 'center', dataIndex: 'r2', slots: { customRender: 'userSlots' } },
  { title: 'A（责任人）', align: 'center', dataIndex: 'a', slots: { customRender: 'userSlots' } },
  { title: 'C（被询问人）', align: 'center', dataIndex: 'c', slots: { customRender: 'userSlots' } },
  { title: 'I（被通知人）', align: 'center', dataIndex: 'i', slots: { customRender: 'inputSlots' } }
]