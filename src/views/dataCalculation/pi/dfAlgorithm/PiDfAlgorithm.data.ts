import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '取数表',
    align: "center",
    dataIndex: 'dataTableName_dictText'
  },
  {
    title: '指标标准',
    align: "center",
    dataIndex: 'indStand_dictText'
  },
  {
    title: '实例化指标',
    align: "center",
    dataIndex: 'indicator_dictText'
  },
];

//计算模型列表数据
export const calcModelColumns: BasicColumn[] = [
  {
    title: 'PI字段',
    align: "center",
    dataIndex: 'piFieldName'
  },
  {
    title: 'DF字段',
    align: "center",
    dataIndex: 'dfFieldName'
  },
  {
    title: '是否覆盖',
    align: "center",
    dataIndex: 'ifCover',
    width: 80
  },
  {
    title: '汉化方式',
    align: "center",
    dataIndex: 'ifSinicized'
  },
  {
    title: '汉化模型',
    align: "center",
    dataIndex: 'sinicizedDic'
  },
  {
    title: '汉化主键字段',
    align: "center",
    dataIndex: 'sinicizedKey'
  },
  {
    title: '汉化取值字段',
    align: "center",
    dataIndex: 'sinicizedValue'
  },
  {
    title: '结果类型',
    align: "center",
    dataIndex: 'resultType',
    width: 120
  },
];