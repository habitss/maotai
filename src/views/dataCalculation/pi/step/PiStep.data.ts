import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '部门',
    align:"center",
    dataIndex: 'deptCodeVal'
   },
   {
    title: '产线',
    align:"center",
    dataIndex: 'lineCodeVal'
   },
   {
    title: '设备',
    align:"center",
    dataIndex: 'deviceIdVal'
   },
   {
    title: '步骤号',
    align:"center",
    dataIndex: 'stepNum'
   },
   {
    title: '步骤名称',
    align:"center",
    dataIndex: 'stepName'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'dept',
    component: 'Input',
  },
  {
    label: '产线',
    field: 'lineId',
    component: 'Input',
  },
  {
    label: '设备',
    field: 'deviceId',
    component: 'InputNumber',
  },
  {
    label: '步骤号',
    field: 'stepNum',
    component: 'Input',
  },
  {
    label: '步骤名称',
    field: 'stepName',
    component: 'Input',
  },
  {
    label: '排序',
    field: 'sortNum',
    component: 'InputNumber',
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