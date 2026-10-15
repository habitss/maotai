import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '应用名称',
    align: 'center',
    dataIndex: 'appName',
  },
  {
    title: '应用标识',
    align: 'center',
    dataIndex: 'appId',
    width: 200,
  },
  {
    title: '应用过期时间',
    align: 'center',
    dataIndex: 'expireTime',
  },
  {
    title: '旧密钥',
    align: 'center',
    dataIndex: 'appSecretOld',
    customRender: ({ text }) => {
      return text ? '*********' : '';
    },
  },
  {
    title: '密钥轮换时间',
    align: 'center',
    dataIndex: 'secretRotateTime',
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status_dictText',
    width: 150,
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  appId: { title: '应用标识', order: 0, view: 'text', type: 'string' },
  appName: { title: '应用名称', order: 1, view: 'text', type: 'string' },
  appSecret: { title: '应用密钥', order: 2, view: 'password', type: 'string' },
  status: {
    title: '状态',
    order: 3,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict WHERE group_code = 'EMS_StateYN'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  expireTime: { title: '应用过期时间', order: 4, view: 'datetime', type: 'string' },
  ipWhitelist: { title: 'IP白名单', order: 5, view: 'textarea', type: 'string' },
  createTime: { title: '创建时间', order: 6, view: 'datetime', type: 'string' },
  appSecretOld: { title: '旧密钥', order: 7, view: 'password', type: 'string' },
  secretRotateTime: { title: '密钥轮换时间', order: 8, view: 'datetime', type: 'string' },
};
