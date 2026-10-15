import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
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
    title: '点位ID',
    align: 'center',
    sorter: true,
    dataIndex: 'tagId',
  },
  {
    title: '点位类型',
    align: 'center',
    sorter: true,
    dataIndex: 'tagType_dictText',
  },
  {
    title: '点位名称',
    align: 'center',
    sorter: true,
    dataIndex: 'tagName',
  },
  {
    title: '单位',
    align: 'center',
    dataIndex: 'unit_dictText',
  },
  {
    title: '点位描述',
    align: 'center',
    dataIndex: 'tagDesc',
  },
  {
    title: '排序号',
    align: 'center',
    sorter: true,
    dataIndex: 'sort',
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy',
  },
  {
    title: '创建时间',
    align: 'center',
    sorter: true,
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '点位ID', order: 3, view: 'text', type: 'string' },
  tagType: {
    title: '点位类型',
    order: 4,
    view: 'list',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagName: { title: '点位名称', order: 5, view: 'text', type: 'string' },
  unit: {
    title: '单位',
    order: 6,
    view: 'sel_search',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_Unit'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagDesc: { title: '点位描述', order: 7, view: 'text', type: 'string' },
  remark: { title: '备注', order: 8, view: 'text', type: 'string' },
  state: {
    title: '状态',
    order: 9,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  sort: { title: '排序号', order: 10, view: 'number', type: 'number' },
  createBy: { title: '创建人', order: 11, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 12, view: 'datetime', type: 'string' },
};
