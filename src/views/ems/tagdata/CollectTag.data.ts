import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '产线',
    align: 'center',
    dataIndex: 'lineCode_dictText',
  },
  {
    title: '点位ID',
    align: 'center',
    dataIndex: 'tagId',
  },
  {
    title: '点位类型',
    align: 'center',
    dataIndex: 'tagType',
  },
  {
    title: '点位名称',
    align: 'center',
    dataIndex: 'tagName',
  },
  {
    title: '单位',
    align: 'center',
    dataIndex: 'unit',
  },
  {
    title: '点位编码',
    align: 'center',
    dataIndex: 'pointCode',
  },
  {
    title: '采集周期',
    align: 'center',
    dataIndex: 'collectPeriod',
  },
  {
    title: '值类型',
    align: 'center',
    dataIndex: 'valType',
  },
  {
    title: '排序号',
    align: 'center',
    dataIndex: 'sort',
  },
];

// 高级查询数据
export const superQuerySchema = {
  deptCode: { title: '部门Code', order: 0, view: 'list', type: 'string', dictCode: '' },
  lineCode: { title: '产线Code', order: 1, view: 'list', type: 'string', dictCode: '' },
  deviceId: { title: '设备id', order: 2, view: 'number', type: 'number', dictTable: 'mes3_mdm_device', dictCode: 'id', dictText: 'device_name' },
  pointCode: { title: '点位编码', order: 3, view: 'text', type: 'string' },
  pointName: { title: '点位中文描述', order: 4, view: 'text', type: 'string' },
  sort: { title: 'sort', order: 5, view: 'number', type: 'number' },
  energyType: { title: '能源类型', order: 6, view: 'text', type: 'string' },
  collectPeriod: { title: '采集周期', order: 7, view: 'number', type: 'number' },
  unit: { title: '单位', order: 8, view: 'text', type: 'string' },
  tagId: { title: '点位ID', order: 9, view: 'text', type: 'string' },
  valType: { title: '值类型(0-瞬时 1-累积)', order: 10, view: 'number', type: 'number' },
};
