import { ref } from 'vue';
import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';

const customizeOptions = ref([] as any);
//列表数据
export const columns: BasicColumn[] = [
  //  {
  //   title: '曲线类型',
  //   align:"center",
  //   dataIndex: 'curveType',
  //   customRender:function ({ record }) {
  //     return record.curveType === 1 ? "单批次曲线" : "多批次曲线";
  //   },
  //  },
   {
    title: '区域',
    align:"center",
    dataIndex: 'areaName'
   },
   {
    title: '设备',
    align:"center",
    dataIndex: 'deviceNoVal'
   },
   {
    title: 'PI报表',
    align:"center",
    dataIndex: 'tableIdVal',
    width:300,
    customRender:function ({ record }) {
      // return `${record.tableIdVal}(${record.tableName})`;
      return `${record.tableIdVal}`;
    },
    
   },
   {
    title: '触发时间',
    align:"center",
    dataIndex: 'eventTimeVal'
   },
   {
    title: '开始时间',
    align:"center",
    dataIndex: 'startTimeVal'
   },
   {
    title: '结束时间',
    align:"center",
    dataIndex: 'endTimeVal'
   },
   {
    title: '设备编码',
    align:"center",
    dataIndex: 'unitNoVal'
   },
   {
    title: '设备名称',
    align:"center",
    dataIndex: 'unitNameVal'
   },
   {
    title: '配方名称',
    align:"center",
    dataIndex: 'recipeVal'
   },
   {
    title: '配方号',
    align:"center",
    dataIndex: 'recipeNoVal'
   },
   {
    title: '订单号',
    align:"center",
    dataIndex: 'orderNoVal'
   },
   {
    title: '批次',
    align:"center",
    dataIndex: 'batchNoVal'
   },
   {
    title: '步骤名称',
    align:"center",
    dataIndex: 'eopNameVal'
   },
   {
    title: '步骤号',
    align:"center",
    dataIndex: 'eopNoVal'
   },
   {
    title: '配方类型',
    align:"center",
    dataIndex: 'recipeTypeVal'
   }
  //  {
  //   title: '排序',
  //   align:"center",
  //   dataIndex: 'sort'
  //  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
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
    }
  },
  {
    label: '设备',
    field: 'deviceNo',
    component: 'JDictSelectTag',
    componentProps: () => {
      return {
        dictCode: "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_DEVICE', item_key, item_val",
        showChooseOption: false,
        showSearch: true,
      }
    }
  },
  {
    label: '报表名称',
    field: 'tableName',
    component: 'JDictSelectTag',
    componentProps: () => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = '0', name, id",
        showChooseOption: false,
        showSearch: true,
      }
    }
  }
];
//表单数据
export const getSearchFormSchema = (
    onAreaChange: Function,
) => [
  // {
  //   label: '曲线类型',
  //   field: 'curveType',
  //   component: 'Select',
  //   componentProps: {
  //     options: [
  //       {
  //         label: '单批次曲线',
  //         value: 1,
  //       },
  //       {
  //         label: '多批次曲线',
  //         value: 2,
  //       },
  //     ],
  //   },
  //   dynamicDisabled: true,  // 设置为只读
  // },
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
    }
  },
  {
    label: '设备',
    field: 'deviceNo',
    component: 'Select',
    componentProps: {
      options: [], // 初始为空，根据数据源动态加载
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label?.toLowerCase().includes(input.toLowerCase());
      },
    }
  },
  {
    label: '报表名称',
    field: 'tableId',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = '0', name, id",
        showChooseOption: false,
        showSearch: true,
        onChange: async () => {
          formModel.eventTime=null
          formModel.startTime=null
          formModel.endTime=null
          formModel.unitNo=null
          formModel.unitName=null
          formModel.recipe=null
          formModel.orderNo=null
          formModel.batchNo=null
          formModel.eopName=null

          formModel.eventTimePi=null
          formModel.startTimePi=null
          formModel.endTimePi=null
          formModel.unitNoPi=null
          formModel.unitNamePi=null
          formModel.recipePi=null
          formModel.orderNoPi=null
          formModel.batchNoPi=null
          formModel.eopNamePi=null

        },

      }
    }
  },
  {
    label: '时间(触发时间)',
    field: 'eventTime',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '开始时间',
    field: 'startTime',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '结束时间',
    field: 'endTime',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '设备编码',
    field: 'unitNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '设备名称',
    field: 'unitName',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '配方',
    field: 'recipe',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '配方号',
    field: 'recipeNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
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
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '批次',
    field: 'batchNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '步骤名称',
    field: 'eopName',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '步骤号',
    field: 'eopNo',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    }
  },
  {
    label: '配方类型',
    field: 'recipeType',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, id",
        showChooseOption: false,
      }
    },
    // 使用函数动态控制显示
    show: ({ values }) => {
      // values 是当前表单的所有值
      return values?.curveType == '2';
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
