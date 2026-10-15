import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    dataIndex: 'deptCode',
  },
  {
    title: '产线',
    align: 'center',
    dataIndex: 'lineCode',
  },
  {
    title: '级别',
    align: 'center',
    dataIndex: 'level',
  },
  {
    title: '父节点ID',
    align: 'center',
    dataIndex: 'parentId',
  },
  {
    title: '名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '能源类型',
    align: 'center',
    dataIndex: 'energyType',
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
  factoryCode: { title: '工厂', order: 0, view: 'text', type: 'string' },
  deptCode: { title: '部门', order: 1, view: 'text', type: 'string' },
  lineCode: { title: '产线', order: 2, view: 'text', type: 'string' },
  level: { title: '级别', order: 3, view: 'number', type: 'number' },
  parentId: { title: '父节点ID', order: 4, view: 'number', type: 'number' },
  name: { title: '名称', order: 5, view: 'text', type: 'string' },
  energyType: { title: '能源类型', order: 6, view: 'text', type: 'string' },
  sort: { title: '排序号', order: 7, view: 'number', type: 'number' },
};
