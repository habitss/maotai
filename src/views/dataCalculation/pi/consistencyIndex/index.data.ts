import { ref } from 'vue';
import { BasicColumn,FormSchema } from '/@/components/Table';
import { getColor} from '/@/utils/common/piColorUtils';
const tableListRef = ref();
const disbledFlag = ref(false);
//柱状图配置
export const barOption = {
  title: {
    text: '重复异常率',
    left: 'center',
    top: '20px'
  },
  grid: {
    show: false,
    top: '12%',
    right: '4%',
    bottom: '12%',
    left: '4%'
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b0}<br />重复率：{c0}%',
  },
  series: [
    {
      name: '重复异常率',
      type: 'bar',
      label: {
        show: true,
        position: 'top',
        color: '#000',
        formatter: '{c}%'
      }
    },
  ],
  visualMap: {
    show: false,
    dimension: 1,
    //值大于等于20显示红色，否则显示绿色
    pieces: [
      {
        gte: 20,
        color: '#FF0000'
      },
      {
        lt: 20,
        color: '#00af50' 
      }
    ]
  }
};

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '报表名称',
    align: "center",
    dataIndex: 'tableName',
    width:120,
  },
  {
    title: 'PI名称',
    align: "center",
    dataIndex: 'fieldName',
    width:200,
  },
  {
    title: '绿区数',
    align: "center",
    dataIndex: 'green',
    width:80,
  },
  {
    title: '黄区数',
    align: "center",
    dataIndex: 'yellow',
    width:80,
  },
  {
    title: '红区数',
    align: "center",
    dataIndex: 'red',
    width:80,
  },
  {
    title: '合计',
    align: "center",
    dataIndex: 'all',
    width:80,
  },
  {
    title: '绿区率目标值',
    align: "center",
    dataIndex: 'greenRateTarget',
    width:120,
  },
  {
    title: '绿区率',
    align: "center",
    dataIndex: 'greenProportion',
    width:80,
    customCell(record) {
      const greenProportion = Number(record.greenProportion);
      const greenRateTarget = Number(record.greenRateTarget);
      let colorObj = { bgColor: '' }
      if (record.all == 0) {
        colorObj.bgColor = getColor('grey');
      } else if (greenProportion >= greenRateTarget) {
        colorObj.bgColor = getColor('green');
      } else if (greenProportion < greenRateTarget) {
        colorObj.bgColor = getColor('red');
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
  },
  {
    title: '黄区率',
    align: "center",
    dataIndex: 'yellowProportion',
    width:80,
  },
  {
    title: '红区率',
    align: "center",
    dataIndex: 'redProportion',
    width:80,
  },
  {
    title: '异常总数',
    align: "center",
    dataIndex: 'anomalyTotal',
    width:80,
  },
  {
    title: '重复异常数',
    align: "center",
    dataIndex: 'anomalyRepeatTotal',
    width:80,
    slots: { customRender: 'anomalyRepeatTotalSlots' },
  },
  {
    title: '重复异常率',
    align: "center",
    dataIndex: 'anomalyRepeatRate',
    width:80,
    customRender: ({ text }) => {
      return text + '%';
    }
  },
  {
    title: '班组评价合格数',
    align: "center",
    dataIndex: 'teamEvaluationTotal',
    width:80,
  },
  {
    title: '5W分析数',
    align: "center",
    dataIndex: 'fiveWhyTotal',
    width:80,
  },
  {
    title: '5W分析率(修正)',
    align: "center",
    dataIndex: 'fiveWhyAmendRate',
    width:80,
  },
  {
    title: '5W分析率(实际)',
    align: "center",
    dataIndex: 'fiveWhyActualRate',
    width:80,
    customRender: ({ text }) => {
      return text + '%';
    }
  },
  {
    title: '车间5w评审数',
    align: "center",
    dataIndex: 'fiveWhyWorkshopEvaluationTotal',
    width:80,
  },
  {
    title: '车间5W评价合格数',
    align: "center",
    dataIndex: 'fiveWhyWorkshopEvaluationOkTotal',
    width:80,
  },
  {
    title: '车间5W自查合格数',
    align: "center",
    dataIndex: 'fiveWhySelfCheckOkTotal',
    width:80,
  },
  {
    title: '5分位值',
    align: "center",
    dataIndex: 'fivePercentile',
    width:80,
  },
  {
    title: '10分位值',
    align: "center",
    dataIndex: 'tenPercentile',
    width:80,
  },
  {
    title: '50分位值',
    align: "center",
    dataIndex: 'fiftyPercentile',
    width:80,
  },
  {
    title: '90分位值',
    align: "center",
    dataIndex: 'ninetyPercentile',
    width:80,
  },
  {
    title: '95分位值',
    align: "center",
    dataIndex: 'ninetyFivePercentile',
    width:80,
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        allowClear: false,
        selectType: 'depart',
        onChange: async () => {
          formModel.lineCode = null;
          formModel.tableIds = null;
          disbledFlag.value = false;
          disbledFlag.value = await tableListRef.value.reloadData(formModel);
        }
      }
    },
    required: true,
    colProps: { span: 6 },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
        mode: "multiple",
        onChange: async () => {
          await tableListRef.value.reloadData(formModel);
        },
        disabled: !disbledFlag.value
      }
    },
    colProps: { span: 6 },
  },
  {
    label: '报表名称',
    field: 'tableIds',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = 0 and dept_code = '"+formModel.deptCode+"', name, id",
        showChooseOption: false,
        mode: 'multiple',
        onChange: async () => {
          await tableListRef.value.reloadData(formModel);
        },
        disabled: !disbledFlag.value
      }
    }
  },
  {
    label: 'PI名称',
    field: 'piName',
    component: 'Input',
  },
  {
    label: '开始时间',
    field: 'queryTime',
    component: 'Input',
    slot: 'queryTime',
  },
  {
    label: '报表查询字段',
    field: 'queryField',
    component: 'Input',
    slot: 'queryFieldSlot',
    colProps: { span: 18 },
  }
];

export function setTableListRef(val){
  tableListRef.value = val;
}