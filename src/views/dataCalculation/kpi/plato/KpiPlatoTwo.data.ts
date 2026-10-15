import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import dayjs from 'dayjs';
//列表数据
export const columns: BasicColumn[] = [
  
   {
    title: '控制点名称',
    align:"center",
    dataIndex: 'controllerName'
   },
   {
    title: '流程名称',
    align:"center",
    dataIndex: 'parentCodeVal'
   },
   {
    title: 'KPI名称',
    align:"center",
    dataIndex: 'superCodeVal'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remarks'
   }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  // {
  //   label: "年份",
  //   field: "year",
  //   component: 'DatePicker',
  //   componentProps: {
  //     picker: "year",
  //     valueFormat: "YYYY",
  //     format: "YYYY"
  //   },
  //   defaultValue: dayjs().format('YYYY'),
  // },
  {
    label: '控制点名称',
    field: 'controllerName',
    component: 'Input',
    colProps: { span: 6 },
  }
];
//表单数据
export const formSchema: FormSchema[] = [
  // {
  //   label: "年份",
  //   field: "year",
  //   component: 'Input',
  //   slot: 'platoOneYear',
  //   required: true,
  // },
  {
    label: '控制点名称',
    field: 'controllerName',
    component: 'Input',
    required: true,
  },
  {
    label: '生产流程名称',
    field: 'parentCode',
    component: 'Input',
    slot: 'platoOneSelect',
    required: true,
  },
  {
    label: '备注',
    field: 'remarks',
    component: 'Input',
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
	  field: 'superCode',
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