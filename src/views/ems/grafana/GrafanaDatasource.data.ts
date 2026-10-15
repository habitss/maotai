import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: 'GF实例',
    align: 'center',
    dataIndex: 'instanceId_dictText',
  },
  {
    title: 'GF数据源类型',
    align: 'center',
    dataIndex: 'type_dictText',
  },
  {
    title: 'GF数据源标识',
    align: 'center',
    dataIndex: 'uid',
  },
  {
    title: 'GF数据源名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: 'GF数据源URL',
    align: 'center',
    dataIndex: 'url',
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  instanceId: { title: 'GF实例ID', order: 0, view: 'list', type: 'string', dictTable: 'grafana_instance', dictCode: 'id', dictText: 'instance_name' },
  type: {
    title: 'GF数据源类型',
    order: 1,
    view: 'list',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'GF_RDSType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  uid: { title: 'GF数据源标识', order: 2, view: 'text', type: 'string' },
  name: { title: 'GF数据源名称', order: 3, view: 'text', type: 'string' },
  url: { title: 'GF数据源URL', order: 4, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 5, view: 'datetime', type: 'string' },
};
