import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: 'KPI编码',
    align:"center",
    dataIndex: 'kpiCode',
    width: 80
   },
   {
    title: 'KPI名称',
    align:"center",
    dataIndex: 'kpiName',
    width: 100
   },
   {
    title: 'PI编码',
    align:"center",
    dataIndex: 'piCode',
    width: 80
   },
   {
    title: 'PI名称',
    align:"left",
    dataIndex: 'piName',
    width: 250
   },
   {
    title: '单位',
    align:"center",
    dataIndex: 'unit',
    width: 80
   },
  //  {
  //   title: '权重',
  //   align:"center",
  //   dataIndex: 'weight',
  //   width: 80
  //  },
   {
    title: '批次配置-PI',
    align:"center",
    dataIndex: 'batchConfig',
    slots: { customRender: 'batchConfigSlot' }
   },
   {
    title: '合格率配置-KPI',
    align:"center",
    dataIndex: 'passRateConfig',
    slots: { customRender: 'passRateConfigSlot' }
   },
  //  {
  //   title: '重复次数配置-PI',
  //   align:"center",
  //   dataIndex: 'repeatCountConfig',
  //   slots: { customRender: 'repeatCountConfigSlot' }
  //  },
  //  {
  //   title: '重复率配置-PI',
  //   align:"center",
  //   dataIndex: 'repeatRateConfig',
  //   slots: { customRender: 'repeatRateConfigSlot' }
  //  },
  //  {
  //   title: '是否需要配置 1是 0否',
  //   align:"center",
  //   dataIndex: 'needConfig'
  //  },
  //  {
  //   title: '排序',
  //   align:"center",
  //   dataIndex: 'sort'
  //  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: 'KPI名称',
    field: 'kpiName',
    component: 'Input',
    colProps: {span: 6},
  },
  {
    label: 'PI名称',
    field: 'piName',
    component: 'Input',
    colProps: {span: 6},
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'KPI名称',
    field: 'kpiCode',
    component: 'Input',
    slot: 'kpiCodeSlot',
    dynamicRules: ({model,schema}) => {
      return [
              { required: true, message: '请选择KPI!'},
      ];
    },
  }
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}