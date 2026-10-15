import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '租户ID',
    align:"center",
    dataIndex: 'tenantId'
   },
   {
    title: '工厂',
    align:"center",
    dataIndex: 'factoryCode'
   },
   {
    title: '部门',
    align:"center",
    dataIndex: 'deptCode'
   },
   {
    title: '产线',
    align:"center",
    dataIndex: 'lineCode'
   },
   {
    title: '工段',
    align:"center",
    dataIndex: 'section'
   },
   {
    title: '填报ID',
    align:"center",
    dataIndex: 'fillId'
   },
   {
    title: 'fillType',
    align:"center",
    dataIndex: 'fillType'
   },
   {
    title: '评审人',
    align:"center",
    dataIndex: 'assessor'
   },
   {
    title: '评审人岗位',
    align:"center",
    dataIndex: 'assessorPosition'
   },
   {
    title: '红线指标',
    align:"center",
    dataIndex: 'redType'
   },
   {
    title: '过程数据',
    align:"center",
    dataIndex: 'processData'
   },
   {
    title: '汇总值',
    align:"center",
    dataIndex: 'collectValue'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
   {
    title: '备注说明',
    align:"center",
    dataIndex: 'remark'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '租户ID',
    field: 'tenantId',
    component: 'InputNumber',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入租户ID!'},
          ];
     },
  },
  {
    label: '工厂',
    field: 'factoryCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入工厂!'},
          ];
     },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入部门!'},
          ];
     },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入产线!'},
          ];
     },
  },
  {
    label: '工段',
    field: 'section',
    component: 'Input',
  },
  {
    label: '填报ID',
    field: 'fillId',
    component: 'InputNumber',
  },
  {
    label: 'fillType',
    field: 'fillType',
    component: 'InputNumber',
  },
  {
    label: '评审人',
    field: 'assessor',
    component: 'Input',
  },
  {
    label: '评审人岗位',
    field: 'assessorPosition',
    component: 'Input',
  },
  {
    label: '红线指标',
    field: 'redType',
    component: 'InputNumber',
  },
  {
    label: '过程数据',
    field: 'processData',
    component: 'Input',
  },
  {
    label: '汇总值',
    field: 'collectValue',
    component: 'InputNumber',
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '备注说明',
    field: 'remark',
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

// 高级查询数据
export const superQuerySchema = {
  tenantId: {title: '租户ID',order: 0,view: 'number', type: 'number',},
  factoryCode: {title: '工厂',order: 1,view: 'text', type: 'string',},
  deptCode: {title: '部门',order: 2,view: 'text', type: 'string',},
  lineCode: {title: '产线',order: 3,view: 'text', type: 'string',},
  section: {title: '工段',order: 4,view: 'text', type: 'string',},
  fillId: {title: '填报ID',order: 5,view: 'number', type: 'number',},
  fillType: {title: 'fillType',order: 6,view: 'number', type: 'number',},
  assessor: {title: '评审人',order: 7,view: 'text', type: 'string',},
  assessorPosition: {title: '评审人岗位',order: 8,view: 'text', type: 'string',},
  redType: {title: '红线指标',order: 9,view: 'number', type: 'number',},
  processData: {title: '过程数据',order: 10,view: 'text', type: 'string',},
  collectValue: {title: '汇总值',order: 11,view: 'number', type: 'number',},
  sort: {title: '排序',order: 12,view: 'number', type: 'number',},
  remark: {title: '备注说明',order: 13,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}