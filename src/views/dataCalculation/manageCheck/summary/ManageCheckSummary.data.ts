import { ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import dayjs from 'dayjs';
import { getCurrentDepart } from '/@/utils/common/compUtils';
export const currentDepart = getCurrentDepart();
//隐藏类型：1-发现人，2-班组，3-岗位，4-责任人
export const hideType = ref([] as any);
//月份区间默认值，默认为本月月初到月末
export const monthRange = ref([
  dayjs().startOf('month').format('YYYY-MM-DD'), 
  dayjs().endOf('month').format('YYYY-MM-DD')
] as any);
//禁用日期选择
const hackValue = ref();

//岗位查询配置
const postSearchFormSchema = {
  component: 'JDictSelectTag',
  componentProps: {
    dictCode: "mes3_mdm_post, post_name, id, del_flag = 0",
    showChooseOption: false,
    mode: 'multiple',
    maxTagCount: 2,
  },
} as FormSchema;

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "日期",
    field: 'checkDate',
    component: 'RangePicker',
    componentProps: {
      format:'YYYY-MM-DD',
      valueFormat:'YYYY-MM-DD',
      allowClear: false,
      onChange: function(value) {
        //设置时间
        monthRange.value = value;
      },
      // openChange: function(open: boolean){
      //   if (open) {
      //     monthRange.value = [] as any;
      //     hackValue.value = [] as any;
      //   } else {
      //     hackValue.value = undefined;
      //   }
      // },
      onCalendarChange : function(value) {
        monthRange.value = value;
      },
      // disabledDate,
    },
    defaultValue: monthRange
  },
  {
    label: "检查部门",
    field: 'checkUserDept',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
      multiple: true,
    },
    defaultValue: currentDepart.id,
  },
  {
    label: "责任部门",
    field: 'checkOwnerDept',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
      multiple: true,
    },
    defaultValue: currentDepart.id,
  },
  {
    ...postSearchFormSchema,
    label: "检查岗位",
    field: 'checkUserPost',
  },
  {
    ...postSearchFormSchema,
    label: "责任岗位",
    field: 'checkOwnerPost',
  },
  {
    label: "问题分类",
    field: 'problemType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_manage_check_category, category_name, code, del_flag = 0 and pid = 0",
      showChooseOption: false,
    },
  },
  {
    label: "问题发现人",
    field: 'checkUser',
    component: 'KtUserScrollSelect',
  },
  {
    label: "查询部门子级",
    field: 'queryDeptChild',
    component: 'RadioGroup',
    componentProps: {
      options: [
        {label: '是', value: 1},
        {label: '否', value: 0},
      ]
    },
    defaultValue: 0,
  },
  {
    label: "",
    field: 'hideType',
    component: 'CheckboxGroup',
    componentProps: {
      name: 'hideType',
      options: [
        { label: '隐藏发现人', value: 1 },
        { label: '隐藏班组', value: 2 },
        { label: '隐藏岗位', value: 3 },
        { label: '隐藏责任人', value: 4 },
      ],
      onChange: function(values){
        hideType.value = values;
      }
    },
    colProps: {span: 7}
  },
];

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '日期',
    align: "center",
    dataIndex: 'dynamicDate',
    customCell: (record, index) => {
      let rowSpan = 0;
      if(index === 0){
        rowSpan = record.rowSpan;
      }
      return { rowSpan };
    },
  },
  {
    title: '问题发现人',
    align: "center",
    dataIndex: 'dynamicText'
  },
  {
    title: '问题汇总',
    align: "center",
    dataIndex: 'problemCount'
  },
  {
    title: '5S问题汇总(问题条数/总问题数)',
    align: "center",
    dataIndex: 'fivesCount'
  },
  {
    title: '非5S问题汇总(问题条数/总问题数)',
    align: "center",
    dataIndex: 'noFivesCount'
  },
  {
    title: '5S问题完成数',
    align: "center",
    dataIndex: 'fivesFinishCount'
  },
  {
    title: '非5S问题完成数',
    align: "center",
    dataIndex: 'noFivesFinishCount'
  },
  {
    title: '5S问题完成率',
    align: "center",
    dataIndex: 'fivesFinishRate',
    customRender: ({ text }) => {
      return text + '%';
    }
  },
  {
    title: '非5S问题完成率',
    align: "center",
    dataIndex: 'noFivesFinishRate',
    customRender: ({ text }) => {
      return text + '%';
    }
  },
  {
    title: '完成总数',
    align: "center",
    dataIndex: 'finishCount',
  },
  {
    title: '完成率',
    align: "center",
    dataIndex: 'finishRate',
    customRender: ({ text }) => {
      return text + '%';
    }
  },
];

//柱状图配置
export const barOption = ref({
  grid: {
    show: false,
    top: '4%',
    right: '4%',
    left: '4%'
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
  legend: {
    top: 'bottom',
    type: 'scroll',
  },
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      type: 'pie',
      radius: [60, 300],
      center: ['50%', '50%'],
      roseType: 'area',
      padAngle: 1,
      itemStyle: {
        borderRadius: 20,
      }
    }
  ]
});

//月份可选范围限制，最多可选3个月
function disabledDate(current){
  if (!monthRange.value || (monthRange.value as any).length === 0) {
    return false;
  }
  const tooLate = dayjs(monthRange.value[0]) && current.diff(monthRange.value[0], 'month') > 2;
  const tooEarly = dayjs(monthRange.value[1]) && dayjs(monthRange.value[1]).diff(current, 'month') > 2;
  return tooEarly || tooLate;
}