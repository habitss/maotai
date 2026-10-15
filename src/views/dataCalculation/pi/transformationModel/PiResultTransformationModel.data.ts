import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '模型名称',
    align: "center",
    dataIndex: 'modelName'
  },
  {
    title: '结果输入类型',
    align: "center",
    dataIndex: 'resultTypeVal'
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort'
  }
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '模型名称',
    field: 'modelName',
    component: 'Input',
  },
  {
    label: '结果输入类型',
    field: 'resultType',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_RESULT_TYPE', item_key, item_val",
        showChooseOption: false,
      }
    }
  },
];


//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '模型名称',
    field: 'modelName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [{ required: true, message: '请输入模型名称!'},];
     },
  },
  {
    label: '结果输入类型',
    field: 'resultType',
    component: 'JDictSelectTag',
      componentProps: ({formModel}) => {
      // 判断是否是编辑模式（可以通过判断是否有id值来确定）
      const isEdit = !!formModel?.id;
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_RESULT_TYPE', item_key, item_val",
        showChooseOption: false,
        stringToNumber: true,
        disabled: isEdit, // 编辑时禁用，新增时可用
        placeholder: isEdit ? '编辑模式下不可修改' : '请选择结果输入类型'
        
      }
    },
    required: true,
    rules: [
      { required: true, message: '区域不能为空', trigger: 'blur' }
    ]
  },
  {
    label: '排序',
    field: 'sort',
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


export const detailFormSchema: FormSchema[] = [
  {
    label: '模型名称',
    field: 'modelName',
    component: 'Input',
    componentProps:{disabled: true},
    colProps: { span: 12 },
    required: true,
  },
  {
    label: '结果输入类型',
    field: 'resultType',
    component: 'JDictSelectTag',
      componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_RESULT_TYPE', item_key, item_val",
        showChooseOption: false,
        stringToNumber: true,
        disabled: true,
      }
    },
    required: true,
    colProps: { span: 10 },
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];