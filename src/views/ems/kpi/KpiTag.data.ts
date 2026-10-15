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
    dataIndex: 'lineCode_dictText',
  },
  {
    title: 'KPI点别名',
    align: 'center',
    dataIndex: 'kpiTagName',
  },
  {
    title: 'KPI结构',
    align: 'center',
    dataIndex: 'structId_dictText',
  },
  {
    title: '点位大类',
    align: 'center',
    dataIndex: 'tagKind_dictText',
  },
  {
    title: '点位',
    align: 'center',
    dataIndex: 'tagId',
    slots: { customRender: 'tagId' },
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
  deptCode: { title: '部门Code', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线Code', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  kpiTagName: { title: 'KPI点别名', order: 3, view: 'text', type: 'string' },
  structId: { title: 'KPI结构ID', order: 4, view: 'number', type: 'number', dictTable: 'ems_kpi_struct', dictCode: 'id', dictText: 'name' },
  tagKind: {
    title: '点位大类',
    order: 5,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagKind'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagId: { title: '点位ID', order: 6, view: 'list', type: 'string', dictTable: 'ems_orsdata_tag', dictCode: 'tag_id', dictText: 'tag_name' },
  sort: { title: '排序号', order: 7, view: 'number', type: 'number' },
};
