import { FormSchema } from '/@/components/Table';
import { getYearArr, getMonthArr } from '/@/utils/common/YMUtil';
import dayjs from 'dayjs';

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'depart',
        onChange: () => {
          formModel.lineCode = null;
        }
      }
    },
    colProps: { span: 6 },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
        mode: "multiple",
      }
    },
    colProps: { span: 6 },
  },
  {
    label: '年',
    field: 'year',
    component: 'Select',
    componentProps: {
      options: getYearArr(10, 5),
      allowClear: false
    },
    defaultValue: new Date().getFullYear(),
  },
  {
    label: '月',
    field: 'months',
    component: 'Select',
    componentProps: {
      options: getMonthArr(false),
      mode: "multiple",
      maxTagCount: 1,
    },
    defaultValue: dayjs().month() + 1,
  },
  {
    label: 'PI名称',
    field: 'piName',
    component: 'Input',
    colProps: { span: 6 },
  },
];