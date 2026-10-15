import { BasicColumn } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { h } from 'vue';

//基础列表数据
export const columns: BasicColumn[] = [
  {
    title: '报表标题',
    align: 'center',
    dataIndex: 'dashboardId_dictText',
  },
  {
    title: '面板ID',
    align: 'center',
    dataIndex: 'panelId',
    width: 100,
  },
  {
    title: '面板标题',
    align: 'center',
    dataIndex: 'panelTitle',
  },
  {
    title: '数据源类型',
    align: 'center',
    sorter: true,
    dataIndex: 'dbType_dictText',
    width: 120,
  },
  {
    title: '原始查询语句',
    align: 'center',
    dataIndex: 'queryText',
  },
  {
    title: '查询语句点位',
    align: 'center',
    dataIndex: 'pointIds',
  },
  {
    title: '自动匹配点位建议',
    align: 'center',
    dataIndex: 'autoMatch',
  },
  {
    title: '匹配状态',
    align: 'center',
    dataIndex: 'matchState_dictText',
    width: 120,
  },
];

//解析与匹配列表数据
export const mappingColumns: BasicColumn[] = [
  {
    title: '面板ID',
    align: 'center',
    dataIndex: 'panelId',
    width: 60,
  },
  {
    title: '面板标题',
    align: 'center',
    dataIndex: 'panelTitle',
    width: 180,
    customRender: ({ text, record }) => {
      return h(
        'a',
        {
          href: `${record.instanceUrl}/d/${record.uid}?viewPanel=panel-${record.panelId}`,
          target: '_blank',
          style: { color: '#d92622', textDecoration: 'none' },
          onClick: (e) => e.stopPropagation(), // 防止触发表格的行点击事件
        },
        render.renderTip(text)
      );
    },
  },
  {
    title: '数据源',
    align: 'center',
    dataIndex: 'dbType_dictText',
    width: 100,
  },
  {
    title: '原始查询语句',
    align: 'center',
    dataIndex: 'queryText',
    slots: { customRender: 'querySqlShow' },
    width: 500,
  },
  {
    title: '查询语句点位',
    align: 'left',
    dataIndex: 'pointIds',
    slots: { customRender: 'pointListShow' },
    width: 250,
  },
  {
    title: '自动匹配点位建议',
    align: 'center',
    dataIndex: 'autoMatch',
    slots: { customRender: 'matchListShow' },
  },
];

// 高级查询数据
export const superQuerySchema = {
  panelId: { title: '面板ID', order: 0, view: 'text', type: 'string' },
  panelTitle: { title: '面板标题', order: 1, view: 'text', type: 'string' },
  dbType: {
    title: '数据库类型(mysql influxdb sqlserver)',
    order: 2,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_DBType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  queryText: { title: '原始查询语句', order: 3, view: 'textarea', type: 'string' },
  pointIds: { title: '查询语句点位表', order: 4, view: 'textarea', type: 'string' },
  autoMatch: { title: '自动匹配点位建议', order: 5, view: 'textarea', type: 'string' },
  matchState: {
    title: '匹配状态(auto manual verified fail)',
    order: 6,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_MatchState'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
};
