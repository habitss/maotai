import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '区域',
    align:"center",
    dataIndex: 'pcuName'
   },
   {
    title: '配方名称',
    align:"center",
    dataIndex: 'recipeName'
   },
   {
    title: '设备名称',
    align:"center",
    dataIndex: 'unitName'
   },
   {
    title: '步骤号',
    align:"center",
    dataIndex: 'eopNo'
   },
   {
    title: '步骤名称',
    align:"center",
    dataIndex: 'eopName'
   },
   {
    title: 'ropId',
    align:"center",
    dataIndex: 'ropId'
   },
   {
    title: '起始值',
    align:"center",
    dataIndex: 'initialValue'
   },
   {
    title: '结束值',
    align:"center",
    dataIndex: 'endValue'
   },
];
//查询数据
export const getSearchFormSchema = (
  onAreaChange: Function,
  onRecipeChange: Function,
  onUnitChange: Function,
) => [
  {
    label: '区域',
    field: 'pcuNo',
    component: 'Select' as const,
    componentProps: {
      options: [] as {value: string; label: string}[],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onAreaChange,
    },
    colProps: { span: 6 }
  },
  {
    label: '配方名称',
    field: 'recipeNo',
    component: 'Select',
    componentProps: {
      options: [],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onRecipeChange,
    },
    colProps: { span: 6 }
  },
  {
    label: '设备名称',
    field: 'unitNo',
    component: 'Select',
    componentProps: {
      options: [],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onUnitChange,
    },
    colProps: { span: 6 }
  },
  {
    label: '步骤号',
    field: 'eopNo',
    component: 'Select',
    componentProps: {
      options: [],
      showSearch: true,
      optionFilterProp: 'label',
    },
    colProps: { span: 6 }
  },
];
//表单数据
export const getFormSchema = (
  onAreaChange: Function,
  onRecipeChange: Function,
  onUnitChange: Function,
  onStepChange: Function,
) => [
  {
    label: '区域',
    field: 'pcuNo',
    component: 'Select' as const,
    required: true,
    rules: [
      { required: true, message: '区域不能为空', trigger: 'change' as const }
    ],
    componentProps: {
      options: [] as {value: string; label: string}[],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onAreaChange,
    },
    colProps: { span: 12 }
  },
  {
    label: '配方名称',
    field: 'recipeNo',
    component: 'Select',
    componentProps: {
      options: [],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onRecipeChange,
      disabled: true,
    },
    required: true,
    rules: [
      { required: true, message: '配方不能为空', trigger: 'blur' }
    ],
    colProps: { span: 12 }
  },
  {
    label: '设备名称',
    field: 'unitNo',
    component: 'Select',
    componentProps: {
      options: [],
      showSearch: true,
      optionFilterProp: 'label',
      onChange: onUnitChange,
      disabled: true,
    },
    required: true,
    rules: [
      { required: true, message: '设备不能为空', trigger: 'blur' }
    ],
    colProps: { span: 12 }
  },
  {
    label: '步骤名称',
    field: 'eopNo',
    component: 'Select',
    componentProps: {
      options: [],
      onChange: onStepChange,
      getPopupContainer: () => document.body,
      disabled: true,
    },
    required: true,
    rules: [
      { required: true, message: '步骤不能为空', trigger: 'blur' }
    ],
    colProps: { span: 12 }
  },
  {
    label: 'ropId',
    field: 'ropId',
    component: 'Input',
    required: true,
    rules: [
      { required: true, message: 'ropId不能为空', trigger: 'blur' }
    ],
    colProps: { span: 12 }
  },
  {
    label: '起始值',
    field: 'initialValue',
    component: 'Input',
    colProps: { span: 12 }
  },
  {
    label: '结束值',
    field: 'endValue',
    component: 'Input',
    colProps: { span: 12 }
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    show: false
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
  {
    label: '区域名称',
    field: 'pcuName',
    component: 'Input',
    show: false
  },
  {
    label: '设备名称',
    field: 'unitName',
    component: 'Input',
    show: false
  },
  {
    label: '步骤名称',
    field: 'eopName',
    component: 'Input',
    show: false
  },
  {
    label: '配方名称',
    field: 'recipeName',
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
  return getFormSchema(() => {}, () => {}, () => {}, () => {}) as FormSchema[];
}