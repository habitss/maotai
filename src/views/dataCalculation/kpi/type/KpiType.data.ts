import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { loadTreeData,list } from './KpiType.api';
import dayjs from 'dayjs';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '名称',
    align:"left",
    dataIndex: 'kpiType'
   },
  //  {
  //   title: '年份',
  //   align:"center",
  //   dataIndex: 'year'
  //  },
   {
    title: '序号',
    align:"center",
    dataIndex: 'sort'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'text'
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
    label: "KPI分类",
    field: "id",
    component: 'ApiTreeSelect',
    componentProps: {
      api: list,
      params: { async: false, parentId: '' },
      fieldNames: { label: 'kpiType', value: 'id' },
      showSearch: true,
      treeNodeFilterProp: 'kpiType',
    },
  },
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
    label: '名称',
    field: 'kpiType',
    component: 'Input',
    required: true,
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '备注',
    field: 'text',
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
    field: 'parentId',
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