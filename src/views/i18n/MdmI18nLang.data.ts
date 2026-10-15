import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '语言编码',
    align: 'center',
    sorter: true,
    dataIndex: 'langCode',
  },
  {
    title: '语言名称',
    align: 'center',
    dataIndex: 'langName',
  },
  {
    title: '是否可用',
    align: 'center',
    dataIndex: 'enabled_dictText',
  },
  {
    title: '版本号',
    align: 'center',
    sorter: true,
    dataIndex: 'version',
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
  langCode: { title: '语言编码', order: 0, view: 'text', type: 'string' },
  langName: { title: '语言名称', order: 1, view: 'text', type: 'string' },
  enabled: {
    title: '是否可用',
    order: 2,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_YN'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  version: { title: '版本号', order: 3, view: 'number', type: 'number' },
  sort: { title: '排序号', order: 4, view: 'number', type: 'number' },
  createBy: { title: '创建人', order: 5, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 6, view: 'datetime', type: 'string' },
  updateBy: { title: '更新人', order: 7, view: 'text', type: 'string' },
  updateTime: { title: '更新时间', order: 8, view: 'datetime', type: 'string' },
};
