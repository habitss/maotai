import dayjs from 'dayjs';
import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { allList } from '/@/views/dataCalculation/cpk/config/CpkConfig.api';
const defaultCode = ref([]);
export let codeList = ref([] as any);
(async function f() {
  codeList.value = await allList({nodeLevel: '1'});
  defaultCode.value = codeList.value[0].nodeCode;
})();
//列表数据
export const reportColumns: BasicColumn[] = [
  {
    title: '指标',
    align: 'center',
    dataIndex: 'tow_level_name',
    slots: { customRender: 'boldTextSlots' },
    customCell: (record, index) => {
      let rowSpan = 0;
      if (index == record.tow_level_index) {
        rowSpan = record.tow_level_merge_num;
      }
      return { rowSpan };
    },
    width: '200px',
  },
  {
    title: '物料/工序',
    align: 'center',
    dataIndex: 'three_level_name',
    slots: { customRender: 'boldTextSlots' },
    customCell: (record, index) => {
      let rowSpan = 0;
      if (index == record.three_level_index) {
        rowSpan = record.three_level_merge_num;
      }
      return { rowSpan };
    },
    width: '200px',
  },
  {
    title: '项目',
    align: 'center',
    dataIndex: 'five_level_name',
    slots: { customRender: 'fiveLevelNameSlots' },
    width: '200px',
  },
];
//查询数据
export const reportSearchFormSchema: FormSchema[] = [
  {
    label: "显示类型",
    field: "code",
    component: 'Select',
    componentProps: {
      showSearch: true,
      options: codeList,
      optionFilterProp: 'label',
      fieldNames: {
        label: 'nodeName',
        value: 'nodeCode',
      }
    },
    defaultValue: defaultCode,
  },
  {
    label: "年份",
    field: "year",
    component: 'DatePicker',
    componentProps: {
      picker: "year",
      valueFormat: "YYYY",
      format: "YYYY",
    },
    defaultValue: dayjs().format('YYYY'),
  },
  {
    label: "月份",
    field: "months",
    component: 'Select',
    componentProps: {
      options: [
        { label: '1月', value: '1' },
        { label: '2月', value: '2' },
        { label: '3月', value: '3' },
        { label: '4月', value: '4' },
        { label: '5月', value: '5' },
        { label: '6月', value: '6' },
        { label: '7月', value: '7' },
        { label: '8月', value: '8' },
        { label: '9月', value: '9' },
        { label: '10月', value: '10' },
        { label: '11月', value: '11' },
        { label: '12月', value: '12' },
      ],
      mode: 'multiple',
      placeholder: '请选择',
    },
    defaultValue: [dayjs().format('M')],
  },
];