import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    width: 150,
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '点位大类',
    align: 'center',
    width: 130,
    sorter: true,
    dataIndex: 'tagKind_dictText',
  },
  {
    title: '关联点位',
    align: 'center',
    sorter: true,
    dataIndex: 'tagName',
    customRender: ({ text, record }) => {
      if (!text) {
        return record.tagId;
      } else {
        return `[${record.tagId}]${text}`;
      }
    },
  },
  {
    title: '时间周期',
    align: 'center',
    width: 100,
    dataIndex: 'grainType_dictText',
  },
  {
    title: '预警等级',
    align: 'center',
    width: 100,
    dataIndex: 'warnLevel_dictText',
  },
  {
    title: '时间点',
    align: 'center',
    sorter: true,
    width: 180,
    dataIndex: 'timePoint',
    customRender: ({ text }) => {
      if (!text) return '';
      return text.split(':')[0] + ':' + text.split(':')[1];
    },
  },
  {
    title: '预警详情',
    align: 'center',
    dataIndex: 'warnMsg',
  },
  {
    title: '是否推送消息',
    align: 'center',
    width: 110,
    dataIndex: 'pushFlag_dictText',
  },
  {
    title: '创建时间',
    align: 'center',
    width: 180,
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '关联点位', order: 3, view: 'list', type: 'string', dictTable: 'ems_thirdparty_tag', dictCode: 'tag_id', dictText: 'tag_name' },
  grainType: {
    title: '粒度类型',
    order: 4,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_GrainType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagKind: {
    title: '点位大类',
    order: 5,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagKind'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  timePoint: { title: '时间点', order: 6, view: 'datetime', type: 'string' },
  warnMsg: { title: '预警详情', order: 7, view: 'textarea', type: 'string' },
  warnLevel: {
    title: '预警等级',
    order: 8,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_WarnLevel'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
};
