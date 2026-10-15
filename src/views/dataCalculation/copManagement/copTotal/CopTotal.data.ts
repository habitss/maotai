import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from "dayjs";
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '名称',
    align: "center",
    dataIndex: 'name',
    fixed:'left',
    ellipsis: true,
  },
  {
    title: '未执行',
    align: "center",
    dataIndex: 'noExecute',
    ellipsis: true,
  },
  {
    title: '计划',
    align: "center",
    dataIndex: 'plan',
    ellipsis: true,
  },
  {
    title: '不合格',
    align: "center",
    dataIndex: 'unqualified',
    ellipsis: true,
  },
  {
    title: '已完成',
    align: "center",
    dataIndex: 'complete',
    ellipsis: true,
  },
  {
    title: '延迟',
    align: "center",
    dataIndex: 'delay',
    ellipsis: true,
  },
  {
    title: '延迟完成',
    align: "center",
    dataIndex: 'delayComplete',
    ellipsis: true,
  },
  {
    title: '待审核',
    align: "center",
    dataIndex: 'check',
    ellipsis: true,
  },
  {
    title: '因生产取消',
    align: "center",
    dataIndex: 'noExecutePlan',
    ellipsis: true,
  },
  {
    title: '理论完成数',
    align: "center",
    dataIndex: 'completeCount',
    ellipsis: true,
  },
  {
    title: '合计',
    align: "center",
    dataIndex: 'total',
    ellipsis: true,
  },
  {
    title: '完成率',
    align: "center",
    dataIndex: 'completeRate',
    ellipsis: true,
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 6 },
  },
  {
    label: '年',
    field: 'year',
    component: 'DatePicker',
    componentProps: {
      picker: 'year',
      format: 'YYYY',
      valueFormat: 'YYYY',
    },
    defaultValue: dayjs().format('YYYY'),
    colProps: { span: 6 },
  },
  {
    label: '月',
    field: 'months',
    component: 'Select',
    componentProps: {
      options: [
        {value: '1', label: '一月'},
        {value: '2', label: '二月'},
        {value: '3', label: '三月'},
        {value: '4', label: '四月'},
        {value: '5', label: '五月'},
        {value: '6', label: '六月'},
        {value: '7', label: '七月'},
        {value: '8', label: '八月'},
        {value: '9', label: '九月'},
        {value: '10', label: '十月'},
        {value: '11', label: '十一月'},
        {value: '12', label: '十二月'},
      ],
      //下拉多选
      mode: 'multiple',
    },
    defaultValue: (new Date().getMonth() + 1).toString(),
    colProps: { span: 6 },
  },
  {
    label: '统计对象',
    field: 'type',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '检查人',
          value: '1',
        },
        {
          label: '审核人',
          value: '2',
        },
      ],
    },
    colProps: { span: 6 },
  },
  {
    label: '检查频次',
    field: 'frequencys',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
      mode: 'multiple',
    },
    colProps: { span: 6 },
    //下拉多选

  },
];

//柱状图颜色配置
export const colorArray = [
  {type: '进行中', color: '#FFFF00'},
  {type: '已完成', color: '#00FF00'},
  {type: '延迟', color: '#FE0000'},
];

//柱状图配置
export const barOption = ref({
  title: {
    text: '清洗分布',
    left: 'center'
  },
  grid: {
    show: false,
    top: '12%',
    right: '20%',
    bottom: '22%',
    left: '10%'
  },
  legend: {
    orient: 'vertical',
    y: 'center',
    x: 'right',
    color: ['yellow', '#FE0000', '#00FF00'],
    padding: [1, 40, 3, 1],
    itemGap: 20,
  },
  dataZoom: [
    {
      type: 'slider',
      show: true,
      start: 0,
      end: 30,
    },
    {
      type: 'inside',
      show: true,
      start: 0,
      end: 100,
      xAxisIndex: [0],
      filterMode: 'filter'
    },
  ],
});

//饼图配置
export const pieOption = ref({
  title: [
    {
      text: '清洗分布图',
      left: 'center'
    },
  ],

});
