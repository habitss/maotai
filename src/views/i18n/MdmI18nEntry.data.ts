import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '词条标识',
    align: 'center',
    sorter: true,
    dataIndex: 'entryKey',
  },
  {
    title: '所属模块',
    align: 'center',
    sorter: true,
    dataIndex: 'module_dictText',
  },
  {
    title: '词条描述',
    align: 'center',
    dataIndex: 'description',
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
    title: '更新人',
    align: 'center',
    dataIndex: 'updateBy',
  },
  {
    title: '更新时间',
    align: 'center',
    dataIndex: 'updateTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  entryKey: { title: '词条标识', order: 0, view: 'text', type: 'string' },
  module: {
    title: '所属模块',
    order: 1,
    view: 'list',
    type: 'string',
    dictTable: 'mes3_mdm_i18n_module',
    dictCode: 'module_code',
    dictText: 'module_name',
  },
  description: { title: '词条描述', order: 2, view: 'text', type: 'string' },
  sort: { title: '排序号', order: 3, view: 'number', type: 'number' },
  createBy: { title: '创建人', order: 4, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 5, view: 'datetime', type: 'string' },
  updateBy: { title: '更新人', order: 6, view: 'text', type: 'string' },
  updateTime: { title: '更新时间', order: 7, view: 'datetime', type: 'string' },
};
