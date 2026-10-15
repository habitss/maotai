import { h } from 'vue';
import { Tooltip } from 'ant-design-vue';
import { BasicColumn } from '/@/components/Table';

function withTooltip(columns: BasicColumn[]): BasicColumn[] {
  return columns.map((col) => {
    return {
      ...col,
      ellipsis: false, // 不使用默认省略号, 否则 tooltip 被覆盖
      customRender: ({ text }) => {
        if (text === null || text === undefined || text === '') {
          return text;
        }
        return h(Tooltip, { title: String(text) }, { default: () => h('span', {}, String(text)) });
      },
    };
  });
}

//列表数据
export const columns: BasicColumn[] = withTooltip([
  {
    title: '工厂名称 FAC_DESC',
    align: 'center',
    dataIndex: 'facDesc',
    width: 110,
  },
  {
    title: '工厂编码 FAC_CODE',
    align: 'center',
    sorter: true,
    dataIndex: 'facCode',
    width: 110,
  },
  {
    title: '部门名称 DEPAR_DESC',
    align: 'center',
    dataIndex: 'deparDesc',
    width: 110,
  },
  {
    title: '部门编码 DEPAR_CODE',
    align: 'center',
    dataIndex: 'deparCode',
    width: 110,
  },
  {
    title: '工序描述 PROCESS_DESC',
    align: 'center',
    dataIndex: 'processDesc',
    width: 150,
  },
  {
    title: '工序编码 PROCESS_CODE',
    align: 'center',
    dataIndex: 'processCode',
    width: 140,
  },
  {
    title: '产线描述 LINE_DESC',
    align: 'center',
    sorter: true,
    dataIndex: 'lineDesc',
    width: 130,
  },
  {
    title: '产线编码 LINE_CODE',
    align: 'center',
    dataIndex: 'lineCode',
    width: 130,
  },
  {
    title: '功能位置编码 FUNLOC_CODE',
    align: 'center',
    sorter: true,
    dataIndex: 'funlocCode',
  },
  {
    title: '功能位置描述 FUNLOC_DESC',
    align: 'center',
    dataIndex: 'funlocDesc',
  },
  {
    title: '业务区域 IND_BUSI_AREA',
    align: 'center',
    dataIndex: 'indBusiArea',
  },
  {
    title: '中心管理部门 IND_CEN_MAN_DEPT',
    align: 'center',
    dataIndex: 'indCenManDept',
    width: 170,
  },
  {
    title: '引用的指标标准编码 IND_STAND_CODE',
    align: 'center',
    dataIndex: 'indStandCode',
  },
  {
    title: '引用的指标标准名称 IND_STAND_DESC',
    align: 'center',
    dataIndex: 'indStandDesc',
    width: 240,
  },
  {
    title: '指标编码 IND_INST_CODE',
    align: 'center',
    dataIndex: 'indInstCode',
  },
  {
    title: '指标名称 IND_NAME',
    align: 'center',
    sorter: true,
    dataIndex: 'indName',
    width: 240,
  },
  {
    title: '指标实例编码 INST_USE_CODE',
    align: 'center',
    dataIndex: 'instUseCode',
  },
  {
    title: '单位 UNIT',
    align: 'center',
    dataIndex: 'unit',
    width: 80,
  },
  {
    title: '单位中文 UNIT_CN',
    align: 'center',
    dataIndex: 'unitCn',
    width: 100,
  },
  {
    title: '主设备编码 EQUIP_CODE',
    align: 'center',
    dataIndex: 'equipCode',
  },
  {
    title: '主设备描述 EQUIP_DESC',
    align: 'center',
    dataIndex: 'equipDesc',
  },
  {
    title: '主设备序号 EQUIP_NUM',
    align: 'center',
    dataIndex: 'equipNum',
  },
  {
    title: '主设备序号描述 EQUIP_NUM_DESC',
    align: 'center',
    dataIndex: 'equipNumDesc',
  },
  {
    title: '辅件设备编码 ACCE_EQUIP_CODE',
    align: 'center',
    dataIndex: 'acceEquipCode',
    width: 200,
  },
  {
    title: '辅件设备描述 ACCE_EQUIP_DESC',
    align: 'center',
    dataIndex: 'acceEquipDesc',
  },
  {
    title: '部件编码 PART_NUM',
    align: 'center',
    dataIndex: 'partNum',
  },
  {
    title: '部件描述 PART_NUM_DESC',
    align: 'center',
    dataIndex: 'partNumDesc',
  },
  {
    title: '零件 PART',
    align: 'center',
    dataIndex: 'part',
  },
  {
    title: '零件描述 PART_DESC',
    align: 'center',
    dataIndex: 'partDesc',
  },
  {
    title: '业务类型 BUSI_TYPE',
    align: 'center',
    dataIndex: 'busiType',
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
]);

// 高级查询数据
export const superQuerySchema = {
  funlocCode: { title: '功能位置编码 FUNLOC_CODE', order: 2, view: 'text', type: 'string' },
  funlocDesc: { title: '功能位置描述 FUNLOC_DESC', order: 3, view: 'text', type: 'string' },
  indBusiArea: { title: '业务区域 IND_BUSI_AREA', order: 4, view: 'text', type: 'string' },
  indCenManDept: { title: '中心管理部门 IND_CEN_MAN_DEPT', order: 5, view: 'text', type: 'string' },
  indName: { title: '指标名称 IND_NAME', order: 6, view: 'text', type: 'string' },
  indStandCode: { title: '指标标准编码 IND_STAND_CODE', order: 13, view: 'text', type: 'string' },
  indStandDesc: { title: '指标标准描述 IND_STAND_DESC', order: 14, view: 'text', type: 'string' },
  unit: { title: '单位 UNIT', order: 15, view: 'text', type: 'string' },
  unitCn: { title: '单位中文 UNIT_CN', order: 16, view: 'text', type: 'string' },
  facCode: { title: '工厂编码 FAC_CODE', order: 17, view: 'text', type: 'string' },
  facDesc: { title: '工厂名称 FAC_DESC', order: 18, view: 'text', type: 'string' },
  deparCode: { title: '部门编码 DEPAR_CODE', order: 19, view: 'text', type: 'string' },
  deparDesc: { title: '部门名称 DEPAR_DESC', order: 20, view: 'text', type: 'string' },
  processCode: { title: '工序编码 PROCESS_CODE', order: 21, view: 'text', type: 'string' },
  processDesc: { title: '工序描述 PROCESS_DESC', order: 22, view: 'text', type: 'string' },
  lineCode: { title: '产线编码 LINE_CODE', order: 23, view: 'text', type: 'string' },
  lineDesc: { title: '产线描述 LINE_DESC', order: 24, view: 'text', type: 'string' },
  equipCode: { title: '设备编码 EQUIP_CODE', order: 25, view: 'text', type: 'string' },
  equipDesc: { title: '设备描述 EQUIP_DESC', order: 26, view: 'text', type: 'string' },
  equipNum: { title: '设备数量 EQUIP_NUM', order: 27, view: 'text', type: 'string' },
  equipNumDesc: { title: '设备数量描述 EQUIP_NUM_DESC', order: 28, view: 'text', type: 'string' },
  acceEquipCode: { title: '辅件设备编码 ACCE_EQUIP_CODE', order: 29, view: 'text', type: 'string' },
  acceEquipDesc: { title: '辅件设备描述 ACCE_EQUIP_DESC', order: 30, view: 'text', type: 'string' },
  partNum: { title: '部件编码 PART_NUM', order: 31, view: 'text', type: 'string' },
  partNumDesc: { title: '部件描述 PART_NUM_DESC', order: 32, view: 'text', type: 'string' },
  part: { title: '零件 PART', order: 33, view: 'text', type: 'string' },
  partDesc: { title: '零件描述 PART_DESC', order: 34, view: 'text', type: 'string' },
  busiType: { title: '业务类型 BUSI_TYPE', order: 36, view: 'text', type: 'string' },
  indInstCode: { title: '指标实例编码 IND_INST_CODE', order: 37, view: 'text', type: 'string' },
  instUseCode: { title: '实例使用编码 INST_USE_CODE', order: 38, view: 'text', type: 'string' },
  businessStatus: { title: '业务状态 BUSINESS_STATUS', order: 39, view: 'text', type: 'string' },
  instBusinessStatus: { title: '实例业务状态 INST_BUSINESS_STATUS', order: 40, view: 'text', type: 'string' },
  lockLineStatus: { title: '锁产线状态 LOCK_LINE_STATUS', order: 41, view: 'text', type: 'string' },
  enableFlag: { title: '是否启用 enable_flag', order: 42, view: 'text', type: 'string' },
  createTime: { title: '创建时间 create_time', order: 44, view: 'datetime', type: 'string' },
  updateTime: { title: '更新时间 update_time', order: 46, view: 'datetime', type: 'string' },
};
