import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: "center",
    dataIndex: 'factoryCode_dictText'
  },
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode_dictText'
  },
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode_dictText'
  },
  {
    title: '表名',
    align: "center",
    dataIndex: 'dfTableName'
  },
  {
    title: '表描述',
    align: "center",
    dataIndex: 'dfTableDesc'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '工厂',
    field: 'factoryCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'factory',
        onChange: () => {
          formModel.deptCode = null;
          formModel.lineCode = null;
        }
      }
    },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'depart',
        parentKey: formModel.factoryCode,
        onChange: () => {
          formModel.lineCode = null;
        }
      }
    },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
      }
    },
  },
  {
    label: '表名',
    field: 'dfTableName',
    component: 'JInput',
  },
  {
    label: '表描述',
    field: 'dfTableDesc',
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '工厂',
    field: 'factoryCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'factory',
        onChange: () => {
          formModel.deptCode = null;
          formModel.lineCode = null;
        }
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.id;
    },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'depart',
        parentKey: formModel.factoryCode,
        onChange: () => {
          formModel.lineCode = null;
        }
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.id;
    },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.id;
    },
  },
  {
    label: '表名',
    field: 'dfTableName',
    component: 'Input',
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.id;
    },
  },
  {
    label: '表描述',
    field: 'dfTableDesc',
    component: 'Input',
    required: true,
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];