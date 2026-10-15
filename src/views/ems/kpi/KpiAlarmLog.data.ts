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
    title: '点位大类',
    align: 'center',
    sorter: true,
    dataIndex: 'tagKind_dictText',
  },
  {
    title: '点位',
    align: 'center',
    sorter: true,
    dataIndex: 'tagId',
    slots: { customRender: 'tagId' },
  },
  {
    title: '报警时间',
    align: 'center',
    dataIndex: 'alarmTime',
  },
  {
    title: '报警内容',
    align: 'center',
    dataIndex: 'alarmContent',
  },
  {
    title: '报警接收人',
    align: 'center',
    dataIndex: 'alarmReciver',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagKind: {
    title: '点位大类',
    order: 4,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagKind'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagId: { title: '点位', order: 5, view: 'list', type: 'string', dictTable: 'ems_orsdata_tag', dictCode: 'tag_id', dictText: 'tag_name' },
  alarmTime: { title: '报警时间', order: 6, view: 'datetime', type: 'string' },
  alarmContent: { title: '报警内容', order: 7, view: 'text', type: 'string' },
  alarmReciver: { title: '报警接收人', order: 8, view: 'text', type: 'string' },
};
