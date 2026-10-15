import { BasicColumn } from '/@/components/Table';
import { h } from 'vue';

//列表数据
export const getColumns = (handleShowDsList: (record: any) => void): BasicColumn[] => [
  {
    title: 'GF实例名称',
    align: 'center',
    dataIndex: 'instanceName',
    slots: {
      customRender: 'nameSolt',
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'state_dictText',
    width: 120,
  },
  {
    title: '数据源数量',
    align: 'center',
    dataIndex: 'dsCnt',
    width: 90,
    customRender: ({ record }) => {
      const hasData = record.dsCnt && record.dsCnt > 0;
      const text = hasData ? `${record.dsCnt}个` : '未配置或未备份';

      return h(
        'span',
        {
          style: {
            color: hasData ? '#d92622' : '#999',
            cursor: hasData ? 'pointer' : 'default',
            transition: 'color 0.2s',
          },
          onClick: () => {
            if (hasData) handleShowDsList(record);
          },
        },
        text
      );
    },
  },
  {
    title: '主数据库类型',
    align: 'center',
    dataIndex: 'pointRdsType_dictText',
    width: 120,
  },
  {
    title: '主数据库URL',
    align: 'center',
    dataIndex: 'pointDbUrl',
  },
  {
    title: '基础点位表',
    align: 'center',
    dataIndex: 'basePointTb',
  },
  {
    title: '中间点位表',
    align: 'center',
    dataIndex: 'orsPointTb',
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];

//侧边展示列表数据
export const sideColumns: BasicColumn[] = [
  {
    title: 'GF实例名称',
    align: 'center',
    dataIndex: 'instanceName',
    slots: { customRender: 'sideContent' },
  },
];

// 高级查询数据
export const superQuerySchema = {
  instanceName: { title: 'GF实例名称', order: 0, view: 'text', type: 'string' },
  instanceUrl: { title: 'GF实例URL', order: 1, view: 'text', type: 'string' },
  apiToken: { title: 'GF实例Token', order: 2, view: 'text', type: 'string' },
  state: {
    title: '状态(0-未备份 1-备份中 2-成功 3-失败)',
    order: 3,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_InstanceBackupState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  errorMsg: { title: '错误信息', order: 4, view: 'text', type: 'string' },
  pointRdsType: {
    title: '主数据库类型',
    order: 5,
    view: 'list',
    type: 'string',
    dictTable: "mes3_mdm_dict where group_code = 'GF_RDSType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  pointDbUrl: { title: '主数据存放库URL', order: 6, view: 'text', type: 'string' },
  pointDbUname: { title: '主数据存放库用户', order: 7, view: 'text', type: 'string' },
  pointDbPassword: { title: '主数据存放库密码', order: 8, view: 'text', type: 'string' },
  basePointTb: { title: '基础点位表', order: 9, view: 'text', type: 'string' },
  baseTbIdCol: { title: '基础点位ID', order: 10, view: 'text', type: 'string' },
  baseTbNameCol: { title: '基础点位NAME', order: 11, view: 'text', type: 'string' },
  orsPointTb: { title: '中间点位表', order: 12, view: 'text', type: 'string' },
  orsTbIdCol: { title: '中间点位ID', order: 13, view: 'text', type: 'string' },
  orsTbNameCol: { title: '中间点位NAME', order: 14, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 15, view: 'datetime', type: 'string' },
};
