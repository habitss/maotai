import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '产线',
    align:"center",
    dataIndex: 'areaName'
   },
   {
    title: '参数',
    align:"center",
    dataIndex: 'param',
    customRender:function ({ record }) {
      if (!record.param && record.param !== 0) {
        return '';
      }
      return record.param === 1 ? "工艺浓度" : "冷麦汁产量";
    },
   },
   {
    title: 'PI报表',
    align:"center",
    dataIndex: 'tableIdVal'
   },
   {
    title: '时间',
    align:"center",
    dataIndex: 'dateTimeName'
   },
   {
    title: '订单号',
    align:"center",
    dataIndex: 'orderName'
   },
   {
    title: '批次号',
    align:"center",
    dataIndex: 'batchName'
   },
   {
    title: '参数值',
    align:"center",
    dataIndex: 'paramValName'
   }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '参数',
    field: 'param',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '工艺浓度',
          value: 1,
        },
        {
          label: '冷麦汁产量',
          value: 2,
        },
      ],
    },
  }
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '产线',
    field: 'areaNo',
    component: 'JDictSelectTag',
     componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val",
        showChooseOption: false,
      }
    },
    required: true,
    rules: [
      { required: true, message: '产线不能为空', trigger: 'blur' }
    ]
  },
  {
    label: '参数',
    field: 'param',
    component: 'Select',
    dynamicDisabled: ({ model }) => {
      // id存在时为编辑模式，禁用选择；id不存在为新增模式，可编辑
      return !!model.id;
    },
    componentProps: {
      options: [
        {
          label: '工艺浓度',
          value: 1,
        },
        {
          label: '冷麦汁产量',
          value: 2,
        },
      ],
    },
    required: true,
    rules: [
      { required: true, message: '参数不能为空', trigger: 'blur' }
    ]
  },
  {
    label: 'PI报表',
    field: 'tableId',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = '0', name, id",
        showChooseOption: false,
        onChange: async () => {
          formModel.dateTime=null;
          formModel.orderNo=null;
          formModel.batchNo=null;
          formModel.paramVal=null;
        },
      }
    }
  },
  {
    label: '时间',
    field: 'dateTime',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, field_name",
        showChooseOption: false,
      }
    }
  },
  {
    label: '订单号',
    field: 'orderNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, field_name",
        showChooseOption: false,
      }
    }
  },
  {
    label: '批次号',
    field: 'batchNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, field_name",
        showChooseOption: false,
      }
    }
  },
  {
    label: '参数值',
    field:"paramVal",
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, field_name",
        showChooseOption: false,
      }
    }
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