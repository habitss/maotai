import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { queryNoExistsUserList } from './UserOnlyDept.api';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '用户姓名',
    align: "center",
    dataIndex: 'realname'
  },
  {
    title: '部门名称',
    align: "center",
    dataIndex: 'userDeptName'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '用户姓名',
    field: 'realname',
    component: 'Input',
  },
  {
    label: '部门名称',
    field: 'userDeptId',
    component: 'KtDepartTreeSelect',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '用户姓名',
    field: 'userId',
    component: 'ApiSelect',
    componentProps: ({ formModel }) => {
      return {
        api: queryNoExistsUserList,
        params:{
          pageSize: 9999,
          isExists: formModel.id ? false : true
        },
        labelField: 'realname',
        valueField: 'id',
        getPopupContainer: () => document.body,
        labelInValue: true,
      }
    },
    dynamicDisabled: ({ values }) => {
      return values.id ? true : false;
    },
    required: true,
  },
  {
    label: '部门名称',
    field: 'userDeptId',
    component: 'KtDepartTreeSelect',
    componentProps: {
      getPopupContainer: () => document.body,
    },
    required: true,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];