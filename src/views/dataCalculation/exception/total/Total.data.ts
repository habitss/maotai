import {FormSchema} from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import {ref} from "vue";
const { t } = useI18n();

//查询数据
//列表页面查询表单
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
    colProps: { span: 7 },
  },
  {
    label: '日期范围',
    field: 'times',
    component: 'RangeDate',
    componentProps: {
      //日期格式化
      format:'YYYY/MM/DD',
      //范围文本描述用集合
      placeholder:['请选择开始日期','请选择结束日期']
    },
    colProps: { span: 7 },
  },
  {
    label: '触发条件',
    field: 'triggerConditions',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_EXCEPTION_TRRIGER_CONDITION\'',
    },
    colProps: {
      span: 6,
    },
  }
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
    text: '异常触发条件分析',
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
      text: '异常触发条件分析',
      left: 'center'
    },
  ],

});


