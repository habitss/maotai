import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '类别名称',
    align: "center",
    dataIndex: 'typeId_dictText',
    width: 200,

  },
  {
    title: '表名称',
    align: "center",
    dataIndex: 'configName'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '表名称',
    field: 'configName',
    component: 'Input',
  },
];
/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}

//配置表格数据
export const configColumns: BasicColumn[] = [
  {
    title: 'id',
    align: "center",
    dataIndex: 'id',
    defaultHidden: true,
    ellipsis: false,
  },
  {
    title: 'text',
    align: "center",
    dataIndex: 'text',
    ellipsis: false,
  },
  {
    title: 'field_1',
    align: "center",
    dataIndex: 'field_1',
    ellipsis: false,
  },
  {
    title: 'field_2',
    align: "center",
    dataIndex: 'field_2',
    ellipsis: false,
  },
  {
    title: 'field_3',
    align: "center",
    dataIndex: 'field_3',
    ellipsis: false,
  },
  {
    title: 'field_4',
    align: "center",
    dataIndex: 'field_4',
    ellipsis: false,
  },
  {
    title: 'scoreText',
    align: "center",
    dataIndex: 'scoreText',
    ellipsis: false,
  },
];

//默认合并规则
export const defaultMergeRuleList = {
  title: {
    0: { rowSpan: 1, colSpan: 5 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 0 },
    3: { rowSpan: 1, colSpan: 0 },
    4: { rowSpan: 1, colSpan: 0 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  influence: {
    0: { rowSpan: 3, colSpan: 2 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 3 },
    3: { rowSpan: 1, colSpan: 0 },
    4: { rowSpan: 1, colSpan: 0 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  influenceText: {
    0: { rowSpan: 0, colSpan: 1 },
    1: { rowSpan: 0, colSpan: 1 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 2, colSpan: 1 },
  },
  influence_1: {
    0: { rowSpan: 0, colSpan: 1 },
    1: { rowSpan: 0, colSpan: 1 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 0, colSpan: 0 },
  },
  requirement: {
    0: { rowSpan: 1, colSpan: 1 },
    1: { rowSpan: 1, colSpan: 1 },
    2: { rowSpan: 1, colSpan: 3 },
    3: { rowSpan: 1, colSpan: 0 },
    4: { rowSpan: 1, colSpan: 0 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  improve: {
    0: { rowSpan: 1, colSpan: 1 },
    1: { rowSpan: 1, colSpan: 1 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  dynamic_innovation: {
    0: { rowSpan: 1, colSpan: 2 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  dynamic_cost: {
    0: { rowSpan: 1, colSpan: 2 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  dynamic_item_1: {
    0: { rowSpan: 1, colSpan: 2 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 1 },
    3: { rowSpan: 1, colSpan: 1 },
    4: { rowSpan: 1, colSpan: 1 },
    5: { rowSpan: 1, colSpan: 1 },
  },
  remark: {
    0: { rowSpan: 1, colSpan: 5 },
    1: { rowSpan: 1, colSpan: 0 },
    2: { rowSpan: 1, colSpan: 0 },
    3: { rowSpan: 1, colSpan: 0 },
    4: { rowSpan: 1, colSpan: 0 },
    5: { rowSpan: 1, colSpan: 1 },
  },
};

//默认表格数据
export const defaultDataSource = [
  { id: 'title', text: '', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '最佳实践评审打分表' },
  { id: 'influence', text: '影响因素', itemScore: '', field_1: '', field_2: '影响水平', field_3: '', field_4: '', scoreText: '评审人' },
  { id: 'influenceText', text: '', itemScore: '', field_1: '', field_2: '低', field_3: '中', field_4: '高', scoreText: '评审人岗位' },
  { id: 'influence_1', text: '', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '' },
  { id: 'requirement', text: '提报要求', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '提报要求' },
  { id: 'improve', text: '改善效果', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '' },
  { id: 'dynamic_innovation', text: '创新性', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '创新性' },
  { id: 'dynamic_cost', text: '实施所需成本', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '实施所需成本' },
  { id: 'dynamic_item_1', text: '推广范围', itemScore: '', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '推广范围' },
  { id: 'remark', text: '备注：强制推广≥23分；建议推广最佳实践≥18分；不推广＜18分', field_1: '', field_2: '', field_3: '', field_4: '', scoreText: '汇总' },
];