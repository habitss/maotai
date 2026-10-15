import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '点位大类',
    align: 'center',
    sorter: true,
    dataIndex: 'tagKind_dictText',
  },
  {
    title: '点位',
    align: 'center',
    dataIndex: 'tagId_dictText',
  },
  {
    title: '预警参数JSON',
    align: 'center',
    dataIndex: 'alarmJson',
  },
];

// 高级查询数据
export const superQuerySchema = {
  tagKind: {
    title: '点位大类',
    order: 0,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagKind'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagId: { title: '点位ID', order: 1, view: 'list', type: 'string', dictTable: 'ems_orsdata_tag', dictCode: 'tag_id', dictText: 'tag_name' },
  alarmJson: { title: '预警参数JSON', order: 2, view: 'text', type: 'string' },
};
