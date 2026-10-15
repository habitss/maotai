import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '模块标识',
    align: 'center',
    sorter: true,
    dataIndex: 'moduleCode',
  },
  {
    title: '模块名称',
    align: 'center',
    sorter: true,
    dataIndex: 'moduleName',
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
];

// 高级查询数据
export const superQuerySchema = {
  moduleCode: { title: '模块标识', order: 0, view: 'text', type: 'string' },
  moduleName: { title: '模块名称', order: 1, view: 'text', type: 'string' },
  createBy: { title: '创建人', order: 2, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 3, view: 'datetime', type: 'string' },
};
