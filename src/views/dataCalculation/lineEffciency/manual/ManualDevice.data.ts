import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
let dictComponentProps = {
  type: 'select',
  labelField: 'item_key',
  valueField: 'item_val',
  showChooseOption: false,
}

//列表数据
export const columns: BasicColumn[] = [
  //MES3_MANUAL_DEVICE_REGION	
   {
    title: '区域',
    align:"center",
    dataIndex: 'regionVal'
   },
   {
    title: '设备编号',
    align:"center",
    dataIndex: 'deviceCode'
   },
   {
    title: '设备名称',
    align:"center",
    dataIndex: 'deviceName'
   },
   {
    title: '是否启用',
    align:"center",
    dataIndex: 'isEnabled',
    customRender:function ({ record }) {
      return record.isEnabled === 1 ? "启用" : "禁用";
    },
   }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '区域',
    field: 'region',
    component: 'JDictSelectTag',
    componentProps: {
      ...dictComponentProps, 
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION', item_key, item_val",
    }
  },
  {
    label: '设备编号',
    field: 'deviceCode',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '设备名称',
    field: 'deviceName',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '是否启用',
    field: 'isEnabled',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '启用',
          value: '1',
        },
        {
          label: '禁用',
          value: '0',
        },
      ],
    },
    colProps: { span: 6 },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  
  {
    label: '区域',
    field: 'region',
    component: 'JDictSelectTag',
    componentProps: {
      ...dictComponentProps, 
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION', item_key, item_val",
    },
    required: true,
  },
  {
    label: '设备编号',
    field: 'deviceCode',
    component: 'Input',
    required: true,
  },
  {
    label: '设备名称',
    field: 'deviceName',
    component: 'Input',
    required: true,
  },
  {
    label: '是否启用',
    field: 'isEnabled',
    component: 'Switch',
    defaultValue: 1, // 默认启用
    componentProps: {
      checkedChildren: '启用',
      unCheckedChildren: '禁用',
      checkedValue: 1,
      unCheckedValue: 0,
    },
    colProps: {
      span: 12,
    },
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