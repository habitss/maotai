import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
const consistencyTypes: any[] = [
  { label: '一级指数', value: '1' },
  { label: '二级指数', value: '2' }
]

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '指数名称',
    align: "center",
    dataIndex: 'consistencyName',
    width: 200,
  },
  {
    title: '得分',
    align: "center",
    dataIndex: 'greenProportionConsistency',
    width: 80,
  },
  {
    title: '指数类型',
    align: "center",
    dataIndex: 'consistencyType',
    customRender: ({ record }) => {
      return record.consistencyType === '1' ? "一级指数" : "二级指数";
    },
    width: 200,
  },
  {
    title: '备注',
    align: "left",
    dataIndex: 'remark'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '一致性类型',
    field: 'consistencyType',
    component: 'Select',
    componentProps: {
      options: consistencyTypes
    },
  },
  {
    label: '开始时间',
    field: 'startTime',
    component: 'RangePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      valueType: 'Date',
      placeholder:['请选择开始时间','请选择结束时间'],
    },
    colProps: { span: 8 },
  },
];

//明细列表数据
export const mxColumns: BasicColumn[] = [
  {
    title: 'pi名称',
    align: "center",
    dataIndex: 'piName',
    width: 200,
  },
  {
    title: '得分',
    align: "center",
    dataIndex: 'greenProportionConsistency',
    width: 100,
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'unit',
    width: 100,
  },
  {
    title: '指数权重(%)',
    align: "center",
    dataIndex: 'weight',
    width: 100,
  },
  {
    title: '绿区数',
    align: "center",
    dataIndex: 'green',
    width: 100,
    customRender: ({ value, record }) => {
      if(record.piCode && !value){
        value = '/';
      }
      return value
    },
  },
  {
    title: '总批次',
    align: "center",
    dataIndex: 'totle',
    width: 100,
    customRender: ({ value, record }) => {
      if(record.piCode && !value){
        value = '/';
      }
      return value
    },
  },
  {
    title: '绿区率',
    align: "center",
    dataIndex: 'greenProportion',
    width: 100,
    customRender: ({ value, record }) => {
      if(record.piCode && !value){
        value = '/';
      }
      return value
    },
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark'
  },
];