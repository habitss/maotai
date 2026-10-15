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
    title: '线级',
    align: 'center',
    dataIndex: 'lineLevel',
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
    dataIndex: 'tagType_dictText',
  },
  {
    title: '点位名称',
    align: 'center',
    sorter: true,
    dataIndex: 'tagName',
    width: 300,
  },
  {
    title: '单位',
    align: 'center',
    dataIndex: 'unit',
  },
  {
    title: '点位描述',
    align: 'center',
    dataIndex: 'tagDesc',
  },
  {
    title: '备注',
    align: 'center',
    dataIndex: 'remark',
  },
  {
    title: '状态',
    align: 'center',
    sorter: true,
    dataIndex: 'state_dictText',
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
  {
    title: '更新人',
    align: 'center',
    dataIndex: 'updateBy',
  },
  {
    title: '更新时间',
    align: 'center',
    sorter: true,
    dataIndex: 'updateTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂Code', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineLevel: { title: '线级', order: 3, view: 'text', type: 'string' },
  tagId: { title: '点位ID', order: 4, view: 'number', type: 'number' },
  tagType: {
    title: '点位类型',
    order: 5,
    view: 'list',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagName: { title: '点位名称', order: 6, view: 'text', type: 'string' },
  unit: { title: '单位', order: 7, view: 'text', type: 'string' },
  tagDesc: { title: '点位描述', order: 8, view: 'text', type: 'string' },
  remark: { title: '备注', order: 9, view: 'text', type: 'string' },
  state: {
    title: '状态',
    order: 10,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  sort: { title: '排序号', order: 11, view: 'number', type: 'number' },
  createBy: { title: '创建人', order: 12, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 13, view: 'datetime', type: 'string' },
  updateBy: { title: '更新人', order: 14, view: 'text', type: 'string' },
  updateTime: { title: '更新时间', order: 15, view: 'datetime', type: 'string' },
};
