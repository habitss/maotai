import { h, ref } from 'vue';
import { Tooltip, SelectProps } from 'ant-design-vue';
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
    sorter: true,
    dataIndex: 'deparCode',
    width: 120,
  },
  {
    title: '工序描述 PROCESS_DESC',
    align: 'center',
    dataIndex: 'processDesc',
    width: 150,
  },
  {
    title: '工序编码 PROCESS_COD',
    align: 'center',
    dataIndex: 'processCod',
    width: 140,
  },
  {
    title: '产线编码 LINE_CODE',
    align: 'center',
    sorter: true,
    dataIndex: 'lineCode',
    width: 130,
  },
  {
    title: '产线子编码 LINE_S_CODE',
    align: 'center',
    dataIndex: 'lineSCode',
    width: 130,
  },
  {
    title: '工厂配方名称 FAC_RECIPE_NAME',
    align: 'center',
    dataIndex: 'facRecipeName',
  },
  {
    title: '工厂配方编码 FAC_RECIPE_CODE',
    align: 'center',
    dataIndex: 'facRecipeCode',
  },
  {
    title: '配方号 RECIPE_NUM',
    align: 'center',
    dataIndex: 'recipeNum',
    width: 120,
  },
  {
    title: '区域配方编码 ZONE_RECIPE_CODE',
    align: 'center',
    dataIndex: 'zoneRecipeCode',
    width: 160,
  },
  {
    title: '区域配方中文名 ZONE_RECIPE_CN_NAME',
    align: 'center',
    dataIndex: 'zoneRecipeCnName',
    width: 200,
  },
  {
    title: '子品牌原始编码 SUB_BRAND_OG_CODE',
    align: 'center',
    dataIndex: 'subBrandOgCode',
    width: 200,
  },
  {
    title: '子品牌中文名称 SUB_BRAND_OG_CN_NAME',
    align: 'center',
    dataIndex: 'subBrandOgCnName',
    width: 230,
  },
  {
    title: '配方类型代码 RECIPE_TYPE',
    align: 'center',
    dataIndex: 'recipeType',
    width: 120,
  },
  {
    title: '配方类型描述 RECIPE_TYPE_DESC',
    align: 'center',
    dataIndex: 'recipeTypeDesc',
    width: 160,
  },
  {
    title: '设备代码 EQUIP_CODE',
    align: 'center',
    dataIndex: 'equipCode',
  },
  {
    title: '设备描述 EQUIP_DESC',
    align: 'center',
    dataIndex: 'equipDesc',
  },
  {
    title: '生效时间 EFFE_DATE',
    align: 'center',
    dataIndex: 'effeDate',
  },
  {
    title: '失效时间 EXPI_DATE',
    align: 'center',
    dataIndex: 'expiDate',
  },
  {
    title: '业务状态 BUSINE_STATUS',
    align: 'center',
    dataIndex: 'busineStatus',
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
  lineCode: { title: '产线编码 LINE_CODE', order: 0, view: 'text', type: 'string' },
  facCode: { title: '工厂编码 FAC_CODE', order: 1, view: 'text', type: 'string' },
  facDesc: { title: '工厂名称 FAC_DESC', order: 2, view: 'text', type: 'string' },
  deparCode: { title: '部门编码 DEPAR_CODE', order: 3, view: 'text', type: 'string' },
  deparDesc: { title: '部门名称 DEPAR_DESC', order: 4, view: 'text', type: 'string' },
  processCod: { title: '工序编码 PROCESS_COD', order: 5, view: 'text', type: 'string' },
  processDesc: { title: '工序描述 PROCESS_DESC', order: 6, view: 'text', type: 'string' },
  lineSCode: { title: '产线子编码 LINE_S_CODE', order: 7, view: 'text', type: 'string' },
  recipeNum: { title: '配方号 RECIPE_NUM', order: 8, view: 'text', type: 'string' },
  recipeType: { title: '配方类型代码 RECIPE_TYPE', order: 9, view: 'text', type: 'string' },
  recipeTypeDesc: { title: '配方类型描述 RECIPE_TYPE_DESC', order: 10, view: 'text', type: 'string' },
  facRecipeName: { title: '工厂配方名称 FAC_RECIPE_NAME', order: 11, view: 'text', type: 'string' },
  facRecipeCode: { title: '工厂配方编码 FAC_RECIPE_CODE', order: 12, view: 'text', type: 'string' },
  zoneRecipeCode: { title: '区域配方编码 ZONE_RECIPE_CODE', order: 13, view: 'text', type: 'string' },
  zoneRecipeCnName: { title: '区域配方中文名 ZONE_RECIPE_CN_NAME', order: 14, view: 'text', type: 'string' },
  subBrandOgCode: { title: '子品牌原始编码 SUB_BRAND_OG_CODE', order: 15, view: 'text', type: 'string' },
  subBrandOgCnName: { title: '子品牌中文名称 SUB_BRAND_OG_CN_NAME', order: 16, view: 'text', type: 'string' },
  equipCode: { title: '设备代码 EQUIP_CODE', order: 17, view: 'text', type: 'string' },
  equipDesc: { title: '设备描述 EQUIP_DESC', order: 18, view: 'text', type: 'string' },
  effeDate: { title: '生效时间 EFFE_DATE', order: 19, view: 'datetime', type: 'string' },
  expiDate: { title: '失效时间 EXPI_DATE', order: 20, view: 'datetime', type: 'string' },
  busineStatus: { title: '业务状态 BUSINE_STATUS', order: 21, view: 'text', type: 'string' },
  enableFlag: { title: '是否启用', order: 24, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 26, view: 'datetime', type: 'string' },
  updateTime: { title: '更新时间', order: 28, view: 'datetime', type: 'string' },
};

//有效状态  有效-Y 无效-N 停用-S
export const enableFlagOptions = ref<SelectProps['options']>([
  { label: '有效', value: 'Y' },
  { label: '无效', value: 'N' },
  { label: '停用', value: 'S' },
]);

//使用状态  使用中 停用中
export const busineStatusOptions = ref<SelectProps['options']>([
  { label: '使用中', value: '使用中' },
  { label: '停用中', value: '停用中' },
]);
