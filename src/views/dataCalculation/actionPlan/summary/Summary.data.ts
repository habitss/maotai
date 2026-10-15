import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { loadTreeData } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import dayjs from 'dayjs';
import { getCurrentDepart } from '/@/utils/common/compUtils';
export const currentDepart = getCurrentDepart();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '车间行动计划汇总',
    align: "center",
    dataIndex: 'practicalFinishDate',
    children: [
      {
        title: '部门、人员',
        dataIndex: 'title',
        width: 140,
        align: 'left',
        key: 'title',
      },

      {
        title: '行动总数',
        dataIndex: 'actionTotal',
        width: '6vw',
        align: 'center',
      },

      {
        title: '行动完成数',
        dataIndex: 'finishedStateTotal',
        width: '6vw',
        align: 'center',
        slots: { customRender: 'totalSlots' },
      },
      {
        title: '行动完成率',
        dataIndex: 'finishedStateRatio',
        width: '6vw',
        align: 'center',
        customRender: ({ text }) => {
          return text.toFixed(2) + '%';
        },
        customCell(record) {
          return {
            style: {
              'background': `linear-gradient(to right, #00FF00 ${(record.finishedStateRatio) + '%'}, white 0%)`
            }
          }
        },
      },
      {
        title: '进行中',
        width: '6vw',
        dataIndex: 'progressStateTotal',
        align: 'center',
        slots: { customRender: 'totalSlots' },
      },
      {
        title: '取消数',
        width: 60,
        dataIndex: 'closeStateTotal',
        align: 'center',
      },
      {
        title: '行动延迟数',
        width: '6vw',
        dataIndex: 'delayStateTotal',
        align: 'center',
        slots: { customRender: 'totalSlots' },
      },
      {
        title: '行动延迟率',
        width: '6vw',
        dataIndex: 'delayStateRatio',
        align: 'center',
        customRender: ({ text }) => {
          return text.toFixed(2) + '%';
        },
        customCell(record) {
          return {
            style: {
              'background': `linear-gradient(to right, #FE0000 ${(record.delayStateRatio) + '%'}, white 0%)`,
            }
          }
        },
      },
      {
        title: '延迟完成次数',
        width: '6vw',
        dataIndex: 'delayFinishStateTotal',
        align: 'center',
        slots: { customRender: 'totalSlots' },
      },
      {
        title: '延迟完成率',
        width: '6vw',
        dataIndex: 'delayFinishStateRatio',
        align: 'center',
        customRender: ({ text }) => {
          return text.toFixed(2) + '%';
        },
        customCell(record) {
          return {
            style: {
              'background': `linear-gradient(to right, #FE0000 ${(record.delayStateRatio) + '%'}, white 0%)`,
            }
          }
        },
      },
      {
        title: '第一次延期数',
        width: '6vw',
        dataIndex: 'oneDelayTotal',
        align: 'center',
      },
      {
        title: '第二次延期数',
        width: '6vw',
        dataIndex: 'twoDelayTotal',
        align: 'center',
      },
      {
        title: '第三次延期数',
        width: '6vw',
        dataIndex: 'threeDelayTotal',
        align: 'center',
      },
      {
        title: '当前延迟总数',
        width: '6vw',
        dataIndex: 'delayTotal',
        align: 'center',
      },
      {
        title: '延迟率',
        dataIndex: 'delayRatio',
        width: '6vw',
        align: 'center',
        customRender: ({ text }) => {
          return text.toFixed(2) + '%';
        },
        customCell(record) {
          return {
            style: {
              'background': `linear-gradient(to right, #FE0000 ${(record.delayRatio) + '%'}, white 0%)`,
            }
          }
        },
      },
      {
        title: '提出人激励',
        dataIndex: 'putKindSum',
        width: '6vw',
        align: 'center',
      },
      {
        title: '责任人激励',
        dataIndex: 'dutKindSum',
        width: '6vw',
        align: 'center',
      },
      {
        title: '执行人激励',
        dataIndex: 'executeKind',
        width: '6vw',
        align: 'center',
      },
      {
        title: '确认人激励',
        dataIndex: 'verifyKind',
        width: '6vw',
        align: 'center',
      },
    ]
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '检索对象',
    field: 'objectType',
    component: 'Select',
    componentProps: {
      options: [
        { label: '提出日期', value: '1' },
        { label: '计划完成日期', value: '2' },
        { label: '实际完成日期', value: '3' },
      ],
    },
    defaultValue: '1',
  },
  {
    label: '日期检索',
    field: 'dateType',
    component: 'Select',
    componentProps: {
      options: [
        { label: '按年', value: '4' },
        { label: '按月', value: '1' },
        { label: '按天', value: '2' },
        { label: '日期区间', value: '3' },
      ],
    },
    defaultValue: '1',
  },
  {
    label: '按年检索',
    field: 'yearField',
    component: 'DatePicker',
    componentProps: {
      format: 'YYYY',
      valueFormat: 'YYYY-MM-DD',
      picker: 'year'
    },
    defaultValue: dayjs().format('YYYY-MM-DD'),
    ifShow: ({ values }) => {
      return values.dateType == '4';
    },
  },
  {
    label: '按月检索',
    field: 'monthField',
    component: 'MonthPicker',
    componentProps: {
      format: 'YYYY-MM',
      valueFormat: 'YYYY-MM-DD',
    },
    defaultValue: dayjs().format('YYYY-MM-DD'),
    ifShow: ({ values }) => {
      return values.dateType == '1';
    },
  },
  {
    label: '按天检索',
    field: 'dayField',
    component: 'DatePicker',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    defaultValue: dayjs().format('YYYY-MM-DD'),
    ifShow: ({ values }) => {
      return values.dateType == '2';
    },
  },
  {
    label: '日期区间',
    field: 'dateRangeField',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    defaultValue: dayjs().format('YYYY-MM-DD')+','+dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
    ifShow: ({ values }) => {
      return values.dateType == '3';
    },
  },
  {
    label: '问题来源',
    field: 'problemTypeIds',
    component: 'ApiTreeSelect',
    componentProps: {
      api: loadTreeData,
      params: { async: false, pcode: '0' },
      fieldNames: { label: 'title', value: 'key' },
      showSearch: true,
      treeNodeFilterProp: 'title',
      multiple: 'multiple',
    },
  },
  {
    label: '部门',
    field: 'deptIds',
    component: 'JSelectDept',
    componentProps: {
      labelKey: 'title',
      rowKey: 'key',
      checkStrictly: true,
      sync: false,
      defaultExpandLevel: 3,
      showButton: false,
      maxTagCount: 2,
      search: true,
      expandOnSearch: true,
      highlight: true,
    },
    defaultValue: currentDepart.id,
  },
  {
    label: '',
    field: 'dutyOrPut',
    component: 'RadioButtonGroup',
    componentProps: {
      options: [
        { label: '责任', value: '1' },
        { label: '提出', value: '2' },
      ]
    },
    defaultValue: '1',
    colProps: { span: 3 },
  },
  {
    label: '',
    field: 'userOrDept',
    component: 'RadioButtonGroup',
    componentProps: {
      type: 'radioButton',
      options: [
        { label: '个人', value: '1' },
        { label: '部门', value: '2' },
      ]
    },
    defaultValue: '1',
    colProps: { span: 3 },
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
    text: '个人行动统计',
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
      text: '延迟率统计',
      left: 'center'
    },
  ],
  legend: {
    orient: 'vertical',
    y: 'center',
    x: 'right',
    color: ['yellow', '#FE0000', '#00FF00'],
    padding: [1, 40, 3, 1],
    itemGap: 20,
  },
  series: [
    {
      name: '',
      type: 'pie',
      padAngle: 2,
      itemStyle: {
        borderRadius: 10,
        color: function (params) {
          const findItem = colorArray.find((item: any) => item.type === params.name);
          return findItem?.color;
        }
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 40,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
    }
  ]
});