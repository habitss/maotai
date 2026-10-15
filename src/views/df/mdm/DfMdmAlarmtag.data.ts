import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '业务唯一标识 UNIQ_ID',
    align: 'center',
    dataIndex: 'uniqId',
    width: 200,
  },
  {
    title: 'TAG功能位置编码 TAG_FUNLOC_CODE',
    align: 'center',
    dataIndex: 'tagFunlocCode',
    width: 160,
  },
  {
    title: 'TAG功能位置层级描述 TAG_FUNLOC_CODE2',
    align: 'center',
    dataIndex: 'tagFunlocCode2',
    width: 200,
  },
  {
    title: '功能位置描述 V_FUNLOC_DESC',
    align: 'center',
    dataIndex: 'vfunlocDesc',
    width: 200,
  },
  {
    title: '标准点位功能位置 V_FUNLOC_DESC2',
    align: 'center',
    dataIndex: 'vfunlocDesc2',
    width: 200,
  },
  {
    title: 'IOT属性编码 IOT_ATTR_CODE',
    align: 'center',
    dataIndex: 'iotAttrCode',
    width: 180,
  },
  {
    title: 'IOT属性显示名称 IOT_ATTR_DISP_NAME',
    align: 'center',
    dataIndex: 'iotAttrDispName',
    width: 180,
  },
  {
    title: '报警位 TAG_BIT',
    align: 'center',
    dataIndex: 'tagBit',
    width: 90,
  },
  {
    title: '报警位描述 TAG_BIT_DESC',
    align: 'center',
    dataIndex: 'tagBitDesc',
    width: 160,
  },
  {
    title: '报警位描述2 TAG_BIT_DESC2',
    align: 'center',
    dataIndex: 'tagBitDesc2',
    width: 180,
  },
  {
    title: '是否启用',
    align: 'center',
    dataIndex: 'enableFlag',
    width: 90,
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    align: 'center',
    dataIndex: 'updateTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  uniqId: { title: '业务唯一标识 UNIQ_ID', order: 2, view: 'text', type: 'string' },
  tagFunlocCode: { title: 'TAG功能位置编码 TAG_FUNLOC_CODE', order: 3, view: 'text', type: 'string' },
  tagFunlocCode2: { title: 'TAG功能位置层级描述 TAG_FUNLOC_CODE2', order: 4, view: 'text', type: 'string' },
  vfunlocDesc: { title: '功能位置描述 V_FUNLOC_DESC', order: 5, view: 'text', type: 'string' },
  vfunlocDesc2: { title: '功能位置描述2 V_FUNLOC_DESC2', order: 6, view: 'text', type: 'string' },
  iotAttrCode: { title: 'IOT属性编码 IOT_ATTR_CODE', order: 7, view: 'text', type: 'string' },
  iotAttrDispName: { title: 'IOT属性显示名称 IOT_ATTR_DISP_NAME', order: 8, view: 'text', type: 'string' },
  tagBit: { title: '报警位 TAG_BIT', order: 9, view: 'text', type: 'string' },
  tagBitDesc: { title: '报警位描述 TAG_BIT_DESC', order: 10, view: 'text', type: 'string' },
  tagBitDesc2: { title: '报警位描述2 TAG_BIT_DESC2', order: 11, view: 'text', type: 'string' },
  enableFlag: { title: '是否启用 Y/N', order: 12, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 14, view: 'datetime', type: 'string' },
  updateTime: { title: '更新时间', order: 16, view: 'datetime', type: 'string' },
};
