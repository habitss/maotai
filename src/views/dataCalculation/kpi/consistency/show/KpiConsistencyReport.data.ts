import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import dayjs from 'dayjs';

// 获取上周末（上周日）日期
function getLastSunday() {
  const today = dayjs();
  const lastSunday = today.day(0).isBefore(today, 'day') ? today.day(0) : today.day(-7);
  return lastSunday;
}
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
  //  {
  //   title: '单位',
  //   align:"center",
  //   dataIndex: 'unit',
  //   width: 80
  //  },
  //  {
  //   title: '权重',
  //   align:"center",
  //   dataIndex: 'weight',
  //   width: 80
  //  },

   {
    title: '绿区批次',
    align:"center",
    dataIndex: 'greenBatchCount',
    width: 80,
    customRender: ({ record, text }) => record.level > 1 ? text : '',
    customCell: (record) => ({ class: record.level <= 1 ? 'cell-muted' : '' }),
   },
  {
    title: '总批次',
    align:"center",
    dataIndex: 'totalBatchCount',
    width: 80,
    customRender: ({ record, text }) => record.level > 1 ? text : '',
    customCell: (record) => ({ class: record.level <= 1 ? 'cell-muted' : '' }),
  },
  {
    title: '合格率',
    align:"center",
    dataIndex: 'passRate',
    width: 80,
    customRender: ({ text }) => text != null ? `${text}%` : '',
  },
  {
    title: '重复次数',
    align:"center",
    dataIndex: 'repeatCount',
    width: 80,
    customRender: ({ record, text }) => record.level > 1 ? text : '',
    customCell: (record) => ({ class: record.level <= 1 ? 'cell-muted' : '' }),
  },
  {
    title: '重复率',
    align:"center",
    dataIndex: 'repeatRate',
    width: 80,
    customRender: ({ record, text }) => record.level > 1 && text != null ? `${text}%` : '',
    customCell: (record) => ({ class: record.level <= 1 ? 'cell-muted' : '' }),
  },
  {
    title: '月年累',
    align:"center",
    dataIndex: 'monthYearAccumulate',
    width: 80,
    customRender: ({ record, text }) => record.level <= 1 ? text : '',
    customCell: (record) => ({ class: record.level > 1 ? 'cell-muted' : '' }),
  },
  {
    title: '年累',
    align:"center",
    dataIndex: 'monthAccumulate',
    width: 80,
    customRender: ({ record, text }) => record.level <= 1 ? text : '',
    customCell: (record) => ({ class: record.level > 1 ? 'cell-muted' : '' }),
  }

  //  {
  //   title: '批次配置-PI',
  //   align:"center",
  //   dataIndex: 'batchConfig',
  //   slots: { customRender: 'batchConfigSlot' }
  //  },
  //  {
  //   title: '合格率配置-KPI',
  //   align:"center",
  //   dataIndex: 'passRateConfig',
  //   slots: { customRender: 'passRateConfigSlot' }
  //  },
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
    label: '周日期',
    field: 'weekDate',
    component: 'DatePicker',
    defaultValue: getLastSunday(),
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      disabledDate: (current: any) => {
        return current && current.day() !== 0;
      },
    },
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