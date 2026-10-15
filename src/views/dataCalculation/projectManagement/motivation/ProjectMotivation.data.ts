import { FormSchema } from '/@/components/Table';
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '人员姓名',
    field: 'userId',
    component: 'KtUserScrollSelect',
  },
  {
    label: '已完成节点',
    field: 'isFinishNode',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: '',
      checkedChildren:'是',
      checkedValue: '1',
    },
    defaultValue: '1'
  },
];