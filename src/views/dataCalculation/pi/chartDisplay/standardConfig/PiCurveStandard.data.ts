import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { reactive } from 'vue';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';

// 配方列表
export const recipeColumns: BasicColumn[] = [
  {
    title: '区域',
    align:"center",
    dataIndex: 'areaName',
    width: 80
  },
  {
    title: '配方号',
    align:"center",
    dataIndex: 'recipeNo',
    width: 100
  },
  {
    title: '配方名称',
    align:"left",
    dataIndex: 'recipeName'
  }
];
//配方表单
export const getRecipeFormSchema = (
    onAreaChange: Function,
    onRecipeChange: Function,
) => [
  {
    label: '区域',
    field: 'areaNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val",
        showChooseOption: false,
        onChange: (value) => onAreaChange(value), //区域变化触发设备过滤
      }
    },
  },
  {
    label: '配方名称',
    field: 'recipeNo',
    component: 'Select', // 改为 Select 组件
    componentProps: {
      options: [], // 初始为空，根据数据源动态加载
      showSearch: true, // 启用搜索功能
      optionFilterProp: 'label', // 按label进行搜索（显示文本）
      onChange: (value) => onRecipeChange(value),
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '数据来源',
    field: 'dataSource',
    component: 'Input',
    show: false,
  },
  {
    label: '配方名称',
    field: 'recipeName',
    component: 'Input',
    show: false,
  },
];


//列表数据
export const columns: BasicColumn[] = [
   {
    title: '设备名称',
    align:"center",
    dataIndex: 'unitName',
    fixed: 'left',  // 固定到左侧
    // customCell: (record, index) => {
      //合并
      // 获取所有数据
      //const data = tableData.value;
      // 如果是第一行
      // if (index === 0) {
      //   return { rowSpan: data.length };
      // }
      // 其他行
      // return { rowSpan: 0 };
    // },
   },
   {
    title: '步骤名称',
    align:"center",
    dataIndex: 'eopName',
    fixed: 'left',  // 固定到左侧
   },
   {
    title: '步骤号',
    align:"center",
    dataIndex: 'eopNo'
   },
   {
    title: '标准起始值',
    align:"center",
    dataIndex: 'initialValue'
   },
   {
    title: '标准结束值',
    align:"center",
    dataIndex: 'endValue'
   },
   {
    title: '标准时长类型',
    align:"center",
    dataIndex: 'durationType',
    customRender: ({ text }) => {
      if (text === 1) return '分钟';
      if (text === 2) return '小时';
      return text;
    }
   },
   {
    title: '速率',
    align:"center",
    dataIndex: 'rate'
   },
   {
    title: '标准时长',
    align:"center",
    dataIndex: 'duration'
   },
   {
    title: '参数名称',
    align:"center",
    dataIndex: 'param'
   },
   {
    title: '单位',
    align:"center",
    dataIndex: 'unit'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
   {
    title: '分组',
    align:"center",
    dataIndex: 'groupName'
   },
];
//查询数据
export const getSearchFormSchema = (
    onAreaChange: Function,
) => [
  {
    label: '区域',
    field: 'areaNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val",
        showChooseOption: false,
        //onChange: (value) => onAreaChange(value), //区域变化触发设备过滤
      }
    },
    colProps: { span: 6 }
  },
  {
    label: '配方号',
    field: 'recipeNo',
    component: 'Input',
    colProps: { span: 6 }
  },
  {
    label: '配方名称',
    field: 'recipeName',
    component: 'Input', // 改为 Select 组件
    // componentProps: {
    //   options: [], // 初始为空，根据数据源动态加载
    // },
    colProps: { span: 6 }
  }
];

export const searchFormSchema = [
  {
    label: '步骤名称',
    field: 'eopName',
    component: 'Input',
    colProps: { span: 6 }
  }
];

//表单数据
//export const formSchema: FormSchema[] = [
export const getFormSchema = (
      onDataSourceChange: Function,
      onAreaChange: Function,
      onUnitChange: Function,
      onRecipeChange: Function,
      onStepChange: Function,
      onRateChange: Function,
      onDurationChange: Function,
) => [
    // {
    //   label: '数据源',
    //   field: 'dataSource',
    //   component: 'Select',
    //   componentProps: {
    //     options: [
    //       { label: 'adapter', value: 1 },
    //       { label: 'ra', value: 2 },
    //     ],
    //     onChange: onDataSourceChange,
    //   },
    //   required: true,
    //   rules: [
    //     { required: true, message: '数据源不能为空', trigger: 'blur' }
    //   ],
    //   colProps: { span: 12 }
    // },
    {
      label: '区域',
      field: 'areaNo',
      component: 'JDictSelectTag',
        componentProps: ({formModel}) => {
        return {
          dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val",
          showChooseOption: false,
          onChange: onAreaChange, //区域变化触发设备过滤
          disabled: true,
        }
      },
      colProps: { span: 12 }
    },
    {
      label: '设备名称',
      field: 'unitNo',
      component: 'Select', // 改为 Select 组件
      componentProps: {
        options: [], // 初始为空，根据数据源动态加载
        onChange: onUnitChange, // 设备变化触发配方过滤
        mode: 'multiple', // 添加多选模式
        maxTagTextLength: 10, // 限制标签文字长度，防止被撑开
      },
      required: true,
      rules: [
        { required: true, message: '设备不能为空', trigger: 'blur' }
      ],
      colProps: { span: 12 }
    },
    // {
    //   label: '设备名称',
    //   field: 'unitName',
    //   component: 'Input',
    //   colProps: { span: 12 }
    // },
    // {
    //   label: '配方号',
    //   field: 'recipeNo',
    //   component: 'Select',
    //   componentProps: {
    //     options: [],
    //     onChange: onRecipeChange,
    //   },
    //   required: true,
    //   rules: [
    //     { required: true, message: '配方名称不能为空', trigger: 'blur' }
    //   ],
    //   colProps: { span: 12 }
    // },
    // {
    //   label: '配方名称',
    //   field: 'recipeName',
    //   component: 'Input',
    //   colProps: { span: 12 }
    // },
    {
      label: '步骤名称',
      field: 'eopNo',
      component: 'Select',
      componentProps: {
        options: [],
        onChange: onStepChange,
        getPopupContainer: () => document.body,
      },
      required: true,
      rules: [
        { required: true, message: '步骤名称不能为空', trigger: 'blur' }
      ],
      colProps: { span: 12 }
    },
    // {
    //   label: '步骤名称',
    //   field: 'eopName',
    //   component: 'Input',
    //   colProps: { span: 12 }
    // },
    {
      label: 'ROP编号',
      field: 'ropId',
      component: 'Input',
      // 使用函数动态控制显示
      show: ({ values }) => {
        // values 是当前表单的所有值
        return values?.dataSource == '1';
      },
      colProps: { span: 12 }
    },
    {
      label: '参数名称',
      field: 'param',
      component: 'Input',
      // required: true,
      // rules: [
      //   { required: true, message: '参数名称不能为空', trigger: 'blur' }
      // ],
      colProps: { span: 12 }
    },
    {
      label: '标准起始值',
      field: 'initialValue',
      component: 'Input',
      colProps: { span: 12 }
    },
    {
      label: '标准结束值',
      field: 'endValue',
      component: 'Input',
      colProps: { span: 12 }
    },
    {
      label: '标准时长类型',
      field: 'durationType',
      component: 'Select',
      componentProps: {
        options: [
          { label: '分钟', value: 1 },
          { label: '小时', value: 2 }
        ]
      },
      required: true,
      rules: [
        { required: true, message: '标准时长类型不能为空', trigger: 'blur' }
      ],
      colProps: { span: 12 }
    },
    {
      label: '速率',
      field: 'rate',
      component: 'Input',
      componentProps: {
        onChange: onRateChange,
      },
      dynamicDisabled: ({ values }) => !values?.initialValue || !values?.endValue || !values?.durationType,
      colProps: { span: 12 }
    },
    {
      label: '标准时长',
      field: 'duration',
      component: 'Input',
      componentProps: {
        onChange: onDurationChange,
      },
      dynamicDisabled: ({ values }) => !values?.initialValue || !values?.endValue || !values?.durationType,
      dynamicRules: ({ values }) => {
        if (values?.initialValue && values?.endValue) {
          return [{ required: true, message: '标准起始值和标准结束值不为空时，标准时长必填', trigger: 'blur' }];
        }
        return [];
      },
      colProps: { span: 12 }
    },
    
    // {
    //   label: '关联点位',
    //   field: 'pointCode',
    //   component: 'JDictSelectTag',
    //     componentProps: ({formModel}) => {
    //     return {
    //       dictCode: "mes3_pi_base_point where del_flag = 0 and data_attribute = 0, point_name, point_code",
    //       showChooseOption: false
    //     }
    //   },
    //   required: true,
    //   rules: [
    //     { required: true, message: '区域不能为空', trigger: 'blur' }
    //   ]
    // },

    {
      label: '单位',
      field: 'unit',
      component: 'Input',
      colProps: { span: 12 }
    },
    {
      label: '排序',
      field: 'sort',
      component: 'InputNumber',
      required: true,
      rules: [
        { required: true, message: '排序不能为空', trigger: 'blur' }
      ],
      colProps: { span: 12 }
    },
    {
      label: '分组',
      field: 'groupName',
      component: 'Input',
      helpMessage: '只有连续且分组名称相同的曲线色块才能合并成一体',
      colProps: { span: 12 }
    },
    // TODO 主键隐藏字段，目前写死为ID
    {
      label: '',
      field: 'id',
      component: 'Input',
      show: false
    },
    {
      label: '数据来源',
      field: 'dataSource',
      component: 'Input',
      show: false
    },
    {
      label: '配方号',
      field: 'recipeNo',
      component: 'Input',
      show: false
    },
    {
      label: '配方名称',
      field: 'recipeName',
      component: 'Input',
      show: false
    },
    {
      label: '配方id',
      field: 'recipeId',
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
      label: '分组id',
      field: 'groupId',
      component: 'Input',
      show: false
    }, 
    {
      label: '设备名称',
      field: 'unitName',
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


//设备分组表单
export const groupFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '分组名称',
    field: 'groupName',
    required: true,
    component: 'Input',
  },
  {
    label: '',
    field: 'recipeId',
    component: 'Input',
    show: false,
  },
];
