import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
export const checkType = ref([1, 2, 3, 4] as any);
// 完成情况确认下拉框数据
const finishVerifyStateOptions = [
  { label: '待确认', value: '1', },
  { label: '合格', value: '2', },
  { label: '不合格', value: '3', },
];

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '',
    align: "left",
    dataIndex: 'actionTitle',
    slots: { customRender: 'actionTitle' },
    ellipsis: false,
  },
  {
    title: '问题分类',
    align: "center",
    dataIndex: 'problemTypeId_dictText',
    width: 100,
    ellipsis: false,
  },
  {
    title: '完成情况',
    align: "center",
    dataIndex: 'finishState_dictText',
    width: 80,
    customCell(record) {
      let colorObj = { bgColor: '' }
      if (record.finishState == 1) {
        colorObj.bgColor = '#00FF00'
      } else if (record.finishState == 2 || record.finishState == 4) {
        colorObj.bgColor = '#FE0000'
      } else if (record.finishState == 0) {
        colorObj.bgColor = 'yellow'
      } else if (record.finishState == 3) {
        colorObj.bgColor = '#666666'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
  },
  {
    title: '完成情况确认',
    align: "center",
    dataIndex: 'finishVerifyState',
    width: 100,
    ellipsis: false,
    customRender: function({text}){
      return finishVerifyStateOptions.find(item => item.value == text)?.label;
    },
    customCell(record) {
      let colorObj = { bgColor: '' }
      if (record.finishVerifyState === '1') {
        colorObj.bgColor = 'yellow'
      } else if (record.finishVerifyState === '2') {
        colorObj.bgColor = '#00FF00'
      } else if (record.finishVerifyState === '3') {
        colorObj.bgColor = '#FE0000'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor,
        }
      }
    },
  },
  {
    title: '计划完成日期',
    align: "center",
    dataIndex: 'planFinishDate',
    width: 100,
    ellipsis: false,
  },
  {
    title: '实际完成日期',
    align: "center",
    dataIndex: 'practicalFinishDate',
    width: 100,
    ellipsis: false,
  },
  {
    title: '剩余天数',
    align: "center",
    dataIndex: 'surplusDay',
    width: 50,
    ellipsis: false,
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '计划完成日期',
    field: 'planFinishDate',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
  },
  {
    label: '完成情况',
    field: 'finishState',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      mode: 'multiple',
      maxTagCount: 2
    },
    defaultValue: ['0', '2'],
    colProps: { span: 6 },
  },
  {
    label: '剩余天数',
    field: 'surplusDay',
    component: 'InputNumber',
  },
  {
    label: '',
    field: 'checkType',
    component: 'CheckboxGroup',
    componentProps: {
      name:'checkType',
      options: [
        { label: '提出单', value: 1 },
        { label: '责任单', value: 2 },
        { label: '执行单', value: 3 },
        { label: '确认单', value: 4 },
      ],
      onChange: function(values){
        checkType.value = values;
      }
    },
    defaultValue: checkType.value,
    colProps: { span: 6 },
  }
];