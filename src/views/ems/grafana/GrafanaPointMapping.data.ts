import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '旧厂点位ID或字段',
    align: 'center',
    dataIndex: 'oldPointId',
  },
  {
    title: '旧厂点位中文描述(若有)',
    align: 'center',
    dataIndex: 'oldPointName',
  },
  {
    title: '新厂候选点位ID(确认后写入)',
    align: 'center',
    dataIndex: 'newPointId',
  },
  {
    title: '新厂点位中文描述',
    align: 'center',
    dataIndex: 'newPointName',
  },
  {
    title: '匹配得分(0 ~ 1)',
    align: 'center',
    dataIndex: 'score',
  },
  {
    title: '匹配度(high medium low)',
    align: 'center',
    dataIndex: 'confidence_dictText',
  },
  {
    title: '匹配类型(auto manual)',
    align: 'center',
    dataIndex: 'matchedFlag_dictText',
  },
];

// 高级查询数据
export const superQuerySchema = {
  oldPointId: { title: '旧厂点位ID或字段', order: 0, view: 'text', type: 'string' },
  oldPointName: { title: '旧厂点位中文描述(若有)', order: 1, view: 'text', type: 'string' },
  newPointId: { title: '新厂候选点位ID(确认后写入)', order: 2, view: 'text', type: 'string' },
  newPointName: { title: '新厂点位中文描述', order: 3, view: 'text', type: 'string' },
  score: { title: '匹配得分(0 ~ 1)', order: 4, view: 'number', type: 'number' },
  confidence: {
    title: '匹配度(high medium low)',
    order: 5,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_Confidence'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  matchedFlag: {
    title: '匹配类型(auto manual)',
    order: 6,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict where group_code = 'GF_MatchedFlag'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
};
