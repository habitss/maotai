import { FormSchema, BasicColumn } from '/@/components/Table';
import { queryAllTreeData } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import { mes3UserListAll } from '@/api/common/api';
import { getPopupContainer } from '/@/utils';
import { useUserStore } from '/@/store/modules/user';
import dayjs from 'dayjs';
// 用户信息
const { userInfo } = useUserStore();
import { getColor} from '/@/utils/common/piColorUtils';

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: 'PI当前实际值',
    field: 'actualValue',
    component: 'Input',
    colProps: { span: 6 },
    dynamicDisabled: true
  },
  {
    label: '问题类别',
    field: 'problemType',
    component: 'ApiTreeSelect',
    componentProps: {
      api: queryAllTreeData,
      params: { async: false, pcode: '0' },
      fieldNames: {children: 'childList', label: 'problemName', value: 'id',},
      showSearch: true,
      treeNodeFilterProp: 'problemName',
    },
    required: true,
    labelWidth: 140,
    labelLength: 4,
    colProps: { span: 6 },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '异常发生时间',
    field: 'abnormalTime',
    component: 'DatePicker',
    componentProps: {
      valueFormat:'YYYY-MM-DD HH:mm:ss',
    },
    labelWidth: 140,
    labelLength: 6,
    colProps: { span: 6 },
    defaultValue: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '分析状态',
    field: 'state',
    component: 'RadioButtonGroup',
    componentProps: {
      showChooseOption: false,
      options: [
        { label: '重复', value: 1 },
        { label: '偶发', value: 2 },
      ],
    },
    defaultValue: 2,
    labelWidth: 140,
    labelLength: 4,
    colProps: { span: 6 },
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '重复异常类型',
    field: 'abnormalType',
    component: 'JDictSelectTag',
    componentProps: ({ formModel })=>{
      return {
        dictCode: `mes3_pi_repeat_exception_type where del_flag = 0 and pi_table_code = '${formModel.tableCode}' and pi_field_code = '${formModel.fieldCode}', exception_name, id`,
        type: 'select',
        labelField: 'item_key',
        valueField: 'item_val',
        showChooseOption: false,
        showSearch: true
      }
    },
    // required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    ifShow: ({ values }) => {
      return values.state == 1;
    },
  },
  {
    label: '班组评价',
    field: 'teamEvaluation',
    component: 'Input',
    slot: 'evaluate',
    colProps: { span: 6 },
  },
  {
    label: '车间评价',
    field: 'workshopEvaluation',
    component: 'Input',
    slot: 'evaluate',
    colProps: { span: 6 },
  },
  {
    label: '异常描述',
    field: 'abnormalDescribe',
    component: 'InputTextArea',
    labelWidth: 140,
    labelLength: 4,
    colProps: { span: 24 },
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '异常点',
    field: 'abnormalAnalysis',
    component: 'Input',
    labelWidth: 140,
    labelLength: 3,
    colProps: { span: 24 },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    colSlot: 'abnormalAnalysis'
  },
  {
    label: '实际表名',
    field: 'tableCode',
    component: 'Input',
    show: false,
  },
  {
    label: '实际表字段',
    field: 'fieldCode',
    component: 'Input',
    show: false,
  },
];

export const topColumns: BasicColumn[] = [
  {
    title: '标准名称',
    dataIndex: 'manageName',
    width: 220,
    align: 'center',
  },
  {
    title: '指标类型',
    dataIndex: 'indicatorType',
    width: 120,
    align: 'center',
    customRender: ({text}) => {
      switch (text) {
        case "1":
          return '实际值'
        case "2":
          return '偏差值'
        default:
          return '/'
      }
    },
  },
  {
    title: '红下',
    dataIndex: 'redDown',
    width: 120,
    align: 'center',
    customHeaderCell: function(){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
  },
  {
    title: '黄下',
    dataIndex: 'yellowDown',
    width: 120,
    align: 'center',
    customHeaderCell: function(){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
  },
  {
    title: '目标值',
    dataIndex: 'standardValue',
    width: 120,
    align: 'center',
    customHeaderCell: function(){
      return {
        style: {
          backgroundColor: getColor('green'),
        }
      };
    },
  },
  {
    title: '黄上',
    dataIndex: 'yellowUp',
    width: 120,
    align: 'center',
    customHeaderCell: function(){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
  },
  {
    title: '红上',
    dataIndex: 'redUp',
    width: 120,
    align: 'center',
    customHeaderCell: function(){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
  }
  // ,
  // {
  //   title: '实际值',
  //   dataIndex: 'actualValue',
  //   width: 120,
  //   align: 'center',
  // }
]

export const chartColumns: BasicColumn[] = [
  {
    title: '时间',
    dataIndex: 'time',
    width: 200,
    align: 'center',
  },
  {
    title: '值',
    dataIndex: 'value',
    width: 90,
    align: 'center',
    customCell(record) {
      //实际值
      const value = Number(record.value); 
      const colorObj = { bgColor: '' }
      //是否引入红黄区
      if(record.introduceStandard){
        const standarVo = record.standar;
        if (standarVo.length == 1) {
          const standar = standarVo[0];
          //根据标准值判断颜色
          //取红上和红下的限定值
          let redDown = standar.redDown;
          let redUp = standar.redUp;
          let yellowDown = standar.yellowDown;
          let yellowUp = standar.yellowUp;
          const standardValue = Number(standar.standardValue)
          switch(standar.indicatorType){
            case '1'://实际值
              redDown = Number(standar.redDown);
              redUp = Number(standar.redUp);
              yellowDown = Number(standar.yellowDown);
              yellowUp = Number(standar.yellowUp);
              break;
            case '2'://偏差值
              redDown = standardValue + Number(standar.redDown);
              redUp = standardValue + Number(standar.redUp);
              yellowDown = standardValue + Number(standar.yellowDown);
              yellowUp = standardValue + Number(standar.yellowUp);
              break;
          }
          if(value >= yellowDown && value <= yellowUp){
          }else
          if((value >= redDown && value < yellowDown) || (value > yellowUp && value <= redUp)){
            colorObj.bgColor = "yellow";
          }else{
            colorObj.bgColor = "red";
          }
        }
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
  }
]

//评价表单schema
export const evaluationFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '异常分析id',
    field: 'recordId',
    component: 'Input',
    show: false,
  },
  {
    label: '评价类型',
    field: 'evaluationType',
    component: 'Input',
    show: false,
  },
  {
    label: '评价内容',
    field: 'evaluationContent',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '评价得分',
    field: 'evaluationScore',
    component: 'InputNumber',
    colProps: { span: 12 },
    required: true,
  },
  {
    label: '评价结果',
    field: 'evaluationResult',
    component: 'Select',
    componentProps: {
      options: [
        { label: '合格', value: '1' },
        { label: '不合格', value: '0' },
      ]
    },
    colProps: { span: 12 },
    required: true,
  },
  {
    label: '评价人',
    field: 'evaluationUser',
    component: 'ApiSelect',
    componentProps:{
      api: mes3UserListAll,
      fieldNames: { label: 'realname', value: 'id' },
      getPopupContainer: (node) => getPopupContainer(),
      showSearch: true,
      filterOption: (inputValue, option) =>{
        return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
      }

    },
    defaultValue: userInfo?.id,
    colProps: { span: 12 },
    required: true,
  },
  {
    label: '评价日期',
    field: 'time',
    component: 'Input',
    componentProps: {
      disabled: true
    },
    defaultValue: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    colProps: { span: 12 },
  },
];