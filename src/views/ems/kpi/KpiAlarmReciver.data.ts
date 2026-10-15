import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '分组名称',
    align: 'center',
    dataIndex: 'groupName',
  },
  {
    title: '用户名',
    align: 'center',
    dataIndex: 'userName',
  },
];

// 高级查询数据
export const superQuerySchema = {
  groupName: { title: '分组名称', order: 0, view: 'text', type: 'string' },
  userName: { title: '用户名', order: 2, view: 'text', type: 'string' },
};
