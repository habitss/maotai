import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '词条',
    align: 'center',
    sorter: true,
    dataIndex: 'entryId_dictText',
  },
  {
    title: '语言编码',
    align: 'center',
    sorter: true,
    dataIndex: 'langCode_dictText',
  },
  {
    title: '内容',
    align: 'center',
    dataIndex: 'content',
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy',
  },
  {
    title: '创建时间',
    align: 'center',
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
    dataIndex: 'updateTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  entryId: { title: '词条ID', order: 0, view: 'number', type: 'number', dictTable: 'mes3_mdm_i18n_entry', dictCode: 'id', dictText: 'description' },
  langCode: {
    title: '语言编码',
    order: 1,
    view: 'list',
    type: 'string',
    dictTable: 'mes3_mdm_i18n_lang',
    dictCode: 'lang_code',
    dictText: 'lang_name',
  },
  content: { title: '内容', order: 2, view: 'text', type: 'string' },
  createBy: { title: '创建人', order: 3, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 4, view: 'datetime', type: 'string' },
  updateBy: { title: '更新人', order: 5, view: 'text', type: 'string' },
  updateTime: { title: '更新时间', order: 6, view: 'datetime', type: 'string' },
};
