import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: 'center',
    sorter: true,
    dataIndex: 'factoryCode_dictText',
  },
  {
    title: '部门',
    align: 'center',
    sorter: true,
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '产线',
    align: 'center',
    sorter: true,
    dataIndex: 'lineCode_dictText',
  },
  {
    title: '报表分类',
    align: 'center',
    sorter: true,
    dataIndex: 'repType_dictText',
  },
  {
    title: '报表名称',
    align: 'center',
    sorter: true,
    dataIndex: 'repName',
  },
  {
    title: '报表图标',
    align: 'center',
    dataIndex: 'iconName',
    slots: { customRender: 'pngIcon' },
  },
  {
    title: '所需基础表名称',
    align: 'center',
    dataIndex: 'basedataName',
  },
  {
    title: '排序号',
    align: 'center',
    sorter: true,
    dataIndex: 'sort',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂Code', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  repType: {
    title: '报表分类',
    order: 3,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_ReportType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  repName: { title: '报表名称', order: 4, view: 'text', type: 'string' },
  iconName: { title: '报表图标', order: 5, view: 'text', type: 'string' },
  basedataName: { title: '所需基础表名称', order: 6, view: 'text', type: 'string' },
  sort: { title: '排序号', order: 7, view: 'number', type: 'number' },
};

export const repIconList = [
  { id: 'rep-default', label: '默认' },
  { id: 'rep-day', label: '日' },
  { id: 'rep-week', label: '周' },
  { id: 'rep-month', label: '月' },
  { id: 'rep-electric', label: '电' },
  { id: 'rep-water', label: '水' },
  { id: 'rep-heat', label: '热' },
  { id: 'rep-air', label: '空压' },
  { id: 'rep-co2', label: 'CO2' },
];
