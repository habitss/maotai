import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '检查项目/指标',
    align: "center",
    dataIndex: 'itemName'
  },
  {
    title: '检查标准',
    align: "center",
    dataIndex: 'itemStandard'
  },
  {
    title: '检查数据填写说明',
    align: "center",
    dataIndex: 'itemDetail'
  },
  {
    title: '结果类型',
    align: "center",
    width: 120,
    dataIndex: 'itemResultTypeCode_dictText'
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: 120,
    slots: { customRender: 'sortSlots' },
  },
];