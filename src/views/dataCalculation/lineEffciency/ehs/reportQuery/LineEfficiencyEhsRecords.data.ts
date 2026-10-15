import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '监测时间',
    align:"center",
    dataIndex: 'monitorDateTime',
    width: 200
   },
   {
    title: '采集值',
    align:"center",
    dataIndex: 'monitorValue',
    width: 100
   },
   {
    title: '⼯⼚代码',
    align:"center",
    dataIndex: 'orgCode'
   },
   {
    title: '点位代码',
    align:"center",
    dataIndex: 'paramCode'
   },
   {
    title: '点位名称',
    align:"center",
    dataIndex: 'paramName'
   },
   {
    title: '发送时间',
    align:"center",
    dataIndex: 'sendTime',
    width: 200
   },
   {
    title: '发送状态',
    align:"center",
    dataIndex: 'sendStatus'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '监测时间',
    field: 'monitorDateTime',
    component: 'RangePicker',
    componentProps: {
        showTime: true,
        format:'YYYY-MM-DD HH:mm:ss',
        valueType: 'Date',
        placeholder:['请选择检测开始时间','请选择检测接收时间']
    },
    colProps: { span: 10 }
  },
  {
    label: '点位名称',
    field: 'paramName',
    component: 'Input',
     colProps: { span: 5 }
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '监测时间',
    field: 'monitorDateTime',
    component: 'Input',
  },
  {
    label: '采集值',
    field: 'monitorValue',
    component: 'Input',
  },
  {
    label: '⼯⼚代码',
    field: 'orgCode',
    component: 'Input',
  },
  {
    label: '点位代码',
    field: 'paramCode',
    component: 'Input',
  },
  {
    label: '点位名称',
    field: 'paramName',
    component: 'Input',
  },
  {
    label: '处理消息',
    field: 'processMsg',
    component: 'Input',
  },
  {
    label: '处理状态',
    field: 'processStatus',
    component: 'Input',
  },
  {
    label: '采集时间',
    field: 'scadaInsertTime',
    component: 'Input',
  },
  {
    label: '状态',
    field: 'status',
    component: 'Input',
  },
  {
    label: '发送时间',
    field: 'sendTime',
    component: 'Input',
  },
  {
    label: '发送状态',
    field: 'sendStatus',
    component: 'Input',
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}