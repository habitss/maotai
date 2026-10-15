import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { list as kpiList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
import { departListAll } from './BestPracticePromotion.api';

export const options = [
  { label: '适用', value: '1' },
  { label: '不适用', value: '0' },
  { label: '不选择', value: '' }
];

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '名称',
    align: "center",
    dataIndex: 'title',
    width: 100,
    fixed: 'left',
  },
  {
    title: '最佳实践推广清单',
    children:[
      {
        title: '编号',
        align: "center",
        dataIndex: 'serialNumber',
        width: 100,
      },
      {
        title: '年月',
        align: "center",
        dataIndex: 'yearMon',
        width: 90,
      },
      {
        title: '提报工厂',
        align: "center",
        dataIndex: 'reportFactory',
        width: 90,
      },
      {
        title: '提报部门',
        align: "center",
        dataIndex: 'reportWorkshop_dictText',
        width: 90,
      },
      {
        title: '改善类型',
        align: "center",
        dataIndex: 'reportType_dictText',
        width: 120,
      },
      {
        title: '对应KPI',
        align: "center",
        dataIndex: 'kpiCode_dictText',
        width: 120,
      },
      {
        title: '最佳实践评审得分',
        align: "center",
        dataIndex: 'reviewScore',
        ellipsis: false,
        width: 80,
      },
    ]
  },
  {
    title: '评审会意见',
    children:[
      {
        title: '专家评审意见',
        align: "center",
        dataIndex: 'reviewOpinion',
        ellipsis: false,
        width: 180,
      },
      {
        title: '评审结果(强制推广≥23分；建议推广≥18分；不推广＜18分)',
        align: "center",
        dataIndex: 'reviewResult',
        ellipsis: false,
        width: 140,
        slots: { customRender: 'reviewResult' },
      },
      {
        title: '发布时间',
        align: "center",
        dataIndex: 'reviewTime',
        width: 90,
      },
    ]
  },
  {
    title: '识别部门',
    children:[
    ]
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "年月",
    field: 'yearMon',
    component: 'Input',
    slot: 'yearMon'
  },
  {
    label: '改善类型',
    field: 'reportType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_best_practice_type where del_flag = 0, type_name, id",
      showChooseOption: false,
      mode: 'multiple',
    },
  },
  {
    label: '名称',
    field: 'title',
    component: 'JInput',
  },
  {
    label: '提报工厂',
    field: 'reportFactory',
    component: 'JInput',
  },
];

//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '最佳实践推广清单',
    field: 'divisionLine',
    component: 'Divider',
    colProps: { span: 24 },
    componentProps: {
      dashed: true,
      orientation: 'center',
      plain: true,
      type:'horizontal',
    },
  },
  {
    label: '',
    field: 'dataId',
    component: 'Input',
    show: false,
  },
  {
    label: '名称',
    field: 'title',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '编号',
    field: 'serialNumber',
    component: 'Input',
  },
  {
    label: '提报工厂',
    field: 'reportFactory',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '提报部门',
    field: 'reportWorkshop',
    component: 'Input',
    slot: 'deptCode',
  },
  {
    label: '改善类型',
    field: 'reportType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_best_practice_type, type_name, id",
      showChooseOption: false,
    },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '对应KPI',
    field: 'kpiCode',
    component: 'ApiTreeSelect',
    componentProps: {
      api: kpiList,
      fieldNames: {children:'children', label:'kpiName', value: 'id' },
      resultField:'records',
      showSearch: true,
      treeNodeFilterProp: 'kpiName',
      params:{pageSize: 9999},
    },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '最佳实践评审得分',
    field: 'reviewScore',
    component: 'InputNumber',
    componentProps: ({ formModel }) => {
      return {
        onChange:e=>{
          if(e < 18){
            formModel.reviewResult = '1';
          } else if(e >= 18 && e < 23){
            formModel.reviewResult = '2';
          } else {
            formModel.reviewResult = '3';
          }
        }
      }
    },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
    
  },
  {
    label: '评审会意见',
    field: 'reviewLine',
    component: 'Divider',
    colProps: { span: 24 },
    componentProps: {
      dashed: true,
      orientation: 'center',
      plain: true,
      type:'horizontal',
    },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '发布时间',
    field: 'reviewTime',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '评审结果',
    field: 'reviewResult',
    component: 'InputNumber',
    slot: 'reviewResult',
    colProps: { span: 16 },
  },
  {
    label: '评审会意见',
    field: 'reviewOpinion',
    component: 'InputTextArea',
    colProps: { span: 24 },
    dynamicDisabled: ({ values }) => {
      return values.dataId;
    },
  },
  {
    label: '识别部门',
    field: 'deptLine',
    component: 'Divider',
    colProps: { span: 24 },
    componentProps: {
      dashed: true,
      orientation: 'center',
      plain: true,
      type:'horizontal',
    },
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'yearMon',
    component: 'Input',
    show: false
  },
];

export async function getDepart(){
  //获取部门数据,并排序
  const res = await departListAll({});
  const deptArr = res
    .filter((item) => item.orgType == 4) 
    .sort((a, b) => a.departOrder - b.departOrder);
  return deptArr;
}