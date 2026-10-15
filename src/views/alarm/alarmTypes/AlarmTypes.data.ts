import {BasicColumn} from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '类型名称',
    align: 'left',
    dataIndex: 'typeName'
  },
  // {
  //   title: '父类型',
  //   align: 'center',
  //   dataIndex: 'parentId_dictText'
  // },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy'
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime'
  },
  {
    title: '更新人',
    align: 'center',
    dataIndex: 'updateBy'
  },
  {
    title: '更新时间',
    align: 'center',
    dataIndex: 'updateTime'
  },
];

// 高级查询数据
export const superQuerySchema = {
  typeName: {title: '类型名称',order: 0,view: 'text', type: 'string',},
  parentId: {title: '父类型ID',order: 1,view: 'number', type: 'number',dictTable: "alarm_types", dictCode: 'id', dictText: 'type_name',},
  createBy: {title: '创建人',order: 2,view: 'text', type: 'string',},
  createTime: {title: '创建时间',order: 3,view: 'datetime', type: 'string',},
  updateBy: {title: '更新人',order: 4,view: 'text', type: 'string',},
  updateTime: {title: '更新时间',order: 5,view: 'datetime', type: 'string',},
};
