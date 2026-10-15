import { BasicColumn } from '/@/components/Table';
import { h } from 'vue';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    sorter: true,
    width: 180,
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '关联点位',
    align: 'left',
    dataIndex: 'tagId_dictText',
    customRender: ({ text, record }) => {
      if (!text) return '';
      return `[${record.tagId}]${text}`;
    },
  },
  {
    title: '对应采集时间段',
    align: 'center',
    width: 150,
    dataIndex: 'collectTime',
    customRender: ({ text }) => {
      if (!text) return '';
      return text.split(':')[0] + ':' + text.split(':')[1];
    },
  },
  {
    title: '参考底数',
    align: 'center',
    width: 140,
    dataIndex: 'lastBaseVal',
  },
  {
    title: '是否完成',
    align: 'center',
    width: 100,
    dataIndex: 'finishFlag_dictText',
    customRender: ({ text, record }) => {
      if (record.finishFlag === 0) {
        return h('span', { style: { color: 'red', fontWeight: 'bold' } }, text);
      }
      return text;
    },
  },
  {
    title: '是否异常',
    align: 'center',
    width: 100,
    dataIndex: 'errorFlag_dictText',
    customRender: ({ text, record }) => {
      if (record.errorFlag === 1) {
        return h('span', { style: { color: 'red', fontWeight: 'bold' } }, text);
      }
      return text;
    },
  },
  {
    title: '重试次数',
    align: 'center',
    width: 100,
    dataIndex: 'retryCount',
  },
  {
    title: '创建时间',
    align: 'center',
    width: 180,
    dataIndex: 'createTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '关联点位', order: 5, view: 'number', type: 'number', dictTable: 'mes3_pi_base_point', dictCode: 'tag_id', dictText: 'point_name' },
  collectTime: { title: '对应采集时间段', order: 6, view: 'datetime', type: 'string' },
  finishFlag: {
    title: '是否完成',
    order: 7,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  lastCollectedTime: { title: '最后一次成功采集时间', order: 8, view: 'datetime', type: 'string' },
  errorFlag: {
    title: '是否异常标记',
    order: 9,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  retryCount: { title: '重试次数', order: 10, view: 'number', type: 'number' },
  createTime: { title: '创建时间', order: 11, view: 'datetime', type: 'string' },
};
