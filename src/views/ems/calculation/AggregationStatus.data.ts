import { BasicColumn } from '/@/components/Table';
import { h } from 'vue';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: 'center',
    width: 150,
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '点位大类',
    align: 'center',
    sorter: true,
    width: 130,
    dataIndex: 'tagKind_dictText',
  },
  {
    title: '关联点位',
    align: 'left',
    dataIndex: 'tagName',
    customRender: ({ text, record }) => {
      if (!text) return '';
      return `[${record.tagId}]${text}`;
    },
  },
  {
    title: '时间周期',
    align: 'center',
    sorter: true,
    width: 100,
    dataIndex: 'grainType_dictText',
  },
  {
    title: '时间点',
    align: 'center',
    sorter: true,
    width: 180,
    dataIndex: 'timePoint',
    customRender: ({ text }) => {
      if (!text) return '';
      return text.split(':')[0] + ':' + text.split(':')[1];
    },
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
      if (record.finishFlag === 0) {
        return h('span', { style: { color: 'red', fontWeight: 'bold' } }, text);
      }
      return text;
    },
  },
  {
    title: '异常原因',
    align: 'center',
    dataIndex: 'errorMsg',
    customRender: ({ text, record }) => {
      if (record.finishFlag === 0) {
        return h('span', { style: { color: 'red', fontWeight: 'bold' } }, text);
      }
      return text;
    },
  },
  {
    title: '更新时间',
    align: 'center',
    sorter: true,
    width: 180,
    dataIndex: 'updateTime',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '关联点位', order: 5, view: 'list', type: 'string', dictCode: '' },
  grainType: {
    title: '粒度类型',
    order: 6,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_GrainType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  tagKind: {
    title: '点位类型',
    order: 7,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_TagKind'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  timePoint: { title: '时间点', order: 8, view: 'datetime', type: 'string' },
  finishFlag: {
    title: '是否完成',
    order: 9,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  errorFlag: {
    title: '是否异常',
    order: 10,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  retryCount: { title: '重试次数', order: 11, view: 'number', type: 'number' },
  createTime: { title: '创建时间', order: 12, view: 'datetime', type: 'string' },
};
