import { FormSchema } from '/@/components/Table';
import { BasicColumn } from '/@/components/Table';
import { getYearArr } from '/@/utils/common/YMUtil';

export const columns: BasicColumn[] = [
  {
    title: '报表名称',
    align: "center",
    dataIndex: 'tableName',
    resizable: true,
    width: 240,
  },
  {
    title: '01月',
    align: "center",
    dataIndex: 'mon1',
    resizable: true,
    width: 80,
  },
  {
    title: '02月',
    align: "center",
    dataIndex: 'mon2',
    resizable: true,
    width: 80,
  },
  {
    title: '03月',
    align: "center",
    dataIndex: 'mon3',
    resizable: true,
    width: 80,
  },
  {
    title: '04月',
    align: "center",
    dataIndex: 'mon4',
    resizable: true,
    width: 80,
  },
  {
    title: '05月',
    align: "center",
    dataIndex: 'mon5',
    resizable: true,
    width: 80,
  },
  {
    title: '06月',
    align: "center",
    dataIndex: 'mon6',
    resizable: true,
    width: 80,
  },
  {
    title: '07月',
    align: "center",
    dataIndex: 'mon7',
    resizable: true,
    width: 80,
  },
  {
    title: '08月',
    align: "center",
    dataIndex: 'mon8',
    resizable: true,
    width: 80,
  },
  {
    title: '09月',
    align: "center",
    dataIndex: 'mon9',
    resizable: true,
    width: 80,
  },
  {
    title: '10月',
    align: "center",
    dataIndex: 'mon10',
    resizable: true,
    width: 80,
  },
  {
    title: '11月',
    align: "center",
    dataIndex: 'mon11',
    resizable: true,
    width: 80,
  },
  {
    title: '12月',
    align: "center",
    dataIndex: 'mon12',
    resizable: true,
    width: 80,
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
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
];